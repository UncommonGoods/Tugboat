// Copyright (c) 2025 Uncommon Goods LLC
// SPDX-License-Identifier: MPL-2.0

package com.uncommongoods.tugboat.engine.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.uncommongoods.tugboat.engine.serialization.DateAdapter;
import com.uncommongoods.tugboat.engine.ports.shipping.model.MapValues;
import com.uncommongoods.tugboat.engine.ports.shipping.model.IParcel;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TugboatParcel implements IParcel {
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
    @SerializedName("predefined_package")
    private String predefinedPackage;
    @Expose
    private Float weight;
    @Expose
    private Float length;
    @Expose
    private Float width;
    @Expose
    private Float height;

    // Dates go out in the same ISO/UTC form the Map constructor reads back.
    private static final Gson gson = new GsonBuilder()
        .registerTypeAdapter(Date.class, new DateAdapter())
        .create();

    public TugboatParcel() {}

    public TugboatParcel(Float weight, Float length, Float width, Float height) {
        this.weight = weight;
        this.length = length;
        this.width = width;
        this.height = height;
    }

    public TugboatParcel(Float weight, Float length, Float width, Float height, String predefinedPackage) {
        this(weight, length, width, height);
        this.predefinedPackage = predefinedPackage;
    }

    public TugboatParcel(Map<String, Object> parcelMap) {
        Object idObj = parcelMap.get("id");
        if (idObj != null) {
            this.id = idObj.toString();
        }

        Object modeObj = parcelMap.get("mode");
        if (modeObj != null) {
            this.mode = modeObj.toString();
        }

        Object objectObj = parcelMap.get("object");
        if (objectObj != null) {
            this.object = objectObj.toString();
        }

        this.createdAt = MapValues.asDate(parcelMap, "createdAt", "created_at");

        this.updatedAt = MapValues.asDate(parcelMap, "updatedAt", "updated_at");

        Object predefinedPackageObj = parcelMap.get("predefined_package");
        if (predefinedPackageObj != null) {
            this.predefinedPackage = predefinedPackageObj.toString();
        }

        Object weightObj = parcelMap.get("weight");
        if (weightObj instanceof Number) {
            this.weight = ((Number) weightObj).floatValue();
        }

        Object lengthObj = parcelMap.get("length");
        if (lengthObj instanceof Number) {
            this.length = ((Number) lengthObj).floatValue();
        }

        Object widthObj = parcelMap.get("width");
        if (widthObj instanceof Number) {
            this.width = ((Number) widthObj).floatValue();
        }

        Object heightObj = parcelMap.get("height");
        if (heightObj instanceof Number) {
            this.height = ((Number) heightObj).floatValue();
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
    public String getPredefinedPackage() {
        return predefinedPackage;
    }

    public void setPredefinedPackage(String predefinedPackage) {
        this.predefinedPackage = predefinedPackage;
    }

    @Override
    public Float getWeight() {
        return weight;
    }

    public void setWeight(Float weight) {
        this.weight = weight;
    }

    @Override
    public Float getLength() {
        return length;
    }

    public void setLength(Float length) {
        this.length = length;
    }

    @Override
    public Float getWidth() {
        return width;
    }

    public void setWidth(Float width) {
        this.width = width;
    }

    @Override
    public Float getHeight() {
        return height;
    }

    public void setHeight(Float height) {
        this.height = height;
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
        map.put("predefined_package", predefinedPackage);
        map.put("weight", weight);
        map.put("length", length);
        map.put("width", width);
        map.put("height", height);
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
