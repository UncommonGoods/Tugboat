// Copyright (c) 2025 Uncommon Goods LLC
// SPDX-License-Identifier: MPL-2.0

package com.uncommongoods.tugboat.engine.manifest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import com.uncommongoods.tugboat.engine.config.EngineConfig;
import com.uncommongoods.tugboat.engine.serialization.LocalDateTimeAdapter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

public class Manifest {
    private static final String CACHE_PREFIX = "TBMANIFEST:";
    private final EngineConfig engineConfig; // Not exposed, won't be serialized
    @Expose
    private final String manifestGroupId;
    @Expose
    private final String manifestBatchId;
    @Expose
    private final String manifestId;
    @Expose
    private String documentType; // "TEXT", "PDF", "CSV"
    @Expose
    private String documentContent;
    @Expose
    private String documentSummary;
    @Expose
    private LocalDateTime createdAt;
    private final Gson gson = new GsonBuilder()
        .excludeFieldsWithoutExposeAnnotation()
        .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
        .create();

    public Manifest(EngineConfig engineConfig, String manifestGroupId, String manifestBatchId, String manifestId) throws IllegalStateException {
        if (engineConfig.getCacheClient() == null) {
            throw new IllegalStateException("Cache client is null");
        }
        if (manifestGroupId == null || manifestGroupId.trim().isEmpty()) {
            throw new IllegalArgumentException("manifestGroupId is required");
        }
        if (manifestBatchId == null || manifestBatchId.trim().isEmpty()) {
            throw new IllegalArgumentException("manifestBatchId is required");
        }
        if (manifestId == null || manifestId.trim().isEmpty()) {
            throw new IllegalArgumentException("manifestId is required");
        }
        this.engineConfig = engineConfig;
        this.manifestGroupId = manifestGroupId;
        this.manifestBatchId = manifestBatchId;
        this.manifestId = manifestId;
        this.createdAt = LocalDateTime.now();
        this.documentType = "TEXT";
    }

    public Manifest create() {
        generateManifestContent();
        save();
        return this;
    }

    public Manifest retrieve() {
        String cacheKey = engineConfig.getCachePrefix() + CACHE_PREFIX + manifestBatchId + ":" + manifestId;
        String manifestJson = engineConfig.getCacheClient().get(cacheKey);

        if (manifestJson != null && !manifestJson.trim().isEmpty()) {
            Manifest manifest = gson.fromJson(manifestJson, Manifest.class);
            this.documentType = manifest.documentType;
            this.documentContent = manifest.documentContent;
            this.documentSummary = manifest.documentSummary;
            this.createdAt = manifest.createdAt;
        }
        return this;
    }

    public Manifest save() {
        String cacheKey = engineConfig.getCachePrefix() + CACHE_PREFIX + manifestBatchId;
        String manifestJson = gson.toJson(this);
        engineConfig.getCacheClient().setEx(cacheKey, engineConfig.getCacheExpirySeconds(), manifestJson);
        return this;
    }

    private void generateManifestContent() {
        ManifestBatch batch = new ManifestBatch(engineConfig, manifestGroupId, manifestBatchId);
        batch.retrieve();

        List<ManifestBatchCargo> cargos = batch.getManifestBatchCargo();

        if ("CSV".equals(documentType)) {
            generateCsvContent(cargos);
        } else if ("PDF".equals(documentType)) {
            generatePdfContent(cargos);
        } else {
            generateTextContent(cargos);
        }

        generateSummary(cargos);
    }

    private void generateTextContent(List<ManifestBatchCargo> cargos) {
        StringBuilder content = new StringBuilder();
        content.append("MANIFEST REPORT\\n");
        content.append("=================\\n");
        content.append("Manifest ID: ").append(manifestId).append("\\n");
        content.append("Batch ID: ").append(manifestBatchId).append("\\n");
        content.append("Created: ").append(createdAt).append("\\n");
        content.append("Total Packages: ").append(cargos.size()).append("\\n\\n");

        content.append("PACKAGE DETAILS:\\n");
        content.append("----------------\\n");

        for (ManifestBatchCargo cargo : cargos) {
            content.append("Cargo ID: ").append(cargo.getCargoId()).append("\\n");
            content.append("External ID: ").append(cargo.getExternalPackageId()).append("\\n");
            if (cargo.getCarrierService() != null) {
                content.append("Carrier: ").append(cargo.getCarrierService().carrier()).append("\\n");
                content.append("Service: ").append(cargo.getCarrierService().service()).append("\\n");
            }
            if (cargo.getTrackingCodes() != null && !cargo.getTrackingCodes().isEmpty()) {
                content.append("Tracking: ").append(String.join(", ", cargo.getTrackingCodes())).append("\\n");
            }
            content.append("\\n");
        }

        this.documentContent = content.toString();
    }

    private void generateCsvContent(List<ManifestBatchCargo> cargos) {
        StringBuilder content = new StringBuilder();
        content.append("Cargo ID,External ID,Carrier,Service,Tracking Codes\\n");

        for (ManifestBatchCargo cargo : cargos) {
            content.append(cargo.getCargoId()).append(",");
            content.append(cargo.getExternalPackageId() != null ? cargo.getExternalPackageId() : "").append(",");
            content.append(cargo.getCarrierService() != null ? cargo.getCarrierService().carrier() : "").append(",");
            content.append(cargo.getCarrierService() != null ? cargo.getCarrierService().service() : "").append(",");
            content.append(cargo.getTrackingCodes() != null ? String.join(";", cargo.getTrackingCodes()) : "").append("\\n");
        }

        this.documentContent = content.toString();
    }

    private void generatePdfContent(List<ManifestBatchCargo> cargos) {
        try (PDDocument document = new PDDocument()) {
            generatePdfPages(document, cargos);

            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                document.save(outputStream);
                byte[] pdfBytes = outputStream.toByteArray();
                this.documentContent = Base64.getEncoder().encodeToString(pdfBytes);
            }

        } catch (IOException e) {
            generateTextContent(cargos);
            this.documentContent = "[PDF GENERATION ERROR: " + e.getMessage() + "]\\n" + this.documentContent;
        }
    }

    private void generatePdfPages(PDDocument document, List<ManifestBatchCargo> cargos) throws IOException {
        PDType1Font font = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        PDType1Font boldFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

        float margin = 50;
        float fontSize = 12;
        float lineHeight = 15;
        float yPosition = 750;

        PDPage currentPage = new PDPage();
        document.addPage(currentPage);
        PDPageContentStream contentStream = new PDPageContentStream(document, currentPage);

        try {
            contentStream.beginText();
            contentStream.setFont(boldFont, 16);
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("MANIFEST REPORT");
            contentStream.endText();
            yPosition -= 30;

            contentStream.beginText();
            contentStream.setFont(font, fontSize);
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Manifest ID: " + manifestId);
            contentStream.newLineAtOffset(0, -lineHeight);
            contentStream.showText("Batch ID: " + manifestBatchId);
            contentStream.newLineAtOffset(0, -lineHeight);
            contentStream.showText("Created: " + createdAt.toString());
            contentStream.newLineAtOffset(0, -lineHeight);
            contentStream.showText("Total Packages: " + cargos.size());
            contentStream.endText();
            yPosition -= 80;

            contentStream.beginText();
            contentStream.setFont(boldFont, fontSize);
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("PACKAGE DETAILS:");
            contentStream.endText();
            yPosition -= 25;

            contentStream.setFont(font, 10);
            for (ManifestBatchCargo cargo : cargos) {
                if (yPosition < 100) {
                    contentStream.close();
                    currentPage = new PDPage();
                    document.addPage(currentPage);
                    contentStream = new PDPageContentStream(document, currentPage);
                    yPosition = 750;
                    contentStream.setFont(font, 10);
                }

                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Cargo ID: " + (cargo.getCargoId() != null ? cargo.getCargoId() : "N/A"));
                contentStream.newLineAtOffset(0, -12);
                contentStream.showText("External ID: " + (cargo.getExternalPackageId() != null ? cargo.getExternalPackageId() : "N/A"));

                if (cargo.getCarrierService() != null) {
                    contentStream.newLineAtOffset(0, -12);
                    contentStream.showText("Carrier: " + cargo.getCarrierService().carrier());
                    contentStream.newLineAtOffset(0, -12);
                    contentStream.showText("Service: " + cargo.getCarrierService().service());
                }

                if (cargo.getTrackingCodes() != null && !cargo.getTrackingCodes().isEmpty()) {
                    contentStream.newLineAtOffset(0, -12);
                    contentStream.showText("Tracking: " + String.join(", ", cargo.getTrackingCodes()));
                }

                contentStream.endText();
                yPosition -= 80;
            }
        } finally {
            contentStream.close();
        }
    }

    private void generateSummary(List<ManifestBatchCargo> cargos) {
        StringBuilder summary = new StringBuilder();
        summary.append("Manifest ").append(manifestId);
        summary.append(" contains ").append(cargos.size()).append(" packages");
        if (!cargos.isEmpty()) {
            long uniqueCarriers = cargos.stream()
                .filter(c -> c.getCarrierService() != null)
                .map(c -> c.getCarrierService().carrier())
                .distinct()
                .count();
            summary.append(" across ").append(uniqueCarriers).append(" carriers");
        }
        this.documentSummary = summary.toString();
    }

    public Object getDocument() {
        return documentContent;
    }

    public String getManifestBatchId() {
        return manifestBatchId;
    }

    public String getManifestId() {
        return manifestId;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public String getDocumentContent() {
        return documentContent;
    }

    public String getDocumentSummary() {
        return documentSummary;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
