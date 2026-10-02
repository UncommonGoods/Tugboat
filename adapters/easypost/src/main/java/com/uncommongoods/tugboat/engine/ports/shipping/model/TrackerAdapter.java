package com.uncommongoods.tugboat.engine.ports.shipping.model;

import com.easypost.model.CarrierDetail;
import com.easypost.model.Tracker;
import com.easypost.model.TrackingDetail;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


public class TrackerAdapter implements ITracker {
    private final Tracker tracker;


    public TrackerAdapter(Tracker tracker) {
        super();
        this.tracker = tracker;
    }

    @Override
    public String getTrackingCode() {
        return tracker.getTrackingCode();
    }

    @Override
    public String getStatus() {
        return tracker.getStatus();
    }

    @Override
    public String getShipmentId() {
        return tracker.getShipmentId();
    }

    @Override
    public String getCarrier() {
        return tracker.getCarrier();
    }

    @Override
    public List<ITrackingDetail> getTrackingDetails() {
        List<TrackingDetail> trackingDetails = tracker.getTrackingDetails();
        if (trackingDetails == null) {
            return null;
        }
        return trackingDetails.stream()
            .map(TrackingDetailAdapter::new)
            .collect(Collectors.toList());
    }

    @Override
    public float getWeight() {
        return tracker.getWeight();
    }

    @Override
    public Date getEstDeliveryDate() {
        return tracker.getEstDeliveryDate();
    }

    @Override
    public String getSignedBy() {
        return tracker.getSignedBy();
    }

    @Override
    public ICarrierDetail getCarrierDetail() {
        CarrierDetail carrierDetail = tracker.getCarrierDetail();
        return carrierDetail != null ? new CarrierDetailAdapter(carrierDetail) : null;
    }

    @Override
    public String getPublicUrl() {
        return tracker.getPublicUrl();
    }

    @Override
    public String getStatusDetail() {
        return tracker.getStatusDetail();
    }

    @Override
    public Boolean getFinalized() {
        return tracker.getFinalized();
    }

    @Override
    public Boolean getIsReturn() {
        return tracker.getIsReturn();
    }

    public Tracker getTracker() {
        return tracker;
    }

    @Override
    public String getId() {
        return tracker.getId();
    }

    @Override
    public String getMode() {
        return tracker.getMode();
    }

    @Override
    public String getObject() {
        return tracker.getObject();
    }

    @Override
    public Date getCreatedAt() {
        return tracker.getCreatedAt();
    }

    @Override
    public Date getUpdatedAt() {
        return tracker.getUpdatedAt();
    }

    @Override
    public String prettyPrint() {
        return "";
    }
}
