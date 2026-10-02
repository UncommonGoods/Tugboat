// Copyright (c) 2025 Uncommon Goods LLC
// SPDX-License-Identifier: MPL-2.0

package com.uncommongoods.tugboat.bridge;

import com.uncommongoods.tugboat.bridge.util.PrefsMigration;

public class Main {
    public static void main(String[] args) {
        // Must precede loading of any class with a static prefs field.
        PrefsMigration.migrateIfNeeded();
        System.setProperty("javafx.preloader", "com.uncommongoods.tugboat.bridge.BridgePreloader");
        BridgeMain.launch(BridgeMain.class, args);
    }
}