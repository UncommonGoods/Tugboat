package com.uncommongoods.tugboat.engine.ports.shipping.model;


import com.easypost.model.Address;
import com.easypost.model.AddressVerifications;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AddressAdapter implements IAddress {
    private final Address address;

    public AddressAdapter(Address address) {
        this.address = address;
    }

    public AddressAdapter(Map<String, Object> addressMap) {
        Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
            .create();
        String json = gson.toJson(addressMap);
        this.address = gson.fromJson(json, Address.class);
    }

    @Override
    public String getId() {
        return address.getId();
    }

    @Override
    public String getMode() {
        return address.getMode();
    }

    @Override
    public String getObject() {
        return address.getObject();
    }

    @Override
    public Date getCreatedAt() {
        return address.getCreatedAt();
    }

    @Override
    public Date getUpdatedAt() {
        return address.getUpdatedAt();
    }

    @Override
    public String getName() {
        return address.getName();
    }

    @Override
    public String getCompany() {
        return address.getCompany();
    }

    @Override
    public String getStreet1() {
        return address.getStreet1();
    }

    @Override
    public String getStreet2() {
        return address.getStreet2();
    }

    @Override
    public String getCity() {
        return address.getCity();
    }

    @Override
    public String getState() {
        return address.getState();
    }

    @Override
    public String getZip() {
        return address.getZip();
    }

    @Override
    public String getCountry() {
        return address.getCountry();
    }

    @Override
    public String getPhone() {
        return address.getPhone();
    }

    @Override
    public String getEmail() {
        return address.getEmail();
    }

    @Override
    public String getMessage() {
        return address.getMessage();
    }

    @Override
    public String getCarrierFacility() {
        return address.getCarrierFacility();
    }

    @Override
    public String getFederalTaxId() {
        return address.getFederalTaxId();
    }

    @Override
    public Boolean getResidential() {
        return address.getResidential();
    }

    @Override
    public IAddressVerifications getVerifications() {
        AddressVerifications addressVerifications = address.getVerifications();
        return addressVerifications != null ? new AddressVerificationsAdapter(addressVerifications) : null;
    }

    @Override
    public String toString() {
        return address.toString();
    }

    @Override
    public String prettyPrint() {
        return address.prettyPrint();
    }

    public Address getAddress() {
        return address;
    }

    @Override
    public JsonElement toJson() {
        Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
            .create();
        return gson.toJsonTree(this.address);
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        if (getId() != null) map.put("id", getId());
        if (getStreet1() != null) map.put("street1", getStreet1());
        if (getStreet2() != null) map.put("street2", getStreet2());
        if (getCity() != null) map.put("city", getCity());
        if (getState() != null) map.put("state", getState());
        if (getZip() != null) map.put("zip", getZip());
        if (getCountry() != null) map.put("country", getCountry());
        if (getName() != null) map.put("name", getName());
        if (getCompany() != null) map.put("company", getCompany());
        if (getPhone() != null) map.put("phone", getPhone());
        if (getEmail() != null) map.put("email", getEmail());
        if (getResidential() != null) map.put("residential", getResidential());
        if (getCarrierFacility() != null) map.put("carrier_facility", getCarrierFacility());
        if (getFederalTaxId() != null) map.put("federal_tax_id", getFederalTaxId());

        IAddressVerifications verifications = getVerifications();
        if (verifications != null) {
            map.put("verifications", ((Mappable) verifications).toMap());
        }

        return map;
    }

    @Override
    public String getProviderType() {
        return "easypost";
    }
}
