package com.uncommongoods.tugboat.engine.ports.shipping.model;

/**
 * Interface for Refund operations
 */
public interface IRefund extends IEasyPostResource {
    /**
     * Get the tracking code of the refund.
     *
     * @return the tracking code of the refund
     */
    String getTrackingCode();

    /**
     * Get the confirmation number of the refund.
     *
     * @return the confirmation number of the refund
     */
    String getConfirmationNumber();

    /**
     * Get the status of the refund.
     *
     * @return the status of the refund
     */
    String getStatus();

    /**
     * Get the carrier of the refund.
     *
     * @return the carrier of the refund
     */
    String getCarrier();

    /**
     * Get the shipment ID of the refund.
     *
     * @return the shipment ID of the refund
     */
    String getShipmentId();
}

