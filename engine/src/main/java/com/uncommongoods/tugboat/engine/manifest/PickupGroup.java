// Copyright (c) 2025 Uncommon Goods LLC
// SPDX-License-Identifier: MPL-2.0

package com.uncommongoods.tugboat.engine.manifest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.annotations.Expose;
import com.uncommongoods.tugboat.engine.Tugboat;
import com.uncommongoods.tugboat.engine.config.EngineConfig;
import com.uncommongoods.tugboat.engine.serialization.LocalDateAdapter;
import com.uncommongoods.tugboat.engine.serialization.LocalDateTimeAdapter;
import com.uncommongoods.tugboat.engine.serialization.ManifestBatchCargoExclusionStrategy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static com.uncommongoods.tugboat.engine.config.EngineConfig.DEFAULT_CLIENT_KEY;

public class PickupGroup {
    private EngineConfig engineConfig;
    @Expose
    private String pickupGroupId;
    @Expose
    private String pickupGroupName;
    @Expose
    private String carrierAccountId;
    @Expose
    private LocalDate pickupDate;
    @Expose
    private String manifestBatchId;
    @Expose
    private List<CarrierService> availableServices;
    @Expose
    private String shippingClientKey;
    @Expose
    ManifestBatch manifestBatch;
    @Expose
    Queue<String> closedManifestBatches;
    private Gson gson = getGson();

    private Gson getGson() {
        if (gson == null) {
            gson = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .addSerializationExclusionStrategy(new ManifestBatchCargoExclusionStrategy())
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
                .create();
        }
        return gson;
    }

    PickupGroup(EngineConfig engineConfig, String pickupGroupId, String pickupGroupName, String carrierAccountId, LocalDate pickupDate) {
        this(engineConfig, pickupGroupId, pickupGroupName, carrierAccountId, pickupDate, DEFAULT_CLIENT_KEY);
    }

    PickupGroup(EngineConfig engineConfig, String pickupGroupId, String pickupGroupName, String carrierAccountId, LocalDate pickupDate, String shippingClientKey) {
        this.engineConfig = engineConfig;
        this.pickupGroupId = pickupGroupId;
        this.pickupGroupName = pickupGroupName.toUpperCase();
        this.carrierAccountId = carrierAccountId;
        this.pickupDate = pickupDate;
        this.availableServices = new ArrayList<>();
        this.shippingClientKey = shippingClientKey;
        this.gson = getGson();
    }

    PickupGroup create() {
        this.manifestBatchId = UUID.randomUUID().toString();
        ManifestBatch batch = new ManifestBatch(engineConfig, pickupGroupId, manifestBatchId, shippingClientKey);
        this.manifestBatch = batch.create();
        return this;
    }

    PickupGroup retrieve() {
        if (this.manifestBatch != null) {
            this.manifestBatch.setEngineConfig(engineConfig);
            this.manifestBatch.retrieve();
        }
        return this;
    }

    PickupGroup close(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("date is required");
        }
        if (date.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("the closeout date (" + date + ") cannot be in the past");
        }
        this.pickupDate = date;

        ManifestBatch currentBatch = new ManifestBatch(engineConfig, pickupGroupId, manifestBatchId, shippingClientKey);
        currentBatch.retrieve();
        currentBatch.close();
        if (this.closedManifestBatches == null) {
            this.closedManifestBatches = new LinkedList<>();
        }
        this.closedManifestBatches.add(manifestBatchId);
        if (this.closedManifestBatches.size() > 20) {
            this.closedManifestBatches.remove();
        }

        this.pickupGroupId = UUID.randomUUID().toString();
        this.manifestBatchId = UUID.randomUUID().toString();
        ManifestBatch newBatch = new ManifestBatch(engineConfig, pickupGroupId, manifestBatchId, shippingClientKey);
        this.manifestBatch = newBatch.create();

        return this;
    }

    void addService(CarrierService service) {
        if (service != null && !availableServices.contains(service)) {
            availableServices.add(service);
            this.pickupGroupId = UUID.randomUUID().toString();
        }
    }

    void removeService(CarrierService service) {
        availableServices.remove(service);
        this.pickupGroupId = UUID.randomUUID().toString();
    }

    public List<CarrierService> getAvailableServices() {
        return availableServices;
    }

    void addTugboatToBatch(Tugboat tugboat) {
        if (this.pickupDate.isBefore(LocalDate.now())) {
            throw new IllegalStateException("the pickup date (" + this.pickupDate + ") on group "+ pickupGroupName + " cannot be in the past");
        }
        this.manifestBatch.retrieve();
        this.manifestBatch.addCargo(tugboat);
    }

    void removeTugboatFromBatch(String cargoId) {
        this.manifestBatch.retrieve();
        this.manifestBatch.removeCargo(cargoId);
    }

    void markBatchCargoConfirmed(String cargoId) {
        this.manifestBatch.retrieve();
        this.manifestBatch.markCargoConfirmed(cargoId);
    }

    void markBatchCargoConfirmed(List<String> cargoIds) {
        this.manifestBatch.retrieve();
        this.manifestBatch.markCargoConfirmed(cargoIds);
    }

    void setEngineConfig(EngineConfig engineConfig) {
        this.engineConfig = engineConfig;
        if (this.manifestBatch != null) {
            this.manifestBatch.setEngineConfig(engineConfig);
        }
    }

    public String getPickupGroupId() {
        return pickupGroupId;
    }

    public String getPickupGroupName() {
        return pickupGroupName;
    }

    public String getCarrierAccountId() {
        return carrierAccountId;
    }

    public LocalDate getPickupDate() {
        return pickupDate;
    }

    public String getManifestBatchId() {
        return manifestBatchId;
    }

    public String getShippingClientKey() {
        return shippingClientKey;
    }

    @Override
    public String toString() {
        if (this.gson == null) {
            this.gson = getGson();
        }
        return this.gson.toJson(this);
    }

    public JsonElement toJson() {
        if (this.gson == null) {
            this.gson = getGson();
        }
        return this.gson.toJsonTree(this);
    }
}
