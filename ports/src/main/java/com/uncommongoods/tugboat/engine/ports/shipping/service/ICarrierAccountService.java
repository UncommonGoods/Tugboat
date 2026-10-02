package com.uncommongoods.tugboat.engine.ports.shipping.service;

import com.uncommongoods.tugboat.engine.exception.TugboatException;
import com.uncommongoods.tugboat.engine.ports.shipping.model.ICarrierAccount;

import java.util.List;
import java.util.Map;

/**
 * Interface for CarrierAccountService operations
 */
public interface ICarrierAccountService {
    /**
     * Create a carrier account.
     *
     * @param params Map of parameters to create.
     * @return created CarrierAccount object.
     * @throws TugboatException when the request fails.
     */
    ICarrierAccount create(Map<String, Object> params) throws TugboatException;

    /**
     * Retrieve a carrier account from the API.
     *
     * @param id id of the carrier account.
     * @return CarrierAccount object.
     * @throws TugboatException when the request fails.
     */
    ICarrierAccount retrieve(String id) throws TugboatException;

    /**
     * List all carrier accounts.
     *
     * @return List of CarrierAccount objects.
     * @throws TugboatException when the request fails.
     */
    List<ICarrierAccount> all() throws TugboatException;

    /**
     * List all carrier accounts.
     *
     * @param params filters to apply to the list.
     * @return List of CarrierAccount objects.
     * @throws TugboatException when the request fails.
     */
    List<ICarrierAccount> all(Map<String, Object> params) throws TugboatException;

    /**
     * Update this carrier account.
     *
     * @param id     The ID of carrier account
     * @param params parameters to update.
     * @return updated CarrierAccount object.
     * @throws TugboatException when the request fails.
     */
    ICarrierAccount update(String id, Map<String, Object> params) throws TugboatException;

    /**
     * Delete this carrier account.
     *
     * @param id The ID of carrier account.
     * @throws TugboatException when the request fails.
     */
    void delete(String id) throws TugboatException;
}
