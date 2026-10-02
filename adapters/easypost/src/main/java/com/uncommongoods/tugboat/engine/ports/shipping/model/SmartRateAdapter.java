package com.uncommongoods.tugboat.engine.ports.shipping.model;

import com.easypost.model.SmartRate;

import java.util.Date;

public class SmartRateAdapter implements ISmartRate {
    private final SmartRate smartRate;

    public SmartRateAdapter(SmartRate smartRate) {
        this.smartRate = smartRate;
    }

    @Override
    public String getId() {
        return smartRate.getId();
    }

    @Override
    public String getMode() {
        return smartRate.getMode();
    }

    @Override
    public String getObject() {
        return smartRate.getObject();
    }

    @Override
    public Date getCreatedAt() {
        return smartRate.getCreatedAt();
    }

    @Override
    public Date getUpdatedAt() {
        return smartRate.getUpdatedAt();
    }

    @Override
    public String getService() {
        return smartRate.getService();
    }

    @Override
    public String getCarrier() {
        return smartRate.getCarrier();
    }

    @Override
    public String getRate() {
        Float rate = smartRate.getRate();
        return rate != null ? rate.toString() : null;
    }

    @Override
    public String getCurrency() {
        return smartRate.getCurrency();
    }

    @Override
    public String prettyPrint() {
        return smartRate.prettyPrint();
    }
    
    public SmartRate getSmartRate() {
        return smartRate;
    }
}
