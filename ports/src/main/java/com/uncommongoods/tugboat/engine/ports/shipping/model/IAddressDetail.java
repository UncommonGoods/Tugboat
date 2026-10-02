package com.uncommongoods.tugboat.engine.ports.shipping.model;

/**
 * Interface for AddressDetail operations
 */
public interface IAddressDetail {
    /**
     * Get the latitude of the address detail.
     *
     * @return the latitude of the address detail
     */
    Float getLatitude();

    /**
     * Get the longitude of the address detail.
     *
     * @return the longitude of the address detail
     */
    Float getLongitude();

    /**
     * Get the time zone of the address detail.
     *
     * @return the time zone of the address detail
     */
    String getTimeZone();
}
