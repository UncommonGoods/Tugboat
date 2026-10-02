package com.uncommongoods.tugboat.engine.ports.shipping.service;

import com.uncommongoods.tugboat.engine.exception.TugboatException;
import com.uncommongoods.tugboat.engine.exception.EndOfPaginationException;
import com.uncommongoods.tugboat.engine.ports.shipping.model.ITracker;
import com.uncommongoods.tugboat.engine.ports.shipping.model.ITrackerCollection;

import java.util.Map;

/**
 * Interface for TrackerService operations
 */
public interface ITrackerService {
    /**
     * Create a new Tracker object using a map of parameters.
     *
     * @param params Map of parameters used to create the Tracker.
     * @return Tracker object.
     * @throws TugboatException when the request fails.
     */
    ITracker create(Map<String, Object> params) throws TugboatException;

    /**
     * Retrieve a Tracker object from the API.
     *
     * @param id ID of the Tracker to retrieve.
     * @return Tracker object.
     * @throws TugboatException when the request fails.
     */
    ITracker retrieve(String id) throws TugboatException;

    /**
     * Get a list of all Tracker objects.
     *
     * @param params Map of parameters used to filter the list of Trackers.
     * @return TrackerCollection object.
     * @throws TugboatException when the request fails.
     */
    ITrackerCollection all(Map<String, Object> params) throws TugboatException;

    /**
     * Get the next page of an TrackerCollection.
     *
     * @param collection TrackerCollection to get next page of.
     * @return TrackerCollection object.
     * @throws EndOfPaginationException when there are no more pages to retrieve.
     */
    ITrackerCollection getNextPage(ITrackerCollection collection) throws EndOfPaginationException;

    /**
     * Get the next page of an TrackerCollection.
     *
     * @param collection TrackerCollection to get next page of.
     * @param pageSize   The number of results to return on the next page.
     * @return TrackerCollection object.
     * @throws EndOfPaginationException when there are no more pages to retrieve.
     */
    ITrackerCollection getNextPage(ITrackerCollection collection, Integer pageSize) throws EndOfPaginationException;
}
