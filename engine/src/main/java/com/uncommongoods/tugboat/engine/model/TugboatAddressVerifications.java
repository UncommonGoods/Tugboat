// Copyright (c) 2025 Uncommon Goods LLC
// SPDX-License-Identifier: MPL-2.0

package com.uncommongoods.tugboat.engine.model;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.annotations.Expose;
import com.uncommongoods.tugboat.engine.ports.shipping.model.IAddressVerification;
import com.uncommongoods.tugboat.engine.ports.shipping.model.IAddressVerifications;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TugboatAddressVerifications implements IAddressVerifications {
    @Expose
    private TugboatAddressVerification zip4;
    @Expose
    private TugboatAddressVerification delivery;

    private static final Gson gson = new Gson();

    public TugboatAddressVerifications(Map<String, Object> verificationsMap) {
        Object zip4Obj = verificationsMap.get("zip4");
        if (zip4Obj instanceof Map) {
            this.zip4 = new TugboatAddressVerification((Map<String, Object>) zip4Obj);
        }

        Object deliveryObj = verificationsMap.get("delivery");
        if (deliveryObj instanceof Map) {
            this.delivery = new TugboatAddressVerification((Map<String, Object>) deliveryObj);
        }
    }

    @Override
    public IAddressVerification getZip4() {
        return zip4;
    }

    public void setZip4(TugboatAddressVerification zip4) {
        this.zip4 = zip4;
    }

    @Override
    public IAddressVerification getDelivery() {
        return delivery;
    }

    public void setDelivery(TugboatAddressVerification delivery) {
        this.delivery = delivery;
    }

    @Override
    public JsonElement toJson() {
        return gson.toJsonTree(this);
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        if (zip4 != null) {
            map.put("zip4", zip4.toMap());
        }
        if (delivery != null) {
            map.put("delivery", delivery.toMap());
        }
        return map;
    }

    @Override
    public String getId() {
        return "";
    }

    @Override
    public String getMode() {
        return "";
    }

    @Override
    public String getObject() {
        return "";
    }

    @Override
    public Date getCreatedAt() {
        return null;
    }

    @Override
    public Date getUpdatedAt() {
        return null;
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
