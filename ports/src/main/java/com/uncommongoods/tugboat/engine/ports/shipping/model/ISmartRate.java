package com.uncommongoods.tugboat.engine.ports.shipping.model;

/**
 * Interface for SmartRate operations
 */
public interface ISmartRate extends IEasyPostResource {
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
     * Get the rate.
     *
     * @return the rate
     */
    String getRate();

    /**
     * Get the currency.
     *
     * @return the currency
     */
    String getCurrency();
}
