// Copyright (c) 2025 Uncommon Goods LLC
// SPDX-License-Identifier: MPL-2.0

package com.uncommongoods.tugboat.engine.config;

import com.easypost.service.EasyPostClient;
import com.uncommongoods.tugboat.engine.ports.cache.ICacheClient;
import com.uncommongoods.tugboat.engine.ports.shipping.service.IShippingClient;
import com.uncommongoods.tugboat.engine.ports.shipping.service.ShippingClientAdapter;

import java.util.HashMap;
import java.util.Map;

public class EngineConfig {
    public static final String DEFAULT_CLIENT_KEY = "default";
    private static final long defaultCacheExpirySeconds = 1210000; // two weeks
    private final Map<String, IShippingClient> shippingClients;
    private final ICacheClient cacheClient;
    private final String cachePrefix;
    private final long cacheExpirySeconds;

    private EngineConfig(Builder builder) {
        this.shippingClients = new HashMap<>(builder.shippingClients);
        this.cacheClient = builder.cacheClient;
        this.cachePrefix = builder.cachePrefix;
        this.cacheExpirySeconds = builder.cacheExpirySeconds;
    }

    public static Builder builder() {
        return new Builder();
    }

    public IShippingClient getDefaultShippingClient() {
        return shippingClients.get(DEFAULT_CLIENT_KEY);
    }

    public Map<String, IShippingClient> getShippingClients() {
        return new HashMap<>(shippingClients);
    }

    public IShippingClient getShippingClient(String clientKey) {
        return shippingClients.get(clientKey);
    }

    public ICacheClient getCacheClient() {
        return cacheClient;
    }

    public String getCachePrefix() {
        return this.cachePrefix.isBlank() ? this.cachePrefix : this.cachePrefix + ":";
    }

    public long getCacheExpirySeconds() {
        return this.cacheExpirySeconds;
    }

    public static class Builder {
        private final Map<String, IShippingClient> shippingClients = new HashMap<>();
        private ICacheClient cacheClient;
        private String cachePrefix = "";
        private long cacheExpirySeconds = defaultCacheExpirySeconds;

        public Builder withShippingClient(IShippingClient client) {
            this.shippingClients.put(DEFAULT_CLIENT_KEY, client);
            return this;
        }

        public Builder withEasyPostClient(EasyPostClient client) {
            IShippingClient shippingClient = new ShippingClientAdapter(client);
            this.shippingClients.put(DEFAULT_CLIENT_KEY, shippingClient);
            return this;
        }

        public Builder withShippingClient(String key, IShippingClient client) {
            this.shippingClients.put(key, client);
            return this;
        }

        public Builder withShippingClients(Map<String, IShippingClient> shippingClients) {
            this.shippingClients.putAll(shippingClients);
            return this;
        }

        /**
         * Set the cache client. Optional — an EngineConfig without one runs
         * cacheless: Tugboat state is not persisted between operations, cargo
         * locking is skipped, and pickup/manifest features are unavailable.
         *
         * <p>Cache clients are built by a
         * {@code com.uncommongoods.tugboat.engine.ports.cache.CacheClientFactory}
         * discovered via {@link java.util.ServiceLoader}, so the engine never
         * names a specific cache technology.
         */
        public Builder withCacheClient(ICacheClient cacheClient) {
            this.cacheClient = cacheClient;
            return this;
        }

        public Builder withCachePrefix(String cachePrefix) {
            this.cachePrefix = cachePrefix != null ? cachePrefix : "";
            return this;
        }

        public Builder withCacheExpirySeconds(long cacheExpirySeconds) {
            if (cacheExpirySeconds < 0) {
                throw new IllegalArgumentException("cacheExpirySeconds must be a positive integer");
            }
            this.cacheExpirySeconds = cacheExpirySeconds;
            return this;
        }

        public EngineConfig build() {
            return new EngineConfig(this);
        }
    }
}
