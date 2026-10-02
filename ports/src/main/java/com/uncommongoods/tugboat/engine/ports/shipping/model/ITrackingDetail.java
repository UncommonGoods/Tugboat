package com.uncommongoods.tugboat.engine.ports.shipping.model;

import java.util.Date;

/**
 * Interface for TrackingDetail operations
 */
public interface ITrackingDetail {
    /**
     * Get the message of the tracking detail.
     *
     * @return the message of the tracking detail
     */
    String getMessage();

    /**
     * Get the description of the tracking detail.
     *
     * @return the description of the tracking detail
     */
    String getDescription();

    /**
     * Get the status of the tracking detail.
     *
     * @return the status of the tracking detail
     */
    String getStatus();

    /**
     * Get the status detail of the tracking detail.
     *
     * @return the status detail of the tracking detail
     */
    String getStatusDetail();

    /**
     * Get the datetime of the tracking detail.
     *
     * @return the datetime of the tracking detail
     */
    Date getDatetime();

    /**
     * Get the source of the tracking detail.
     *
     * @return the source of the tracking detail
     */
    String getSource();

    /**
     * Get the carrier code of the tracking detail.
     *
     * @return the carrier code of the tracking detail
     */
    String getCarrierCode();

    /**
     * Get the tracking location of the tracking detail.
     *
     * @return the tracking location of the tracking detail
     */
    ITrackingLocation getTrackingLocation();

    /**
     * Get the estimated delivery date of the tracking detail.
     *
     * @return the estimated delivery date of the tracking detail
     */
    String getEstDeliveryDate();
}
