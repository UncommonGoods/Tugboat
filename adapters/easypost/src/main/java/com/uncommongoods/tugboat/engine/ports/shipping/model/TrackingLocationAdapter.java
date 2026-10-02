package com.uncommongoods.tugboat.engine.ports.shipping.model;

import com.easypost.model.TrackingLocation;

public class TrackingLocationAdapter implements ITrackingLocation {
    private final TrackingLocation trackingLocation;

    public TrackingLocationAdapter(TrackingLocation trackingLocation) {
        this.trackingLocation = trackingLocation;
    }

    @Override
    public String getCity() {
        return trackingLocation.getCity();
    }

    @Override
    public String getState() {
        return trackingLocation.getState();
    }

    @Override
    public String getCountry() {
        return trackingLocation.getCountry();
    }

    @Override
    public String getZip() {
        return trackingLocation.getZip();
    }

    public TrackingLocation getTrackingLocation() {
        return trackingLocation;
    }
}
