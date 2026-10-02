package com.uncommongoods.tugboat.engine.ports.shipping.service;

import com.uncommongoods.tugboat.engine.exception.TugboatException;
import com.uncommongoods.tugboat.engine.ports.shipping.model.IParcel;

import java.util.Map;

/**
 * Interface for parcel service operations
 */
public interface IParcelService {
    /**
     * Create a new parcel
     *
     * @param params Parcel parameters
     * @return Created parcel
     * @throws TugboatException when the operation fails
     */
    IParcel create(Map<String, Object> params) throws TugboatException;

    /**
     * Retrieve a parcel by ID
     *
     * @param id Parcel ID
     * @return Retrieved parcel
     * @throws TugboatException when the operation fails
     */
    IParcel retrieve(String id) throws TugboatException;
}
