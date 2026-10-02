// Copyright (c) 2026 Uncommon Goods LLC
// SPDX-License-Identifier: MPL-2.0

package com.uncommongoods.tugboat.bridge.util;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * One-time migration of Java Preferences from the pre-rename package namespace.
 *
 * The 2026-08 package rename (org.bushwald -> com.uncommongoods) moved every
 * {@code Preferences.userNodeForPackage(...)} node, which would otherwise strand
 * all saved settings — including the AES key EncryptionUtil stores, without
 * which every encrypted value is unrecoverable. This copies the old subtree to
 * the new location on first launch. The old subtree is deliberately left in
 * place so rolling back to a pre-rename install still finds its config.
 */
public final class PrefsMigration {

    private static final String OLD_ROOT = "/org/bushwald/tugboat";
    private static final String NEW_ROOT = "/com/uncommongoods/tugboat";
    private static final String MIGRATED_MARKER = "prefs.migrated.from.bushwald";

    private PrefsMigration() {
    }

    /**
     * Must run before any class whose static initializer touches its prefs node
     * (ConfigurationManager, SettingsController, SignInController, EncryptionUtil)
     * is loaded — i.e. first thing in Main.main().
     */
    public static void migrateIfNeeded() {
        try {
            Preferences userRoot = Preferences.userRoot();
            if (!userRoot.nodeExists(OLD_ROOT.substring(1))) {
                return;
            }
            Preferences newRoot = userRoot.node(NEW_ROOT.substring(1));
            if (newRoot.get(MIGRATED_MARKER, null) != null) {
                return;
            }
            copySubtree(userRoot.node(OLD_ROOT.substring(1)), newRoot);
            newRoot.put(MIGRATED_MARKER, "true");
            newRoot.flush();
        } catch (BackingStoreException e) {
            // Non-fatal: worst case the app starts unconfigured, same as a fresh install.
            System.err.println("Preferences migration from " + OLD_ROOT + " failed: " + e.getMessage());
        }
    }

    private static void copySubtree(Preferences from, Preferences to) throws BackingStoreException {
        for (String key : from.keys()) {
            String value = from.get(key, null);
            if (value != null) {
                to.put(key, value);
            }
        }
        for (String child : from.childrenNames()) {
            copySubtree(from.node(child), to.node(child));
        }
    }
}
