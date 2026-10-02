package com.uncommongoods.tugboat.engine.ports.shipping.model;

import java.util.Map;

/**
 * Interface that ensures implementing classes can be converted to a Map representation.
 * This provides a functional contract for Map serialization with runtime validation.
 */
public interface Mappable {

    /**
     * Converts this object to Map format.
     * Implementations must ensure this method never throws exceptions
     * and produces a valid Map representation.
     *
     * @return Map representation of this object
     */
    Map<String, Object> toMap();
}
