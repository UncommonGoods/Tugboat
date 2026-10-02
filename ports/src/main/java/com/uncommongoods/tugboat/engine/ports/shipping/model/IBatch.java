package com.uncommongoods.tugboat.engine.ports.shipping.model;

import java.util.List;

/**
 * Interface for Batch operations
 */
public interface IBatch extends IEasyPostResource {
    /**
     * Get the state of the batch.
     *
     * @return the state of the batch
     */
    String getState();

    /**
     * Get the status of the batch.
     *
     * @return the status of the batch
     */
    IBatchStatus getStatus();

    /**
     * Get the number of shipments in the batch.
     *
     * @return the number of shipments in the batch
     */
    Number getNumShipments();

    /**
     * Get the shipments in the batch.
     *
     * @return the shipments in the batch
     */
    List<IShipment> getShipments();

    /**
     * Get the label URL of the batch.
     *
     * @return the label URL of the batch
     */
    String getLabelUrl();

    /**
     * Get the scan form of the batch.
     *
     * @return the scan form of the batch
     */
    IScanForm getScanForm();

    /**
     * Get the reference of the batch.
     *
     * @return the reference of the batch
     */
    String getReference();
}
