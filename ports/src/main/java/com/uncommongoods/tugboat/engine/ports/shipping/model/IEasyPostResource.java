package com.uncommongoods.tugboat.engine.ports.shipping.model;

import java.util.Date;


/**
 * Common interface for all EasyPost resources
 */
public interface IEasyPostResource {
    /**
     * Get the ID of the resource.
     *
     * @return the ID of the resource
     */
    String getId();

    /**
     * Get the mode of the resource.
     *
     * @return the mode of the resource
     */
    String getMode();

    /**
     * Get the object type of the resource.
     *
     * @return the object type of the resource
     */
    String getObject();

    /**
     * Get the creation date of the resource.
     *
     * @return the creation date of the resource
     */
    Date getCreatedAt();

    /**
     * Get the last update date of the resource.
     *
     * @return the last update date of the resource
     */
    Date getUpdatedAt();

    /**
     * Returns a string representation of the resource.
     *
     * @return String representation of the resource.
     */
    String toString();

    /**
     * Pretty print the JSON representation of the resource.
     *
     * @return the JSON representation of the resource.
     */
    String prettyPrint();
}
