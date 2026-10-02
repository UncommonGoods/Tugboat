package com.uncommongoods.tugboat.engine.ports.shipping.model;

import com.easypost.model.CarrierAccount;
import com.easypost.model.Fields;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

import java.util.Date;
import java.util.Map;

public class CarrierAccountAdapter implements ICarrierAccount {
    private final CarrierAccount carrierAccount;

    public CarrierAccountAdapter(CarrierAccount carrierAccount) {
        super();
        this.carrierAccount = carrierAccount;
    }

    @Override
    public String getType() {
        return carrierAccount.getType();
    }

    @Override
    public IFields getFields() {
        Fields fields = carrierAccount.getFields();
        return fields != null ? new FieldsAdapter(fields) : null;
    }

    @Override
    public boolean isClone() {
        return carrierAccount.isClone();
    }

    @Override
    public String getLogo() {
        return carrierAccount.getLogo();
    }

    @Override
    public String getReadable() {
        return carrierAccount.getReadable();
    }

    @Override
    public String getDescription() {
        return carrierAccount.getDescription();
    }

    @Override
    public String getReference() {
        return carrierAccount.getReference();
    }

    @Override
    public String getBillingType() {
        return carrierAccount.getBillingType();
    }

    @Override
    public Map<String, Object> getCredentials() {
        return carrierAccount.getCredentials();
    }

    @Override
    public Map<String, Object> getTestCredentials() {
        return carrierAccount.getTestCredentials();
    }

    public CarrierAccount getCarrierAccount() {
        return carrierAccount;
    }

    @Override
    public JsonElement toJson() {
        Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
            .create();
        return gson.toJsonTree(this);
    }

    @Override
    public String toJsonString() {
        return ICarrierAccount.super.toJsonString();
    }

    @Override
    public String getId() {
        return carrierAccount.getId();
    }

    @Override
    public String getMode() {
        return carrierAccount.getMode();
    }

    @Override
    public String getObject() {
        return carrierAccount.getObject();
    }

    @Override
    public Date getCreatedAt() {
        return carrierAccount.getCreatedAt();
    }

    @Override
    public Date getUpdatedAt() {
        return carrierAccount.getUpdatedAt();
    }

    @Override
    public String prettyPrint() {
        return "";
    }

    @Override
    public String getProviderType() {
        return "easypost";
    }
}
