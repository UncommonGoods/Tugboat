package com.uncommongoods.tugboat.engine.ports.shipping.model;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.easypost.model.Rate;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;


public class RateAdapter implements IRate, Mappable {
    Gson gson = new GsonBuilder()
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
        .create();
    private Rate rate;

    public RateAdapter(Rate rate) {
        this.rate = rate;
    }

    public RateAdapter(Map<String, Object> rateMap) {
        String json = gson.toJson(rateMap);
        this.rate = gson.fromJson(json, Rate.class);
    }

    @Override
    public String getCarrier() {
        return rate.getCarrier();
    }

    @Override
    public void setCarrier(String carrier) {
        Map<String, Object> rateMap = gson.fromJson(this.toJsonString(), Map.class);
        rateMap.put("carrier", carrier);
        String json = gson.toJson(rateMap);
        this.rate = gson.fromJson(json, Rate.class);
    }

    @Override
    public String getService() {
        return rate.getService();
    }

    @Override
    public void setService(String service) {
        Map<String, Object> rateMap = gson.fromJson(this.toJsonString(), Map.class);
        rateMap.put("service", service);
        String json = gson.toJson(rateMap);
        this.rate = gson.fromJson(json, Rate.class);
    }

    @Override
    public Float getRate() {
        return rate.getRate();
    }

    @Override
    public void setRate(Float rate) {
        Map<String, Object> rateMap = gson.fromJson(this.toJsonString(), Map.class);
        rateMap.put("rate", rate);
        String json = gson.toJson(rateMap);
        this.rate = gson.fromJson(json, Rate.class);
    }

    @Override
    public String getCurrency() {
        return rate.getCurrency();
    }

    @Override
    public Float getListRate() {
        return rate.getListRate();
    }

    @Override
    public String getListCurrency() {
        return rate.getListCurrency();
    }

    @Override
    public Float getRetailRate() {
        return rate.getRetailRate();
    }

    @Override
    public String getRetailCurrency() {
        return rate.getRetailCurrency();
    }

    @Override
    public Number getDeliveryDays() {
        return rate.getDeliveryDays();
    }

    @Override
    public void setDeliveryDays(Number deliveryDays) {
        Map<String, Object> rateMap = gson.fromJson(this.toJsonString(), Map.class);
        rateMap.put("deliveryDays", deliveryDays);
        String json = gson.toJson(rateMap);
        this.rate = gson.fromJson(json, Rate.class);
    }

    @Override
    public String getDeliveryDate() {
        return rate.getDeliveryDate();
    }

    @Override
    public void setDeliveryDate(String deliveryDate) {
        Map<String, Object> rateMap = gson.fromJson(this.toJsonString(), Map.class);
        rateMap.put("deliveryDate", deliveryDate);
        String json = gson.toJson(rateMap);
        this.rate = gson.fromJson(json, Rate.class);
    }

    @Override
    public Boolean getDeliveryDateGuaranteed() {
        return rate.getDeliveryDateGuaranteed();
    }

    @Override
    public Number getEstDeliveryDays() {
        return rate.getEstDeliveryDays();
    }

    @Override
    public String getShipmentId() {
        return rate.getShipmentId();
    }

    @Override
    public String getCarrierAccountId() {
        return rate.getCarrierAccountId();
    }

    @Override
    public String getBillingType() {
        return rate.getBillingType();
    }

    @Override
    public String getId() {
        return rate.getId();
    }

    @Override
    public String getMode() {
        return rate.getMode();
    }

    @Override
    public String getObject() {
        return rate.getObject();
    }

    @Override
    public Date getCreatedAt() {
        return rate.getCreatedAt();
    }

    @Override
    public Date getUpdatedAt() {
        return rate.getUpdatedAt();
    }

    @Override
    public String toString() {
        return rate.toString();
    }

    @Override
    public String prettyPrint() {
        return rate.prettyPrint();
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();

        if (getId() != null) map.put("id", getId());
        if (getCarrier() != null) map.put("carrier", getCarrier());
        if (getService() != null) map.put("service", getService());
        if (getRate() != null) map.put("rate", getRate());
        if (getCurrency() != null) map.put("currency", getCurrency());
        if (getListRate() != null) map.put("list_rate", getListRate());
        if (getListCurrency() != null) map.put("list_currency", getListCurrency());
        if (getRetailRate() != null) map.put("retail_rate", getRetailRate());
        if (getRetailCurrency() != null) map.put("retail_currency", getRetailCurrency());
        if (getDeliveryDays() != null) map.put("delivery_days", getDeliveryDays());
        if (getDeliveryDate() != null) map.put("delivery_date", getDeliveryDate());
        if (getDeliveryDateGuaranteed() != null) map.put("delivery_date_guaranteed", getDeliveryDateGuaranteed());
        if (getEstDeliveryDays() != null) map.put("est_delivery_days", getEstDeliveryDays());
        if (getShipmentId() != null) map.put("shipment_id", getShipmentId());
        if (getCarrierAccountId() != null) map.put("carrier_account_id", getCarrierAccountId());
        if (getBillingType() != null) map.put("billing_type", getBillingType());

        return map;
    }

    public Rate getERate() {
        return rate;
    }

    @Override
    public JsonElement toJson() {
        Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
            .create();
        return gson.toJsonTree(this.rate);
    }

    @Override
    public IRate clone() throws CloneNotSupportedException {
        Map<String, Object> rateMap = gson.fromJson(this.toJsonString(), Map.class);
        String json = gson.toJson(rateMap);
        return new RateAdapter(gson.fromJson(json, Rate.class));
    }

    @Override
    public String getProviderType() {
        return "easypost";
    }
}
