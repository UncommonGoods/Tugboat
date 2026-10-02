package com.uncommongoods.tugboat.engine.ports.shipping.service;

import java.util.Map;

import com.easypost.exception.General.EndOfPaginationError;
import com.uncommongoods.tugboat.engine.exception.TugboatException;
import com.uncommongoods.tugboat.engine.exception.EndOfPaginationException;
import com.easypost.model.Tracker;
import com.easypost.model.TrackerCollection;
import com.easypost.service.EasyPostClient;
import com.easypost.service.TrackerService;
import com.uncommongoods.tugboat.engine.ports.shipping.model.TrackerAdapter;
import com.uncommongoods.tugboat.engine.ports.shipping.model.TrackerCollectionAdapter;
import com.uncommongoods.tugboat.engine.ports.shipping.model.ITracker;
import com.uncommongoods.tugboat.engine.ports.shipping.model.ITrackerCollection;

public class TrackerServiceAdapter implements ITrackerService {
    private final TrackerService trackerService;

    public TrackerServiceAdapter(EasyPostClient client) {
        this.trackerService = client.tracker;
    }

    public TrackerServiceAdapter(TrackerService trackerService) {
        this.trackerService = trackerService;
    }

    @Override
    public ITracker create(Map<String, Object> params) throws TugboatException {
        try {
            Tracker tracker = trackerService.create(params);
            return tracker != null ? new TrackerAdapter(tracker) : null;
        } catch (com.easypost.exception.EasyPostException e) {
            throw new TugboatException(e.getMessage(), e);
        }
    }

    @Override
    public ITracker retrieve(String id) throws TugboatException {
        try {
            Tracker tracker = trackerService.retrieve(id);
            return tracker != null ? new TrackerAdapter(tracker) : null;
        } catch (com.easypost.exception.EasyPostException e) {
            throw new TugboatException(e.getMessage(), e);
        }
    }

    @Override
    public ITrackerCollection all(Map<String, Object> params) throws TugboatException {
        try {
            TrackerCollection trackerCollection = trackerService.all(params);
            return trackerCollection != null ? new TrackerCollectionAdapter(trackerCollection) : null;
        } catch (com.easypost.exception.EasyPostException e) {
            throw new TugboatException(e.getMessage(), e);
        }
    }

    @Override
    public ITrackerCollection getNextPage(ITrackerCollection collection) throws EndOfPaginationException {
        if (!(collection instanceof TrackerCollectionAdapter)) {
            throw new IllegalArgumentException("Collection must be an TrackerCollectionAdapter");
        }

        TrackerCollection addressCollection = ((TrackerCollectionAdapter) collection).getCollection();
        try {
            TrackerCollection nextPage = trackerService.getNextPage(addressCollection);
            return nextPage != null ? new TrackerCollectionAdapter(nextPage) : null;
        } catch (EndOfPaginationError e) {
            throw new EndOfPaginationException(e.getMessage(), e);
        }
    }

    @Override
    public ITrackerCollection getNextPage(ITrackerCollection collection, Integer pageSize) throws EndOfPaginationException {
        if (!(collection instanceof TrackerCollectionAdapter)) {
            throw new IllegalArgumentException("Collection must be an TrackerCollectionAdapter");
        }

        TrackerCollection addressCollection = ((TrackerCollectionAdapter) collection).getCollection();
        try {
            TrackerCollection nextPage = trackerService.getNextPage(addressCollection, pageSize);
            return nextPage != null ? new TrackerCollectionAdapter(nextPage) : null;
        } catch (EndOfPaginationError e) {
            throw new EndOfPaginationException(e.getMessage(), e);
        }
    }

    public TrackerService getTrackerService() {
        return trackerService;
    }
}
