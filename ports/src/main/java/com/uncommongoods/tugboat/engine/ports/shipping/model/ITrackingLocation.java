package com.uncommongoods.tugboat.engine.ports.shipping.model;

/**
 * Interface for TrackingLocation operations
 */
public interface ITrackingLocation {
    /**
     * Get the city of the tracking location.
     *
     * @return the city of the tracking location
     */
    String getCity();

    /**
     * Get the state of the tracking location.
     *
     * @return the state of the tracking location
     */
    String getState();

    /**
     * Get the country of the tracking location.
     *
     * @return the country of the tracking location
     */
    String getCountry();

    /**
     * Get the zip of the tracking location.
     *
     * @return the zip of the tracking location
     */
    String getZip();
}
