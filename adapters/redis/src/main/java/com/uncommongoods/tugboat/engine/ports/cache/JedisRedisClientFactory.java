package com.uncommongoods.tugboat.engine.ports.cache;

import java.util.List;
import java.util.Map;

public class JedisRedisClientFactory implements CacheClientFactory {

    public static final String CACHE_URL = "Cache URL";
    public static final String CACHE_PORT = "Cache Port";

    private static final int DEFAULT_PORT = 6379;

    @Override
    public String type() {
        return "redis";
    }

    @Override
    public List<String> configKeys() {
        return List.of(CACHE_URL, CACHE_PORT);
    }

    @Override
    public ICacheClient create(Map<String, String> config) {
        String url = config.get(CACHE_URL);
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("redis cache client requires '" + CACHE_URL + "'");
        }

        String portValue = config.get(CACHE_PORT);
        int port = DEFAULT_PORT;
        if (portValue != null && !portValue.isBlank()) {
            try {
                port = Integer.parseInt(portValue.trim());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                    "redis cache client '" + CACHE_PORT + "' must be a number, got '" + portValue + "'", e);
            }
        }

        return new JedisRedisAdapter(url.trim(), port);
    }
}
