package com.uncommongoods.tugboat.engine.ports.shipping.model;

/**
 * Interface for RecommendShipDateForShipmentResult operations
 */
public interface IRecommendShipDateForShipmentResult {
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
     * Get the ship date.
     *
     * @return the ship date
     */
    String getShipDate();

    /**
     * Get the delivery date.
     *
     * @return the delivery date
     */
    String getDeliveryDate();

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
