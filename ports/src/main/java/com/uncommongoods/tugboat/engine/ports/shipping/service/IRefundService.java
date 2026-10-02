package com.uncommongoods.tugboat.engine.ports.shipping.service;

import java.util.List;
import java.util.Map;

import com.uncommongoods.tugboat.engine.exception.TugboatException;
import com.uncommongoods.tugboat.engine.exception.EndOfPaginationException;
import com.uncommongoods.tugboat.engine.ports.shipping.model.IRefund;
import com.uncommongoods.tugboat.engine.ports.shipping.model.IRefundCollection;


/**
 * Interface for RefundService operations
 */
public interface IRefundService {
    /**
     * Create a Refund object from a map of parameters.
     *
     * @param params Map of parameters
     * @return Refund object
     * @throws TugboatException when the request fails.
     */
    List<IRefund> create(Map<String, Object> params) throws TugboatException;

    /**
     * Retrieve a Refund object from the API.
     *
     * @param id ID of refund to retrieve
     * @return Refund object
     * @throws TugboatException when the request fails.
     */
    IRefund retrieve(String id) throws TugboatException;

    /**
     * List all Refunds objects.
     *
     * @param params Map of parameters
     * @return RefundCollection object
     * @throws TugboatException when the request fails.
     */
    IRefundCollection all(Map<String, Object> params) throws TugboatException;

    /**
     * Get the next page of an RefundCollection.
     *
     * @param collection RefundCollection to get next page of.
     * @return RefundCollection object.
     * @throws EndOfPaginationException when there are no more pages to retrieve.
     */
    IRefundCollection getNextPage(IRefundCollection collection) throws EndOfPaginationException;

    /**
     * Get the next page of an RefundCollection.
     *
     * @param collection RefundCollection to get next page of.
     * @param pageSize   The number of results to return on the next page.
     * @return RefundCollection object.
     * @throws EndOfPaginationException when there are no more pages to retrieve.
     */
    IRefundCollection getNextPage(IRefundCollection collection, Integer pageSize) throws EndOfPaginationException;
}
