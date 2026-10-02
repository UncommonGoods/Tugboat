package com.uncommongoods.tugboat.engine.ports.shipping.model;

import com.easypost.model.TrackingDetail;
import com.easypost.model.TrackingLocation;

import java.util.Date;

public class TrackingDetailAdapter implements ITrackingDetail {
    private final TrackingDetail trackingDetail;

    public TrackingDetailAdapter(TrackingDetail trackingDetail) {
        this.trackingDetail = trackingDetail;
    }

    @Override
    public String getMessage() {
        return trackingDetail.getMessage();
    }

    @Override
    public String getDescription() {
        return trackingDetail.getDescription();
    }

    @Override
    public String getStatus() {
        return trackingDetail.getStatus();
    }

    @Override
    public String getStatusDetail() {
        return trackingDetail.getStatusDetail();
    }

    @Override
    public Date getDatetime() {
        return trackingDetail.getDatetime();
    }

    @Override
    public String getSource() {
        return trackingDetail.getSource();
    }

    @Override
    public String getCarrierCode() {
        return trackingDetail.getCarrierCode();
    }

    @Override
    public ITrackingLocation getTrackingLocation() {
        TrackingLocation trackingLocation = trackingDetail.getTrackingLocation();
        return trackingLocation != null ? new TrackingLocationAdapter(trackingLocation) : null;
    }

    @Override
    public String getEstDeliveryDate() {
        return trackingDetail.getEstDeliveryDate();
    }

    public TrackingDetail getTrackingDetail() {
        return trackingDetail;
    }
}
