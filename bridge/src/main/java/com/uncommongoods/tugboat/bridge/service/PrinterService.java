// Copyright (c) 2025 Uncommon Goods LLC
// SPDX-License-Identifier: MPL-2.0

package com.uncommongoods.tugboat.bridge.service;

import javax.print.*;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterJob;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.printing.PDFPrintable;
import org.apache.pdfbox.printing.Scaling;
import com.uncommongoods.tugboat.engine.ports.shipping.model.IPostageLabel;
import com.uncommongoods.tugboat.engine.ports.shipping.model.ShipmentOptionValues.LabelFormat;

public class PrinterService {

    private static final int ERROR_LINE_LENGTH = 40;

    public void printLabels(List<IPostageLabel> labels, String cargoId) throws Exception {
        if (labels == null || labels.isEmpty()) {
            String errorMessage = "No labels received for cargo: " + cargoId;
            System.err.println(errorMessage);
            printErrorMessage(errorMessage);
            return;
        }

        for (IPostageLabel label : labels) {
            if (label == null) {
                continue;
            }

            byte[] labelBytes;
            try {
                labelBytes = readLabelContent(label);
            } catch (Exception readException) {
                String errorMessage = "Error processing label for " + cargoId + ": " + readException.getMessage();
                System.err.println(errorMessage);
                printErrorMessage(errorMessage);
                throw new Exception("Error decoding label file: " + readException.getMessage());
            }

            if (labelBytes == null || labelBytes.length == 0) {
                String errorMessage = "Label has no content for cargo: " + cargoId;
                System.err.println(errorMessage);
                printErrorMessage(errorMessage);
                continue;
            }

            printLabel(labelBytes, detectFormat(label, labelBytes), cargoId);
        }
    }

    private byte[] readLabelContent(IPostageLabel label) throws Exception {
        String labelFile = label.getLabelFile();
        if (labelFile != null && !labelFile.isBlank()) {
            return Base64.getDecoder().decode(labelFile);
        }

        String url = label.getLabelPdfUrl() != null && !label.getLabelPdfUrl().isBlank()
            ? label.getLabelPdfUrl()
            : label.getLabelUrl();
        if (url == null || url.isBlank()) {
            return null;
        }

        System.out.println("Label sent by reference; downloading from " + url);
        HttpRequest request = HttpRequest.newBuilder(URI.create(url)).GET().build();
        HttpResponse<byte[]> response = HttpClient.newHttpClient()
            .send(request, HttpResponse.BodyHandlers.ofByteArray());
        if (response.statusCode() != 200) {
            throw new Exception("Label download failed with HTTP " + response.statusCode());
        }
        return response.body();
    }

    private LabelFormat detectFormat(IPostageLabel label, byte[] labelBytes) {
        String head = new String(labelBytes, 0, Math.min(labelBytes.length, 8), StandardCharsets.ISO_8859_1);
        if (head.startsWith("%PDF")) {
            return LabelFormat.PDF;
        }
        if (head.startsWith("^XA")) {
            return LabelFormat.ZPL;
        }

        String fileType = label.getLabelFileType();
        if (fileType != null) {
            String lower = fileType.toLowerCase();
            if (lower.contains("pdf")) {
                return LabelFormat.PDF;
            }
            if (lower.contains("zpl")) {
                return LabelFormat.ZPL;
            }
        }

        return PrinterPreferences.resolveLabelFormat();
    }

    private void printLabel(byte[] labelBytes, LabelFormat format, String cargoId) {
        boolean isPdf = format == LabelFormat.PDF;
        String printerName = isPdf ? PrinterPreferences.getPdfPrinter() : PrinterPreferences.getZplPrinter();
        if (!PrinterPreferences.isConfigured(printerName)) {
            System.out.println("No " + format + " printer configured - label not printed");
            printErrorMessage("No " + format + " printer configured for cargo: " + cargoId);
            return;
        }

        CompletableFuture
            .supplyAsync(() -> isPdf
                ? printPdf(printerName, labelBytes)
                : printToZplPrinter(printerName, new String(labelBytes, StandardCharsets.US_ASCII)))
            .thenAccept(success -> {
                if (success) {
                    System.out.println("Label printed successfully for cargo: " + cargoId);
                } else {
                    String printErrorMessage = "Failed to print label for cargo: " + cargoId;
                    System.err.println(printErrorMessage);
                    printErrorMessage(printErrorMessage);
                }
            })
            .exceptionally(throwable -> {
                String printErrorMessage = "Error printing label for cargo " + cargoId + ": " + throwable.getMessage();
                System.err.println(printErrorMessage);
                throwable.printStackTrace();
                printErrorMessage(printErrorMessage);
                return null;
            });
    }

    public CompletableFuture<Boolean> printErrorMessage(String errorMessage) {
        if (PrinterPreferences.isZplConfigured()) {
            String printerName = PrinterPreferences.getZplPrinter();
            return CompletableFuture.supplyAsync(() -> printToZplPrinter(printerName, createErrorZpl(errorMessage)));
        }

        if (PrinterPreferences.isPdfConfigured()) {
            String printerName = PrinterPreferences.getPdfPrinter();
            return CompletableFuture.supplyAsync(() -> printTextPage(printerName, splitMessage(errorMessage)));
        }

        System.err.println("No printer configured for error printing: " + errorMessage);
        return CompletableFuture.completedFuture(false);
    }

    private boolean printToZplPrinter(String printerName, String zplContent) {
        try {
            PrintService printService = findPrintService(printerName);
            if (printService == null) {
                System.err.println("ZPL printer not found: " + printerName);
                return false;
            }

            DocPrintJob job = printService.createPrintJob();
            DocFlavor flavor = DocFlavor.BYTE_ARRAY.AUTOSENSE;
            Doc doc = new SimpleDoc(zplContent.getBytes("ASCII"), flavor, null);
            PrintRequestAttributeSet pras = new HashPrintRequestAttributeSet();

            job.print(doc, pras);
            System.out.println("ZPL content sent successfully to printer: " + printerName);
            return true;

        } catch (Exception e) {
            System.err.println("Error printing to ZPL printer '" + printerName + "': " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private boolean printPdf(String printerName, byte[] pdfBytes) {
        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            PrintService printService = findPrintService(printerName);
            if (printService == null) {
                System.err.println("PDF printer not found: " + printerName);
                return false;
            }

            PrinterJob job = PrinterJob.getPrinterJob();
            job.setPrintService(printService);
            job.setJobName("Tugboat Label");

            PageFormat pageFormat = job.defaultPage();
            pageFormat.setOrientation(isLandscape(document) ? PageFormat.LANDSCAPE : PageFormat.PORTRAIT);
            job.setPrintable(new PDFPrintable(document, Scaling.SHRINK_TO_FIT, false, 0, true), pageFormat);

            job.print();
            System.out.println("PDF sent successfully to printer: " + printerName);
            return true;

        } catch (Exception e) {
            System.err.println("Error printing to PDF printer '" + printerName + "': " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private boolean isLandscape(PDDocument document) {
        if (document.getNumberOfPages() == 0) {
            return false;
        }
        PDRectangle box = document.getPage(0).getMediaBox();
        return box.getWidth() > box.getHeight();
    }

    private boolean printTextPage(String printerName, String[] lines) {
        try {
            PrintService printService = findPrintService(printerName);
            if (printService == null) {
                System.err.println("PDF printer not found: " + printerName);
                return false;
            }

            PrinterJob job = PrinterJob.getPrinterJob();
            job.setPrintService(printService);
            job.setJobName("Tugboat Message");
            job.setPrintable((Graphics graphics, PageFormat pageFormat, int pageIndex) -> {
                if (pageIndex > 0) {
                    return Printable.NO_SUCH_PAGE;
                }
                Graphics2D g2d = (Graphics2D) graphics;
                g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
                g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
                int y = 40;
                for (String line : lines) {
                    g2d.drawString(line, 0, y);
                    y += 32;
                }
                return Printable.PAGE_EXISTS;
            });

            job.print();
            System.out.println("Message sent successfully to printer: " + printerName);
            return true;

        } catch (Exception e) {
            System.err.println("Error printing message to '" + printerName + "': " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private String createErrorZpl(String errorMessage) {
        StringBuilder zpl = new StringBuilder();
        zpl.append("^XA\n");

        // titel
        zpl.append("^FO75,50^FWN^A0,200,200^FDERROR^FS\n");

        zpl.append("^FO50,250^FWN^A0,90,90^FB700,14,,\n^FD");
        zpl.append(errorMessage);
        zpl.append("^FS\n");

        zpl.append("^FX<ERROR>^FS\n");
        zpl.append("^XZ\n");

        return zpl.toString();
    }

    private String[] splitMessage(String message) {
        if (message.length() <= ERROR_LINE_LENGTH) {
            return new String[]{message};
        }

        String[] words = message.split(" ");
        StringBuilder currentLine = new StringBuilder();
        java.util.List<String> lines = new java.util.ArrayList<>();

        for (String word : words) {
            if (currentLine.length() + word.length() + 1 > ERROR_LINE_LENGTH) {
                if (!currentLine.isEmpty()) {
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder();
                }

                // If single word is too long, truncate it
                if (word.length() > ERROR_LINE_LENGTH) {
                    word = word.substring(0, ERROR_LINE_LENGTH - 3) + "...";
                }
            }

            if (!currentLine.isEmpty()) {
                currentLine.append(" ");
            }
            currentLine.append(word);
        }

        if (!currentLine.isEmpty()) {
            lines.add(currentLine.toString());
        }

        return lines.toArray(new String[0]);
    }

    private PrintService findPrintService(String printerName) {
        PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);
        for (PrintService service : printServices) {
            if (service.getName().equals(printerName)) {
                return service;
            }
        }
        return null;
    }

    /** Test the given ZPL printer, which need not be the saved one. */
    public CompletableFuture<Boolean> testZplPrinter(String printerName) {
        return CompletableFuture.supplyAsync(() -> {
            String testZpl = "^XA^FO50,50^A0N,50,50^FDPrint successful: " + timestamp() + "^FS" +
                             "^FO50,120^A0N,30,30^FDTugboat Bridge^FS^XZ";
            return printToZplPrinter(printerName, testZpl);
        });
    }

    /**
     * Test the given PDF printer with a real generated PDF, so the button
     * exercises the same rendering path a shipping label takes.
     */
    public CompletableFuture<Boolean> testPdfPrinter(String printerName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return printPdf(printerName, createTestPdf());
            } catch (Exception e) {
                System.err.println("PDF printer test failed: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        });
    }

    private byte[] createTestPdf() throws Exception {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            // 4x6 at 72dpi
            PDPage page = new PDPage(new PDRectangle(288, 432));
            document.addPage(page);

            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                content.beginText();
                content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 16);
                content.newLineAtOffset(20, 380);
                content.showText("Print successful");
                content.newLineAtOffset(0, -28);
                content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                content.showText(timestamp());
                content.newLineAtOffset(0, -20);
                content.showText("Tugboat Bridge");
                content.endText();
            }

            document.save(output);
            return output.toByteArray();
        }
    }

    private String timestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
