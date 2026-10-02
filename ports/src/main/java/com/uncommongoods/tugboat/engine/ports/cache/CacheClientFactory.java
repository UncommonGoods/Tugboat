package com.uncommongoods.tugboat.engine.ports.cache;

import java.util.List;
import java.util.Map;

/**
 * Factory SPI for cache clients.
 *
 * <p>Adapter modules implement this interface and register the implementation
 * in {@code META-INF/services/com.uncommongoods.tugboat.engine.ports.cache.CacheClientFactory}
 * so hosts can discover it with {@link java.util.ServiceLoader}. Hosts resolve
 * {@link #configKeys()} from their own configuration store — the Bridge renders
 * one settings row per key, XO reads them as environment variables — and call
 * {@link #create(Map)} with the collected values.
 *
 * <p>Unlike shipping clients, a host uses a single cache client and does not ask
 * the user to choose one: it takes the first registered factory, or the one
 * whose {@link #type()} matches the optional {@code CACHE_TYPE} setting. The
 * cache is optional, so a host with no factory on the classpath — or with no
 * values configured — runs without one.
 */
public interface CacheClientFactory {

    /** Unique type identifier for this client, e.g. {@code "redis"}. */
    String type();

    /**
     * The configuration field names this client requires, in display order,
     * e.g. {@code ["Cache URL", "Cache Port"]}. Used both as UI labels and as
     * the keys of the map passed to {@link #create(Map)}. Hosts canonicalize
     * these names for storage and environment lookup, so "Cache URL" and
     * {@code CACHE_URL} refer to the same value.
     */
    List<String> configKeys();

    /**
     * Build a client from the given configuration, keyed by
     * {@link #configKeys()}. Implementations should throw
     * {@link IllegalArgumentException} (or another RuntimeException) when the
     * configuration is missing or invalid; callers catch and log, then continue
     * without a cache.
     *
     * <p>The returned client owns whatever connection resources it created. If
     * it holds any, it should implement {@link AutoCloseable} so hosts can
     * release them.
     */
    ICacheClient create(Map<String, String> config);
}
