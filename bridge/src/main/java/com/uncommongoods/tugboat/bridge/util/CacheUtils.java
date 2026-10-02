// Copyright (c) 2025 Uncommon Goods LLC
// SPDX-License-Identifier: MPL-2.0

package com.uncommongoods.tugboat.bridge.util;

import com.uncommongoods.tugboat.engine.ports.cache.ICacheClient;

import java.util.HashSet;
import java.util.Set;

/**
 * Utility methods for cache operations that are not direct Redis commands
 * but are commonly needed by bridge applications.
 */
public class CacheUtils {

    /**
     * Scans all keys matching a pattern, with an optional limit.
     * This is a convenience method that handles cursor iteration internally.
     *
     * @param cacheClient the cache client to use
     * @param pattern the glob-style pattern to match keys against
     * @param limit maximum number of keys to return (0 = no limit)
     * @return Set of matching keys (up to limit if specified)
     */
    public static Set<String> scanKeys(ICacheClient cacheClient, String pattern, int limit) {
        Set<String> keys = new HashSet<>();
        String cursor = "0";

        do {
            // Create scan params with pattern matching
            ICacheClient.ScanParams params = new BridgeScanParams().match(pattern).count(1000);
            ICacheClient.ScanResult<String> result = cacheClient.scan(cursor, params);

            for (String key : result.getResult()) {
                keys.add(key);

                // Check limit if specified
                if (limit > 0 && keys.size() >= limit) {
                    return keys;
                }
            }

            cursor = result.getCursor();
        } while (!"0".equals(cursor));

        return keys;
    }

    /**
     * Scans all keys matching a pattern with no limit.
     *
     * @param cacheClient the cache client to use
     * @param pattern the glob-style pattern to match keys against
     * @return Set of all matching keys
     */
    public static Set<String> scanKeys(ICacheClient cacheClient, String pattern) {
        return scanKeys(cacheClient, pattern, 0);
    }

    /**
     * Simple ScanParams implementation for bridge use
     */
    private static class BridgeScanParams implements ICacheClient.ScanParams {
        private String pattern;
        private Integer count = 10;

        @Override
        public ICacheClient.ScanParams match(String pattern) {
            this.pattern = pattern;
            return this;
        }

        @Override
        public ICacheClient.ScanParams count(Integer count) {
            this.count = count;
            return this;
        }

        @Override
        public String match() {
            return pattern;
        }
    }
}
