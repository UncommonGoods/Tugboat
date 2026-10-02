// Copyright (c) 2025 Uncommon Goods LLC
// SPDX-License-Identifier: MPL-2.0

package com.uncommongoods.tugboat.bridge.util;

import com.uncommongoods.tugboat.engine.config.EngineConfig;
import com.uncommongoods.tugboat.engine.ports.shipping.model.ICarrierAccount;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CarrierAccountUtil {

    public static class CarrierAccountItem {
        private final String id;
        private final String displayName;

        public CarrierAccountItem(String id, String readable, String description) {
            this.id = id;
            // Get first 8 characters of ID for display
            String idPrefix = id != null ? (id.length() > 8 ? id.substring(0, 8) + "..." : id) : "";

            // Format as "readable | description | idPrefix" with fallbacks
            if (readable != null && description != null && !description.trim().isEmpty()) {
                this.displayName = readable + " | " + description + " | " + idPrefix;
            } else if (readable != null && !readable.trim().isEmpty()) {
                this.displayName = readable + " | " + idPrefix;
            } else {
                this.displayName = idPrefix;
            }
        }

        public String getId() {
            return id;
        }

        public String getDisplayName() {
            return displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    public static Map<String, List<ICarrierAccount>> loadAllCarrierAccounts(EngineConfig engineConfig) {
        Map<String, List<ICarrierAccount>> carrierAccountCache = new HashMap<>();

        try {
            var shippingClients = engineConfig.getShippingClients();
            for (String clientKey : shippingClients.keySet()) {
                try {
                    var shippingClient = shippingClients.get(clientKey);
                    if (shippingClient != null) {
                        var carrierAccounts = shippingClient.getCarrierAccountService().all();
                        carrierAccountCache.put(clientKey, carrierAccounts);
                        System.out.println("Loaded " + carrierAccounts.size() + " carrier accounts for " + clientKey);
                    }
                } catch (Exception e) {
                    System.err.println("Error loading carrier accounts for " + clientKey + ": " + e.getMessage());
                    // Continue with other clients if one fails
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading carrier accounts: " + e.getMessage());
        }

        return carrierAccountCache;
    }

    public static ICarrierAccount findCarrierAccount(Map<String, List<ICarrierAccount>> carrierAccountCache,
                                                    String shippingClientKey, String carrierAccountId) {
        List<ICarrierAccount> accounts = carrierAccountCache.get(shippingClientKey);
        if (accounts != null) {
            return accounts.stream()
                .filter(account -> account.getId().equals(carrierAccountId))
                .findFirst()
                .orElse(null);
        }
        return null;
    }
}
