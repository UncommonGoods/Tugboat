package com.uncommongoods.tugboat.engine.ports.shipping.model;

import java.util.Map;
import java.util.Set;

/**
 * Factory SPI for shipping model types.
 *
 * <p>Adapter modules implement this interface and register the implementation in
 * {@code META-INF/services/com.uncommongoods.tugboat.engine.ports.shipping.model.ModelTypeProvider}
 * so the engine can discover it with {@link java.util.ServiceLoader} -- the same
 * arrangement
 * {@link com.uncommongoods.tugboat.engine.ports.shipping.service.ShippingClientFactory}
 * uses for clients.
 *
 * <p>Teaching Tugboat to persist a new provider's models is therefore entirely an
 * adapter-module concern: implement this, register it, and the engine can read and
 * write those models. No engine source is involved.
 *
 * <p>{@link #providerType()} is the contract's hinge. The engine writes
 * {@link JsonSerializable#getProviderType()} into each serialized object and looks
 * the value back up here, so a provider whose token disagrees with its models'
 * would emit JSON it could never read. The engine rejects that mismatch on the
 * first write rather than letting it surface later as missing data.
 */
public interface ModelTypeProvider {

    /**
     * Unique token for this provider, e.g. {@code "easypost"}. Must equal
     * {@link JsonSerializable#getProviderType()} on every model it contributes.
     */
    String providerType();

    /** The model kinds this provider contributes. */
    Set<EntityType> supportedTypes();

    /**
     * The concrete class this provider uses for a kind. Used to validate the
     * registration at startup; must implement {@link EntityType#modelInterface()}.
     *
     * @return the class, or {@code null} if {@code type} is not in
     *     {@link #supportedTypes()}
     */
    Class<? extends JsonSerializable> modelClass(EntityType type);

    /**
     * Rebuild a model from its decoded JSON.
     *
     * <p>Values in {@code map} are raw decoded JSON: every number is a
     * {@code Double}, every date an ISO-8601 {@code String}, nested objects are
     * {@code Map}s and arrays are {@code List}s. {@link MapValues} has coercion
     * helpers for the common cases; use {@code resolver} for nested models rather
     * than constructing them directly.
     *
     * @param type the kind to build; always one of {@link #supportedTypes()}
     * @param map the object's fields, with the {@link EntityType#TYPE_KEY} entry
     *     already removed
     */
    JsonSerializable fromMap(EntityType type, Map<String, Object> map, ModelResolver resolver);
}
