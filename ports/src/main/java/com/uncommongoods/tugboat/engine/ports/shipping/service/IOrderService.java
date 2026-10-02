package com.uncommongoods.tugboat.engine.ports.shipping.service;

import com.uncommongoods.tugboat.engine.exception.TugboatException;
import com.uncommongoods.tugboat.engine.ports.shipping.model.IOrder;
import com.uncommongoods.tugboat.engine.ports.shipping.model.IRate;

import java.util.Map;

/**
 * Interface for OrderService operations
 */
public interface IOrderService {
    /**
     * Create an Order object from a map of parameters.
     *
     * @param params Map of parameters.
     * @return Order object.
     * @throws TugboatException when the request fails.
     */
    IOrder create(Map<String, Object> params) throws TugboatException;

    /**
     * Retrieve an Order object from the API.
     *
     * @param id ID of the Order to retrieve.
     * @return Order object.
     * @throws TugboatException when the request fails.
     */
    IOrder retrieve(String id) throws TugboatException;

    /**
     * Get new rates for this Order.
     *
     * @param id The ID of order.
     * @return Order object.
     * @throws TugboatException when the request fails.
     */
    IOrder newRates(String id) throws TugboatException;

    /**
     * Get new rates for this Order.
     *
     * @param id     The ID of order.
     * @param params Map of parameters.
     * @return Order object.
     * @throws TugboatException when the request fails.
     */
    IOrder newRates(String id, Map<String, Object> params) throws TugboatException;

    /**
     * Buy this Order.
     *
     * @param id     The ID of order.
     * @param params Map of parameters.
     * @return Order object.
     * @throws TugboatException when the request fails.
     */
    IOrder buy(String id, Map<String, Object> params) throws TugboatException;

    /**
     * Buy this Order.
     *
     * @param id   The ID of order.
     * @param rate Rate to buy.
     * @return Order object.
     * @throws TugboatException when the request fails.
     */
    IOrder buy(String id, IRate rate) throws TugboatException;
}
