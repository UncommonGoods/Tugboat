package com.uncommongoods.tugboat.engine.ports.shipping.model;

/**
 * Interface for BatchStatus operations
 */
public interface IBatchStatus {
    /**
     * Get the number of created shipments.
     *
     * @return the number of created shipments
     */
    int getCreated();

    /**
     * Get the number of creation failed shipments.
     *
     * @return the number of creation failed shipments
     */
    int getCreationFailed();

    /**
     * Get the number of postage purchased shipments.
     *
     * @return the number of postage purchased shipments
     */
    int getPostagePurchased();

    /**
     * Get the number of postage purchase failed shipments.
     *
     * @return the number of postage purchase failed shipments
     */
    int getPostagePurchaseFailed();
}
