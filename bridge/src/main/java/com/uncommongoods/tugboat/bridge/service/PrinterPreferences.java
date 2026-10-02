// Copyright (c) 2025 Uncommon Goods LLC
// SPDX-License-Identifier: MPL-2.0

package com.uncommongoods.tugboat.bridge.service;

import java.util.prefs.Preferences;
import com.uncommongoods.tugboat.bridge.SettingsController;
import com.uncommongoods.tugboat.engine.ports.shipping.model.ShipmentOptionValues.LabelFormat;

public final class PrinterPreferences {

    public static final String NO_ZPL_PRINTERS = "No ZPL printers found";
    public static final String NO_PDF_PRINTERS = "No PDF printers found";

    private static final Preferences prefs = Preferences.userNodeForPackage(SettingsController.class);
    private static final String DEVICE_PREFS_PREFIX = "tugboat.device.";
    private static final String ZPL_PRINTER_KEY = DEVICE_PREFS_PREFIX + "zplPrinter";
    private static final String PDF_PRINTER_KEY = DEVICE_PREFS_PREFIX + "pdfPrinter";
    private static final String LABEL_FORMAT_KEY = DEVICE_PREFS_PREFIX + "labelFormat";

    private PrinterPreferences() {
    }

    public static String getZplPrinter() {
        return prefs.get(ZPL_PRINTER_KEY, "");
    }

    public static String getPdfPrinter() {
        return prefs.get(PDF_PRINTER_KEY, "");
    }

    /** A printer name is usable if the user picked one rather than a placeholder row. */
    public static boolean isConfigured(String printerName) {
        return printerName != null && !printerName.isBlank() &&
               !printerName.equals(NO_ZPL_PRINTERS) && !printerName.equals(NO_PDF_PRINTERS);
    }

    public static boolean isZplConfigured() {
        return isConfigured(getZplPrinter());
    }

    public static boolean isPdfConfigured() {
        return isConfigured(getPdfPrinter());
    }

    public static LabelFormat getPreferredFormat() {
        try {
            return LabelFormat.valueOf(prefs.get(LABEL_FORMAT_KEY, LabelFormat.ZPL.name()));
        } catch (IllegalArgumentException e) {
            return LabelFormat.ZPL;
        }
    }

    public static LabelFormat resolveLabelFormat() {
        boolean zpl = isZplConfigured();
        boolean pdf = isPdfConfigured();
        if (zpl && pdf) {
            return getPreferredFormat();
        }
        return pdf ? LabelFormat.PDF : LabelFormat.ZPL;
    }

    public static void save(String zplPrinter, String pdfPrinter, LabelFormat preferredFormat) {
        prefs.put(ZPL_PRINTER_KEY, zplPrinter != null ? zplPrinter : "");
        prefs.put(PDF_PRINTER_KEY, pdfPrinter != null ? pdfPrinter : "");
        prefs.put(LABEL_FORMAT_KEY, (preferredFormat != null ? preferredFormat : LabelFormat.ZPL).name());
    }
}
