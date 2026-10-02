package com.uncommongoods.tugboat.engine.ports.shipping.service;

import com.uncommongoods.tugboat.engine.exception.TugboatException;
import com.uncommongoods.tugboat.engine.exception.EndOfPaginationException;
import com.uncommongoods.tugboat.engine.ports.shipping.model.IScanForm;
import com.uncommongoods.tugboat.engine.ports.shipping.model.IScanFormCollection;

import java.util.Map;

/**
 * Interface for ScanformService operations
 */
public interface IScanFormService {
    /**
     * Create a ScanForm from a map of parameters.
     *
     * @param params the map of parameters.
     * @return ScanForm object.
     * @throws TugboatException when the request fails.
     */
    IScanForm create(Map<String, Object> params) throws TugboatException;

    /**
     * Retrieve a ScanForm from the API.
     *
     * @param id the id of the ScanForm to retrieve.
     * @return ScanForm object.
     * @throws TugboatException when the request fails.
     */
    IScanForm retrieve(String id) throws TugboatException;

    /**
     * Get a list of ScanForms from the API.
     *
     * @param params the parameters to send to the API.
     * @return ScanFormCollection object.
     * @throws TugboatException when the request fails.
     */
    IScanFormCollection all(Map<String, Object> params) throws TugboatException;

    /**
     * Get the next page of an ScanFormCollection.
     *
     * @param collection ScanFormCollection to get next page of.
     * @return ScanFormCollection object.
     * @throws EndOfPaginationException when there are no more pages to retrieve.
     */
    IScanFormCollection getNextPage(IScanFormCollection collection) throws EndOfPaginationException;

    /**
     * Get the next page of an ScanFormCollection.
     *
     * @param collection ScanFormCollection to get next page of.
     * @param pageSize   The number of results to return on the next page.
     * @return ScanFormCollection object.
     * @throws EndOfPaginationException when there are no more pages to retrieve.
     */
    IScanFormCollection getNextPage(IScanFormCollection collection, Integer pageSize) throws EndOfPaginationException;
}
