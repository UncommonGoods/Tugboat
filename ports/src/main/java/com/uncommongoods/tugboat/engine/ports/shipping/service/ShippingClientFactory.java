package com.uncommongoods.tugboat.engine.ports.shipping.service;

import java.util.List;
import java.util.Map;

/**
 * Factory SPI for shipping clients.
 *
 * <p>Adapter modules implement this interface and register the implementation
 * in {@code META-INF/services/com.uncommongoods.tugboat.engine.ports.shipping.service.ShippingClientFactory}
 * so hosts can discover it with {@link java.util.ServiceLoader}. Hosts render
 * {@link #configKeys()} as configuration fields (the Bridge settings page shows
 * one input per key) and call {@link #create(Map)} with the collected values.
 *
 * <p>Adding a new shipping provider is therefore an adapter-module concern:
 * implement the factory, register it, and it appears in the host's client type
 * list — no host code changes.
 */
public interface ShippingClientFactory {

    /** Unique type identifier for this client, e.g. {@code "easypost"}. */
    String type();

    /**
     * The configuration field names this client requires, in display order,
     * e.g. {@code ["API Key"]}. Used both as UI labels and as the keys of the
     * map passed to {@link #create(Map)}.
     */
    List<String> configKeys();

    /**
     * Build a client from the given configuration, keyed by
     * {@link #configKeys()}. Implementations should throw
     * {@link IllegalArgumentException} (or another RuntimeException) when the
     * configuration is missing or invalid; callers catch per-client and log.
     */
    IShippingClient create(Map<String, String> config);
}
