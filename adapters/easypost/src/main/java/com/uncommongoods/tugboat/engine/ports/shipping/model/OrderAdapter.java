package com.uncommongoods.tugboat.engine.ports.shipping.model;

import java.util.*;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.uncommongoods.tugboat.engine.exception.TugboatException;
import com.easypost.model.*;

public class OrderAdapter implements IOrder {
    private final Order order;

    public OrderAdapter(Order order) {
        this.order = order;
    }

    public OrderAdapter(Map<String, Object> orderMap) {
        // Create a new Order by deserializing from the map
        Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
            .create();
        String json = gson.toJson(orderMap);
        this.order = gson.fromJson(json, Order.class);
    }

    @Override
    public String getService() {
        return order.getService();
    }

    @Override
    public String getReference() {
        return order.getReference();
    }

    @Override
    public Boolean getIsReturn() {
        return order.getIsReturn();
    }

    @Override
    public IAddress getToAddress() {
        Address address = order.getToAddress();
        return address != null ? new AddressAdapter(address) : null;
    }

    @Override
    public IAddress getBuyerAddress() {
        Address address = order.getBuyerAddress();
        return address != null ? new AddressAdapter(address) : null;
    }

    @Override
    public IAddress getFromAddress() {
        Address address = order.getFromAddress();
        return address != null ? new AddressAdapter(address) : null;
    }

    @Override
    public IAddress getReturnAddress() {
        Address address = order.getReturnAddress();
        return address != null ? new AddressAdapter(address) : null;
    }

    @Override
    public ICustomsInfo getCustomsInfo() {
        CustomsInfo customsInfo = order.getCustomsInfo();
        return customsInfo != null ? new CustomsInfoAdapter(customsInfo) : null;
    }

    @Override
    public List<IShipment> getShipments() {
        List<Shipment> shipments = order.getShipments();
        if (shipments == null) {
            return null;
        }

        return shipments.stream()
            .map(ShipmentAdapter::new)
            .collect(Collectors.toList());
    }

    @Override
    public List<IRate> getRates() {
        List<Rate> rates = order.getRates();
        if (rates == null) {
            return null;
        }

        return rates.stream()
            .map(RateAdapter::new)
            .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getOptions() {
        return Map.of();
    }

    @Override
    public List<IShipmentMessage> getMessages() {
        List<ShipmentMessage> messages = order.getMessages();
        if (messages == null) {
            return null;
        }

        return messages.stream()
            .map(ShipmentMessageAdapter::new)
            .collect(Collectors.toList());
    }

    @Override
    public List<ICarrierAccount> getCarrierAccounts() {
        return List.of();
    }

    @Override
    public IRate lowestRate() throws TugboatException {
        try {
            Rate rate = order.lowestRate();
            return rate != null ? new RateAdapter(rate) : null;
        } catch (com.easypost.exception.EasyPostException e) {
            throw new TugboatException(e.getMessage(), e);
        }
    }

    @Override
    public IRate lowestRate(List<String> carriers, List<String> services) throws TugboatException {
        try {
            Rate rate = order.lowestRate(carriers, services);
            return rate != null ? new RateAdapter(rate) : null;
        } catch (com.easypost.exception.EasyPostException e) {
            throw new TugboatException(e.getMessage(), e);
        }
    }

    @Override
    public IRate lowestRate(List<String> carriers) throws TugboatException {
        try {
            Rate rate = order.lowestRate(carriers);
            return rate != null ? new RateAdapter(rate) : null;
        } catch (com.easypost.exception.EasyPostException e) {
            throw new TugboatException(e.getMessage(), e);
        }
    }

    @Override
    public String getId() {
        return order.getId();
    }

    @Override
    public String getMode() {
        return order.getMode();
    }

    @Override
    public String getObject() {
        return order.getObject();
    }

    @Override
    public Date getCreatedAt() {
        return order.getCreatedAt();
    }

    @Override
    public Date getUpdatedAt() {
        return order.getUpdatedAt();
    }

    @Override
    public String toString() {
        return order.toString();
    }

    @Override
    public String prettyPrint() {
        return order.prettyPrint();
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        if (getId() != null) map.put("id", getId());
        if (getReference() != null) map.put("reference", getReference());
        if (getIsReturn() != null) map.put("is_return", getIsReturn());
        if (getService() != null) map.put("service", getService());

        // Add nested address objects
        IAddress fromAddress = getFromAddress();
        if (fromAddress != null && fromAddress instanceof Mappable) {
            map.put("from_address", ((Mappable) fromAddress).toMap());
        }

        IAddress toAddress = getToAddress();
        if (toAddress != null && toAddress instanceof Mappable) {
            map.put("to_address", ((Mappable) toAddress).toMap());
        }

        IAddress returnAddress = getReturnAddress();
        if (returnAddress != null && returnAddress instanceof Mappable) {
            map.put("return_address", ((Mappable) returnAddress).toMap());
        }

        IAddress buyerAddress = getBuyerAddress();
        if (buyerAddress != null && buyerAddress instanceof Mappable) {
            map.put("buyer_address", ((Mappable) buyerAddress).toMap());
        }

        ICustomsInfo customsInfo = getCustomsInfo();
        if (customsInfo != null && customsInfo instanceof Mappable) {
            map.put("customs_info", ((Mappable) customsInfo).toMap());
        }

        // Add shipments list
        List<IShipment> shipments = getShipments();
        if (shipments != null) {
            List<Map<String, Object>> shipmentMaps = new ArrayList<>();
            for (IShipment shipment : shipments) {
                if (shipment instanceof Mappable) {
                    shipmentMaps.add(((Mappable) shipment).toMap());
                }
            }
            map.put("shipments", shipmentMaps);
        }

        // Add rates list
        List<IRate> rates = getRates();
        if (rates != null) {
            List<Map<String, Object>> rateMaps = new ArrayList<>();
            for (IRate rate : rates) {
                if (rate instanceof Mappable) {
                    rateMaps.add(((Mappable) rate).toMap());
                }
            }
            map.put("rates", rateMaps);
        }

        if (getOptions() != null) map.put("options", getOptions());

        return map;
    }

    /**
     * Get the underlying Order object.
     *
     * @return the underlying Order object
     */
    public Order getOrder() {
        return order;
    }

    @Override
    public JsonElement toJson() {
        Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
            .create();
        return gson.toJsonTree(this.order);
    }

    @Override
    public String toJsonString() {
        return this.toJson().toString();
    }

    @Override
    public String getProviderType() {
        return "easypost";
    }
}
