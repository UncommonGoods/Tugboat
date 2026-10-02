package com.uncommongoods.tugboat.engine.ports.shipping.model;

/**
 * Interface for Field operations
 */
public interface IField extends IEasyPostResource {
    /**
     * Get the visibility of the field.
     *
     * @return the visibility of the field
     */
    String getVisibility();

    /**
     * Get the label of the field.
     *
     * @return the label of the field
     */
    String getLabel();

    /**
     * Get the value of the field.
     *
     * @return the value of the field
     */
    String getValue();
}
