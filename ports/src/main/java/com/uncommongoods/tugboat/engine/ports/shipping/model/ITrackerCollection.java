package com.uncommongoods.tugboat.engine.ports.shipping.model;

import java.util.List;

/**
 * Interface for TrackerCollection operations
 */
public interface ITrackerCollection extends IPaginatedCollection<ITracker> {
    /**
     * Get the trackers in this collection.
     *
     * @return the trackers in this collection
     */
    List<ITracker> getTrackers();

    /**
     * Get the tracking code filter.
     *
     * @return the tracking code filter
     */
    String getTrackingCode();

    /**
     * Set the tracking code filter.
     *
     * @param trackingCode the tracking code filter
     */
    void setTrackingCode(String trackingCode);

    /**
     * Get the tracking codes filter.
     *
     * @return the tracking codes filter
     */
    List<String> getTrackingCodes();

    /**
     * Set the tracking codes filter.
     *
     * @param trackingCodes the tracking codes filter
     */
    void setTrackingCodes(List<String> trackingCodes);

    /**
     * Get the carrier filter.
     *
     * @return the carrier filter
     */
    String getCarrier();

    /**
     * Set the carrier filter.
     *
     * @param carrier the carrier filter
     */
    void setCarrier(String carrier);
}
