package com.uncommongoods.tugboat.engine.ports.shipping.model;

import com.google.gson.JsonElement;

/**
 * Interface that ensures implementing classes can be serialized to JSON.
 * This provides a functional contract for JSON serialization with runtime validation.
 */
public interface JsonSerializable {

    /**
     * Converts this object to JSON format.
     * Implementations must ensure this method never throws exceptions
     * and produces valid JSON.
     *
     * @return JsonElement representation of this object
     */
    JsonElement toJson();

    /**
     * Converts this object to JSON string format.
     * Default implementation uses toJson().toString().
     *
     * @return JSON string representation of this object
     */
    default String toJsonString() {
        return toJson().toString();
    }

    /**
     * Returns the provider type identifier for this object.
     * This is used during serialization to embed type information
     * and during deserialization to determine the correct concrete class.
     *
     * @return provider type (e.g., "easypost", "esw", "tugboat")
     */
    String getProviderType();
}
