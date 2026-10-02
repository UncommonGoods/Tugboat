package com.uncommongoods.tugboat.engine.ports.shipping.model;

/**
 * Interface for EstimatedDeliveryDate operations
 */
public interface IEstimatedDeliveryDate {
    /**
     * Get the rate ID.
     *
     * @return the rate ID
     */
    String getRateId();

    /**
     * Get the service.
     *
     * @return the service
     */
    String getService();

    /**
     * Get the carrier.
     *
     * @return the carrier
     */
    String getCarrier();

    /**
     * Get the carrier account ID.
     *
     * @return the carrier account ID
     */
    String getCarrierAccountId();

    /**
     * Get the estimated delivery date.
     *
     * @return the estimated delivery date
     */
    String getEdd();

    /**
     * Get the on-time performance.
     *
     * @return the on-time performance
     */
    Double getOnTimePerformance();

    /**
     * Get the delivery days.
     *
     * @return the delivery days
     */
    Integer getDeliveryDays();

    /**
     * Get the delivery date guaranteed.
     *
     * @return true if delivery date is guaranteed, false otherwise
     */
    Boolean getDeliveryDateGuaranteed();
}
