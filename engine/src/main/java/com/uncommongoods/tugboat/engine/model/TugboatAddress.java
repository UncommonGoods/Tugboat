// Copyright (c) 2025 Uncommon Goods LLC
// SPDX-License-Identifier: MPL-2.0

package com.uncommongoods.tugboat.engine.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.annotations.Expose;
import com.uncommongoods.tugboat.engine.serialization.DateAdapter;
import com.uncommongoods.tugboat.engine.ports.shipping.model.MapValues;
import com.uncommongoods.tugboat.engine.ports.shipping.model.IAddress;
import com.uncommongoods.tugboat.engine.ports.shipping.model.IAddressVerifications;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TugboatAddress implements IAddress {
    @Expose
    private String id;
    @Expose
    private String mode;
    @Expose
    private String object;
    @Expose
    private Date createdAt;
    @Expose
    private Date updatedAt;
    @Expose
    private String name;
    @Expose
    private String company;
    @Expose
    private String street1;
    @Expose
    private String street2;
    @Expose
    private String city;
    @Expose
    private String state;
    @Expose
    private String zip;
    @Expose
    private String country;
    @Expose
    private String phone;
    @Expose
    private String email;
    @Expose
    private String message;
    @Expose
    private String carrierFacility;
    @Expose
    private String federalTaxId;
    @Expose
    private Boolean residential;
    @Expose
    private IAddressVerifications verifications;
    @Expose
    private Boolean verify;

    // Dates go out in the same ISO/UTC form the Map constructor reads back.
    private static final Gson gson = new GsonBuilder()
        .registerTypeAdapter(Date.class, new DateAdapter())
        .create();


    public TugboatAddress() {}

    public TugboatAddress(String name, String street1, String street2, String city, String state, String zip, String country, String phone, String email, boolean residential) {
        this.name = name;
        this.street1 = street1;
        this.street2 = street2;
        this.city = city;
        this.state = state;
        this.zip = zip;
        this.country = country;
        this.phone = phone;
        this.email = email;
        this.residential = residential;
    }

    public TugboatAddress(Map<String, Object> addressMap) {
        Object idObj = addressMap.get("id");
        if (idObj != null) {
            this.id = idObj.toString();
        }

        Object modeObj = addressMap.get("mode");
        if (modeObj != null) {
            this.mode = modeObj.toString();
        }

        Object objectObj = addressMap.get("object");
        if (objectObj != null) {
            this.object = objectObj.toString();
        }

        this.createdAt = MapValues.asDate(addressMap, "createdAt", "created_at");

        this.updatedAt = MapValues.asDate(addressMap, "updatedAt", "updated_at");

        Object nameObj = addressMap.get("name");
        if (nameObj != null) {
            this.name = nameObj.toString();
        }

        Object companyObj = addressMap.get("company");
        if (companyObj != null) {
            this.company = companyObj.toString();
        }

        Object street1Obj = addressMap.get("street1");
        if (street1Obj != null) {
            this.street1 = street1Obj.toString();
        }

        Object street2Obj = addressMap.get("street2");
        if (street2Obj != null) {
            this.street2 = street2Obj.toString();
        }

        Object cityObj = addressMap.get("city");
        if (cityObj != null) {
            this.city = cityObj.toString();
        }

        Object stateObj = addressMap.get("state");
        if (stateObj != null) {
            this.state = stateObj.toString();
        }

        Object zipObj = addressMap.get("zip");
        if (zipObj != null) {
            this.zip = zipObj.toString();
        }

        Object countryObj = addressMap.get("country");
        if (countryObj != null) {
            this.country = countryObj.toString();
        }

        Object phoneObj = addressMap.get("phone");
        if (phoneObj != null) {
            this.phone = phoneObj.toString();
        }

        Object emailObj = addressMap.get("email");
        if (emailObj != null) {
            this.email = emailObj.toString();
        }

        Object messageObj = addressMap.get("message");
        if (messageObj != null) {
            this.message = messageObj.toString();
        }

        Object carrierFacilityObj = addressMap.get("carrier_facility");
        if (carrierFacilityObj != null) {
            this.carrierFacility = carrierFacilityObj.toString();
        }

        Object federalTaxIdObj = addressMap.get("federal_tax_id");
        if (federalTaxIdObj != null) {
            this.federalTaxId = federalTaxIdObj.toString();
        }

        Object residentialObj = addressMap.get("residential");
        if (residentialObj instanceof Boolean) {
            this.residential = (Boolean) residentialObj;
        }

        Object verificationsObj = addressMap.get("verifications");
        if (verificationsObj instanceof Map) {
            this.verifications = new TugboatAddressVerifications((Map<String, Object>) verificationsObj);
        }

        Object verifyObj = addressMap.get("verify");
        if (verifyObj instanceof Boolean) {
            this.verify = (Boolean) verifyObj;
        }
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    @Override
    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    @Override
    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    @Override
    public String getStreet1() {
        return street1;
    }

    public void setStreet1(String street1) {
        this.street1 = street1;
    }

    @Override
    public String getStreet2() {
        return street2;
    }

    public void setStreet2(String street2) {
        this.street2 = street2;
    }

    @Override
    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    @Override
    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @Override
    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    @Override
    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    @Override
    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Override
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String getCarrierFacility() {
        return carrierFacility;
    }

    public void setCarrierFacility(String carrierFacility) {
        this.carrierFacility = carrierFacility;
    }

    @Override
    public String getFederalTaxId() {
        return federalTaxId;
    }

    public void setFederalTaxId(String federalTaxId) {
        this.federalTaxId = federalTaxId;
    }

    @Override
    public Boolean getResidential() {
        return residential;
    }

    public void setResidential(Boolean residential) {
        this.residential = residential;
    }

    @Override
    public IAddressVerifications getVerifications() {
        return verifications;
    }

    public void setVerifications(IAddressVerifications verifications) {
        this.verifications = verifications;
    }

    public void setVerify(Boolean verify) {
        this.verify = verify;
    }

    @Override
    public JsonElement toJson() {
        return gson.toJsonTree(this);
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("mode", mode);
        map.put("object", object);
        map.put("created_at", createdAt);
        map.put("updated_at", updatedAt);
        map.put("name", name);
        map.put("company", company);
        map.put("street1", street1);
        map.put("street2", street2);
        map.put("city", city);
        map.put("state", state);
        map.put("zip", zip);
        map.put("country", country);
        map.put("phone", phone);
        map.put("email", email);
        map.put("message", message);
        map.put("carrier_facility", carrierFacility);
        map.put("federal_tax_id", federalTaxId);
        map.put("residential", residential);
        if (verifications != null) {
            map.put("verifications", verifications.toMap());
        }
        map.put("verify", verify);
        return map;
    }

    @Override
    public String toString() {
        return toJsonString();
    }

    @Override
    public String prettyPrint() {
        return gson.toJson(toJson());
    }

    @Override
    public String getProviderType() {
        return "tugboat";
    }
}
