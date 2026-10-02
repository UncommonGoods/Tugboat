package com.uncommongoods.tugboat.engine.ports.shipping.model;

import java.util.Date;
import java.util.List;

/**
 * Interface for Tracker operations
 */
public interface ITracker extends IEasyPostResource {
    /**
     * Get the tracking code of the tracker.
     *
     * @return the tracking code of the tracker
     */
    String getTrackingCode();

    /**
     * Get the status of the tracker.
     *
     * @return the status of the tracker
     */
    String getStatus();

    /**
     * Get the shipment ID of the tracker.
     *
     * @return the shipment ID of the tracker
     */
    String getShipmentId();

    /**
     * Get the carrier of the tracker.
     *
     * @return the carrier of the tracker
     */
    String getCarrier();

    /**
     * Get the tracking details of the tracker.
     *
     * @return the tracking details of the tracker
     */
    List<ITrackingDetail> getTrackingDetails();

    /**
     * Get the weight of the package.
     *
     * @return the weight of the package
     */
    float getWeight();

    /**
     * Get the estimated delivery date of the tracker.
     *
     * @return the estimated delivery date of the tracker
     */
    Date getEstDeliveryDate();

    /**
     * Get the name of who signed for the package.
     *
     * @return the name of who signed for the package
     */
    String getSignedBy();

    /**
     * Get the carrier detail of the tracker.
     *
     * @return the carrier detail of the tracker
     */
    ICarrierDetail getCarrierDetail();

    /**
     * Get the public URL of the tracker.
     *
     * @return the public URL of the tracker
     */
    String getPublicUrl();

    /**
     * Get the status detail of the tracker.
     *
     * @return the status detail of the tracker
     */
    String getStatusDetail();

    /**
     * Check if the tracker is finalized.
     *
     * @return true if the tracker is finalized, otherwise false
     */
    Boolean getFinalized();

    /**
     * Check if the tracker is for a return.
     *
     * @return true if the tracker is for a return, otherwise false
     */
    Boolean getIsReturn();
}
