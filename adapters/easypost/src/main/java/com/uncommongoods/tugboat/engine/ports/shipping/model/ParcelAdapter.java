package com.uncommongoods.tugboat.engine.ports.shipping.model;

import com.easypost.model.Parcel;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ParcelAdapter implements IParcel {
    Gson gson = new GsonBuilder()
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
        .create();
    private Parcel parcel;


    public ParcelAdapter(Parcel parcel) {
        this.parcel = parcel;
    }

    public ParcelAdapter(Map<String, Object> parcelMap) {
        Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
            .create();
        String json = gson.toJson(parcelMap);
        this.parcel = gson.fromJson(json, Parcel.class);
    }

    @Override
    public String getPredefinedPackage() {
        return parcel.getPredefinedPackage();
    }

    @Override
    public Float getWeight() {
        return parcel.getWeight();
    }

    @Override
    public Float getLength() {
        return parcel.getLength();
    }

    @Override
    public Float getWidth() {
        return parcel.getWidth();
    }

    @Override
    public Float getHeight() {
        return parcel.getHeight();
    }

    @Override
    public void setPredefinedPackage(String predefinedPackage) {
        Map<String, Object> parcelMap = gson.fromJson(this.toJsonString(), Map.class);
        parcelMap.put("predefined_package", predefinedPackage);
        String json = gson.toJson(parcelMap);
        this.parcel = gson.fromJson(json, Parcel.class);
    }

    @Override
    public void setWeight(Float weight) {
        Map<String, Object> parcelMap = gson.fromJson(this.toJsonString(), Map.class);
        parcelMap.put("weight", weight);
        String json = gson.toJson(parcelMap);
        this.parcel = gson.fromJson(json, Parcel.class);
    }

    @Override
    public void setLength(Float length) {
        Map<String, Object> parcelMap = gson.fromJson(this.toJsonString(), Map.class);
        parcelMap.put("length", length);
        String json = gson.toJson(parcelMap);
        this.parcel = gson.fromJson(json, Parcel.class);
    }

    @Override
    public void setWidth(Float width) {
        Map<String, Object> parcelMap = gson.fromJson(this.toJsonString(), Map.class);
        parcelMap.put("width", width);
        String json = gson.toJson(parcelMap);
        this.parcel = gson.fromJson(json, Parcel.class);
    }

    @Override
    public void setHeight(Float height) {
        Map<String, Object> parcelMap = gson.fromJson(this.toJsonString(), Map.class);
        parcelMap.put("height", height);
        String json = gson.toJson(parcelMap);
        this.parcel = gson.fromJson(json, Parcel.class);
    }

    @Override
    public String getId() {
        return parcel.getId();
    }

    @Override
    public String getMode() {
        return parcel.getMode();
    }

    @Override
    public String getObject() {
        return parcel.getObject();
    }

    @Override
    public Date getCreatedAt() {
        return parcel.getCreatedAt();
    }

    @Override
    public Date getUpdatedAt() {
        return parcel.getUpdatedAt();
    }

    @Override
    public String toString() {
        return parcel.toString();
    }

    @Override
    public String prettyPrint() {
        return parcel.prettyPrint();
    }

    @Override
    public JsonElement toJson() {
        Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
            .create();
        return gson.toJsonTree(this.parcel);
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        if (getId() != null) map.put("id", getId());
        if (getLength() != null) map.put("length", getLength());
        if (getWidth() != null) map.put("width", getWidth());
        if (getHeight() != null) map.put("height", getHeight());
        if (getPredefinedPackage() != null) map.put("predefined_package", getPredefinedPackage());
        if (getWeight() != null) map.put("weight", getWeight());
        return map;
    }

    public Parcel getParcel() {
        return parcel;
    }

    @Override
    public String getProviderType() {
        return "easypost";
    }
}
