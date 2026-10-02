package com.uncommongoods.tugboat.engine.ports.shipping.service;

import com.uncommongoods.tugboat.engine.exception.TugboatException;
import com.uncommongoods.tugboat.engine.exception.EndOfPaginationException;
import com.uncommongoods.tugboat.engine.ports.shipping.model.IAddress;
import com.uncommongoods.tugboat.engine.ports.shipping.model.IAddressCollection;

import java.util.Map;

/**
 * Interface for AddressService operations
 */
public interface IAddressService {
    /**
     * Create Address object from parameter map.
     *
     * @param params Map of address parameters.
     * @return Address object.
     * @throws TugboatException when the request fails.
     */
    IAddress create(Map<String, Object> params) throws TugboatException;

    /**
     * Retrieve Address object from API.
     *
     * @param id ID of address to retrieve.
     * @return Address object.
     * @throws TugboatException when the request fails.
     */
    IAddress retrieve(String id) throws TugboatException;

    /**
     * List all Address objects.
     *
     * @param params Map of parameters.
     * @return AddressCollection object.
     * @throws TugboatException when the request fails.
     */
    IAddressCollection all(Map<String, Object> params) throws TugboatException;

    /**
     * Get the next page of an AddressCollection.
     *
     * @param collection AddressCollection to get next page of.
     * @return AddressCollection object.
     * @throws EndOfPaginationException when there are no more pages to retrieve.
     */
    IAddressCollection getNextPage(IAddressCollection collection) throws EndOfPaginationException;

    /**
     * Get the next page of an AddressCollection.
     *
     * @param collection AddressCollection to get next page of.
     * @param pageSize   The number of results to return on the next page.
     * @return AddressCollection object.
     * @throws EndOfPaginationException when there are no more pages to retrieve.
     */
    IAddressCollection getNextPage(IAddressCollection collection, Integer pageSize) throws EndOfPaginationException;

    /**
     * Create Address object from parameter map and immediately verify it.
     *
     * @param params Map of address parameters.
     * @return Address object.
     * @throws TugboatException when the request fails.
     */
    IAddress createAndVerify(Map<String, Object> params) throws TugboatException;

    /**
     * Verify this Address object.
     *
     * @param id The ID of address.
     * @return Address object.
     * @throws TugboatException when the request fails.
     */
    IAddress verify(String id) throws TugboatException;
}
