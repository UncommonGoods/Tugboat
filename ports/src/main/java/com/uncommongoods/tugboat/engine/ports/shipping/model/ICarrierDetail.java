package com.uncommongoods.tugboat.engine.ports.shipping.model;

/**
 * Interface for CarrierDetail operations
 */
public interface ICarrierDetail {
    /**
     * Get the service of the carrier detail.
     *
     * @return the service of the carrier detail
     */
    String getService();

    /**
     * Get the container type of the carrier detail.
     *
     * @return the container type of the carrier detail
     */
    String getContainerType();

    /**
     * Get the estimated delivery date local of the carrier detail.
     *
     * @return the estimated delivery date local of the carrier detail
     */
    String getEstDeliveryDateLocal();

    /**
     * Get the estimated delivery time local of the carrier detail.
     *
     * @return the estimated delivery time local of the carrier detail
     */
    String getEstDeliveryTimeLocal();

    /**
     * Get the origin location of the carrier detail.
     *
     * @return the origin location of the carrier detail
     */
    String getOriginLocation();

    /**
     * Get the destination location of the carrier detail.
     *
     * @return the destination location of the carrier detail
     */
    String getDestinationLocation();

    /**
     * Get the guaranteed delivery date of the carrier detail.
     *
     * @return the guaranteed delivery date of the carrier detail
     */
    String getGuaranteedDeliveryDate();

    /**
     * Get the alternate identifier of the carrier detail.
     *
     * @return the alternate identifier of the carrier detail
     */
    String getAlternateIdentifier();

    /**
     * Get the initial delivery attempt of the carrier detail.
     *
     * @return the initial delivery attempt of the carrier detail
     */
    String getInitialDeliveryAttempt();
}
