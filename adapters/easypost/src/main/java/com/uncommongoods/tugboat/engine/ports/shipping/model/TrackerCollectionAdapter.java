package com.uncommongoods.tugboat.engine.ports.shipping.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.uncommongoods.tugboat.engine.exception.EndOfPaginationException;
import com.easypost.model.Tracker;
import com.easypost.model.TrackerCollection;

public class TrackerCollectionAdapter extends PaginatedCollectionAdapter<ITracker, Tracker, TrackerCollection> implements ITrackerCollection {

    public TrackerCollectionAdapter(TrackerCollection collection) {
        super(collection);
    }

    @Override
    public List<ITracker> getTrackers() {
        List<Tracker> trackers = collection.getTrackers();
        if (trackers == null) {
            return null;
        }

        List<ITracker> adaptedTrackers = new ArrayList<>();
        for (Tracker tracker : trackers) {
            adaptedTrackers.add(new TrackerAdapter(tracker));
        }

        return adaptedTrackers;
    }

    @Override
    public String getTrackingCode() {
        return collection.getTrackingCode();
    }

    @Override
    public void setTrackingCode(String trackingCode) {
        collection.setTrackingCode(trackingCode);
    }

    @Override
    public List<String> getTrackingCodes() {
        return collection.getTrackingCodes();
    }

    @Override
    public void setTrackingCodes(List<String> trackingCodes) {
        collection.setTrackingCodes(trackingCodes);
    }

    @Override
    public String getCarrier() {
        return collection.getCarrier();
    }

    @Override
    public void setCarrier(String carrier) {
        collection.setCarrier(carrier);
    }

    @Override
    public Map<String, Object> buildNextPageParameters(List<ITracker> entries, Integer pageSize) throws EndOfPaginationException {
        if (entries == null || entries.isEmpty()) {
            throw new EndOfPaginationException("No current entries to paginate from");
        }

        String lastId = entries.get(entries.size() - 1).getId();

        Map<String, Object> parameters = new java.util.HashMap<>();
        parameters.put("before_id", lastId);

        if (pageSize != null) {
            parameters.put("page_size", pageSize);
        }

        if (getTrackingCode() != null) {
            parameters.put("tracking_code", getTrackingCode());
        }

        if (getTrackingCodes() != null) {
            parameters.put("tracking_codes", getTrackingCodes());
        }

        if (getCarrier() != null) {
            parameters.put("carrier", getCarrier());
        }

        return parameters;
    }

    @Override
    protected List<Tracker> convertInterfaceToConcreteList(List<ITracker> interfaceEntries) {
        if (interfaceEntries == null) {
            return null;
        }

        List<Tracker> concreteEntries = new ArrayList<>();
        for (ITracker item : interfaceEntries) {
            if (item instanceof TrackerAdapter) {
                concreteEntries.add(((TrackerAdapter) item).getTracker());
            } else {
                throw new IllegalArgumentException("Tracker must be a TrackerAdapter");
            }
        }

        return concreteEntries;
    }
}
