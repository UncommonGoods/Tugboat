// Copyright (c) 2025 Uncommon Goods LLC
// SPDX-License-Identifier: MPL-2.0

package com.uncommongoods.tugboat.bridge.service;

import com.google.gson.Gson;
import com.uncommongoods.tugboat.bridge.SettingsController;
import com.uncommongoods.tugboat.engine.model.TugboatAddress;
import com.uncommongoods.tugboat.engine.serialization.TugboatGson;

import java.util.prefs.Preferences;

/**
 * Local storage for the shipping origin address used when there is no
 * {@link com.uncommongoods.tugboat.engine.manifest.PickupFacility} i.e., when no
 * cache is configured and there is nowhere to keep a facility.
 *The address lives in the same Prefs node as the rest of the Bridge's
 * config as a single JSON object {@code tugboat.origin}.
 */
public final class OriginAddressStore {

    private static final Preferences prefs = Preferences.userNodeForPackage(SettingsController.class);
    private static final String ORIGIN_KEY = "tugboat.origin";
    private static final Gson gson = TugboatGson.cargo();

    private OriginAddressStore() {}

    public static TugboatAddress load() {
        String json = prefs.get(ORIGIN_KEY, "");
        if (json.isBlank()) {
            return null;
        }
        try {
            return gson.fromJson(json, TugboatAddress.class);
        } catch (Exception e) {
            System.err.println("Ignoring unreadable origin address: " + e.getMessage());
            return null;
        }
    }

    public static void save(TugboatAddress address) {
        if (address == null) {
            clear();
            return;
        }
        prefs.put(ORIGIN_KEY, gson.toJson(address));
    }

    public static void clear() {
        prefs.remove(ORIGIN_KEY);
    }
}
