package com.uncommongoods.tugboat.engine.ports.shipping.service;

import com.uncommongoods.tugboat.engine.exception.TugboatException;
import com.uncommongoods.tugboat.engine.ports.shipping.model.IBatch;
import com.uncommongoods.tugboat.engine.ports.shipping.model.IBatchCollection;
import com.uncommongoods.tugboat.engine.ports.shipping.model.IShipment;

import java.util.List;
import java.util.Map;

/**
 * Interface for BatchService operations
 */
public interface IBatchService {
    /**
     * Create a Batch object.
     *
     * @return Batch object.
     * @throws TugboatException when the request fails.
     */
    IBatch create() throws TugboatException;

    /**
     * Create a Batch object.
     *
     * @param params Map of parameters.
     * @return Batch object.
     * @throws TugboatException when the request fails.
     */
    IBatch create(Map<String, Object> params) throws TugboatException;

    /**
     * Retrieve a Batch object from the API.
     *
     * @param id ID of the Batch to retrieve.
     * @return Batch object.
     * @throws TugboatException when the request fails.
     */
    IBatch retrieve(String id) throws TugboatException;

    /**
     * List all Batch objects.
     *
     * @param params Map of parameters.
     * @return BatchCollection object.
     * @throws TugboatException when the request fails.
     */
    IBatchCollection all(Map<String, Object> params) throws TugboatException;

    /**
     * Label this Batch object.
     *
     * @param id     The ID of batch.
     * @param params Map of parameters.
     * @return Batch object.
     * @throws TugboatException when the request fails.
     */
    IBatch label(String id, Map<String, Object> params) throws TugboatException;

    /**
     * Add shipments to this Batch object.
     *
     * @param id     The ID of batch.
     * @param params Map of parameters.
     * @return Batch object.
     * @throws TugboatException when the request fails.
     */
    IBatch addShipments(String id, Map<String, Object> params) throws TugboatException;

    /**
     * Add shipments to this Batch object.
     *
     * @param id        The ID of batch.
     * @param shipments List of Shipment objects.
     * @return Batch object.
     * @throws TugboatException when the request fails.
     */
    IBatch addShipments(String id, List<IShipment> shipments) throws TugboatException;

    /**
     * Remove shipments from this Batch object.
     *
     * @param id     The ID of batch.
     * @param params Map of parameters.
     * @return Batch object.
     * @throws TugboatException when the request fails.
     */
    IBatch removeShipments(String id, Map<String, Object> params) throws TugboatException;

    /**
     * Remove shipments from this Batch object.
     *
     * @param id        The ID of batch.
     * @param shipments List of Shipment objects.
     * @return Batch object.
     * @throws TugboatException when the request fails.
     */
    IBatch removeShipments(String id, List<IShipment> shipments) throws TugboatException;

    /**
     * Buy this batch.
     *
     * @param id The ID of batch.
     * @return Batch object.
     * @throws TugboatException when the request fails.
     */
    IBatch buy(String id) throws TugboatException;

    /**
     * Buy this batch.
     *
     * @param id     The ID of batch.
     * @param params Map of parameters.
     * @return Batch object.
     * @throws TugboatException when the request fails.
     */
    IBatch buy(String id, Map<String, Object> params) throws TugboatException;

    /**
     * Create a scan form for this batch.
     *
     * @param id The ID of batch.
     * @return Batch object.
     * @throws TugboatException when the request fails.
     */
    IBatch createScanForm(String id) throws TugboatException;

    /**
     * Create a scan form for this batch.
     *
     * @param id     The ID of batch.
     * @param params Map of parameters.
     * @return Batch object.
     * @throws TugboatException when the request fails.
     */
    IBatch createScanForm(String id, Map<String, Object> params) throws TugboatException;
}
