package com.uncommongoods.tugboat.engine.ports.shipping.service;

import java.util.HashMap;
import java.util.Map;

import com.uncommongoods.tugboat.engine.exception.TugboatException;
import com.easypost.model.Order;
import com.easypost.model.Rate;
import com.easypost.service.EasyPostClient;
import com.easypost.service.OrderService;
import com.uncommongoods.tugboat.engine.ports.shipping.model.OrderAdapter;
import com.uncommongoods.tugboat.engine.ports.shipping.model.RateAdapter;
import com.uncommongoods.tugboat.engine.ports.shipping.model.IOrder;
import com.uncommongoods.tugboat.engine.ports.shipping.model.IRate;

public class OrderServiceAdapter implements IOrderService {
    private final OrderService orderService;

    public OrderServiceAdapter(EasyPostClient client) {
        this.orderService = client.order;
    }

    public OrderServiceAdapter(OrderService orderService) {
        this.orderService = orderService;
    }

    @Override
    public IOrder create(Map<String, Object> params) throws TugboatException {
        try {
            Order order = orderService.create(params);
            return order != null ? new OrderAdapter(order) : null;
        } catch (com.easypost.exception.EasyPostException e) {
            throw new TugboatException(e.getMessage(), e);
        }
    }

    @Override
    public IOrder retrieve(String id) throws TugboatException {
        try {
            Order order = orderService.retrieve(id);
            return order != null ? new OrderAdapter(order) : null;
        } catch (com.easypost.exception.EasyPostException e) {
            throw new TugboatException(e.getMessage(), e);
        }
    }

    @Override
    public IOrder newRates(String id) throws TugboatException {
        try {
            Order order = orderService.newRates(id);
            return order != null ? new OrderAdapter(order) : null;
        } catch (com.easypost.exception.EasyPostException e) {
            throw new TugboatException(e.getMessage(), e);
        }
    }

    @Override
    public IOrder newRates(String id, Map<String, Object> params) throws TugboatException {
        try {
            Order order = orderService.newRates(id, params);
            return order != null ? new OrderAdapter(order) : null;
        } catch (com.easypost.exception.EasyPostException e) {
            throw new TugboatException(e.getMessage(), e);
        }
    }

    @Override
    public IOrder buy(String id, Map<String, Object> params) throws TugboatException {
        try {
            Order order = orderService.buy(id, params);
            return order != null ? new OrderAdapter(order) : null;
        } catch (com.easypost.exception.EasyPostException e) {
            String message = "easypost id: " + id + " - " + e.getMessage();
            throw new TugboatException(message, e);
        }
    }

    @Override
    public IOrder buy(String id, IRate rate) throws TugboatException {
        if (!(rate instanceof RateAdapter)) {
            // If the rate is not from our adapter, create parameters manually
            Map<String, Object> params = new HashMap<>();
            params.put("carrier", rate.getCarrier());
            params.put("service", rate.getService());
            return buy(id, params);
        }

        // Use the underlying Rate object
        Rate concreteRate = ((RateAdapter) rate).getERate();
        try {
            Order order = orderService.buy(id, concreteRate);
            return order != null ? new OrderAdapter(order) : null;
        } catch (com.easypost.exception.EasyPostException e) {
            String message = "easypost id: " + id + " - " + e.getMessage();
            throw new TugboatException(message, e);
        }
    }

    public OrderService getOrderService() {
        return orderService;
    }
}
