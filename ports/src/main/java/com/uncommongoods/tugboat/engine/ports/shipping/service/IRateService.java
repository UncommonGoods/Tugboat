package com.uncommongoods.tugboat.engine.ports.shipping.service;

import com.uncommongoods.tugboat.engine.exception.TugboatException;
import com.uncommongoods.tugboat.engine.ports.shipping.model.IRate;

/**
 * Interface for RateService operations
 */
public interface IRateService {
    /**
     * Retrieve a Rate from the API.
     *
     * @param id ID of the Rate to retrieve.
     * @return Rate object.
     * @throws TugboatException when the request fails.
     */
    IRate retrieve(String id) throws TugboatException;
}
