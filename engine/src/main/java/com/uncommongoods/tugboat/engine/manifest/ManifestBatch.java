// Copyright (c) 2025 Uncommon Goods LLC
// SPDX-License-Identifier: MPL-2.0

package com.uncommongoods.tugboat.engine.manifest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.uncommongoods.tugboat.engine.Tugboat;
import com.uncommongoods.tugboat.engine.ports.shipping.model.IOrder;
import com.uncommongoods.tugboat.engine.ports.shipping.model.IRate;
import com.uncommongoods.tugboat.engine.ports.shipping.model.IScanForm;
import com.uncommongoods.tugboat.engine.ports.shipping.model.IShipment;
import com.uncommongoods.tugboat.engine.ports.shipping.service.IShippingClient;
import com.uncommongoods.tugboat.engine.config.EngineConfig;
import com.uncommongoods.tugboat.engine.serialization.LocalDateTimeAdapter;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.uncommongoods.tugboat.engine.config.EngineConfig.DEFAULT_CLIENT_KEY;
import static com.uncommongoods.tugboat.engine.manifest.ManifestBatchCargo.Status.CONFIRMED;
import static com.uncommongoods.tugboat.engine.manifest.ManifestBatchCargo.Status.PENDING;

public class ManifestBatch {
    private static final String CACHE_PREFIX = "TBMANBATCH:";
    private EngineConfig engineConfig;
    @Expose
    private String manifestBatchId;
    @Expose
    private final String manifestGroupId;
    @Expose
    @SerializedName("manifestBatchCargo")
    private Set<ManifestBatchCargo> manifestBatchCargo;
    @Expose
    private Manifest manifest;
    @Expose
    private String scanFormId;

    private enum BatchStatuses {
        NEW, OPEN, CLOSED
    }

    @Expose
    private String batchStatus = BatchStatuses.NEW.toString();
    @Expose
    @JsonAdapter(LocalDateTimeAdapter.class)
    private LocalDateTime createdAt;
    @Expose
    private String shippingClientKey;
    private Gson gson = getGson();

    private Gson getGson() {
        if (gson == null) {
            gson = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
                .create();
        }
        return gson;
    }


    public ManifestBatch(EngineConfig engineConfig, String manifestGroupId, String manifestBatchId) {
        this(engineConfig, manifestGroupId, manifestBatchId, DEFAULT_CLIENT_KEY);
    }
    public ManifestBatch(EngineConfig engineConfig, String manifestGroupId, String manifestBatchId, String shippingClientKey) {
        if (engineConfig.getCacheClient() == null) {
            throw new IllegalStateException("Cache client is null");
        }
        if (manifestGroupId == null || manifestGroupId.trim().isEmpty()) {
            throw new IllegalArgumentException("manifestGroupId is required");
        }
        if (manifestBatchId == null || manifestBatchId.trim().isEmpty()) {
            throw new IllegalArgumentException("manifestBatchId is required");
        }
        this.engineConfig = engineConfig;
        this.manifestGroupId = manifestGroupId;
        this.manifestBatchId = manifestBatchId;
        this.manifestBatchCargo = new HashSet<>();
        this.shippingClientKey = shippingClientKey;
    }

    public ManifestBatch create() {
        this.createdAt = LocalDateTime.now();
        save();
        return this;
    }

    public ManifestBatch retrieve() {
        String cacheKey = engineConfig.getCachePrefix() + CACHE_PREFIX + manifestBatchId;
        String batchJson = engineConfig.getCacheClient().get(cacheKey);

        if (batchJson != null && !batchJson.trim().isEmpty()) {
            ManifestBatch batch = getGson().fromJson(batchJson, ManifestBatch.class);
            this.manifestBatchCargo = batch.manifestBatchCargo != null ? batch.manifestBatchCargo : new HashSet<>();
            this.batchStatus = batch.batchStatus;
            this.scanFormId = batch.scanFormId;
            this.createdAt = batch.createdAt;
            this.shippingClientKey = batch.shippingClientKey != null ? batch.shippingClientKey : DEFAULT_CLIENT_KEY;
        } else {
            throw new IllegalStateException("Batch " + manifestBatchId + " not found");
        }
        return this;
    }

    public ManifestBatch save() {
        if (manifestBatchId == null) {
            throw new IllegalStateException("Batch must be created or retrieved before it can be saved");
        }
        String cacheKey = engineConfig.getCachePrefix() + CACHE_PREFIX + manifestBatchId;
        String batchJson = getGson().toJson(this);
        engineConfig.getCacheClient().setEx(cacheKey, engineConfig.getCacheExpirySeconds(), batchJson);
        return this;
    }

    public ManifestBatch close() {
        if (manifestBatchId == null) {
            throw new IllegalStateException("Batch must be created or retrieved before it can be closed");
        }
        this.batchStatus = BatchStatuses.CLOSED.toString();

        if (manifestBatchCargo != null && !manifestBatchCargo.isEmpty()) {
            List<ManifestBatchCargo> pendingCargo = this.manifestBatchCargo.stream()
                .filter(cargo -> cargo.getStatus().equals(PENDING))
                .toList();
            pendingCargo.forEach(this.manifestBatchCargo::remove);
            tryCreateScanForm();
            String newManifestId = UUID.randomUUID().toString();
            Manifest manifest = new Manifest(engineConfig, manifestGroupId, manifestBatchId, newManifestId);
            this.manifest = manifest.create();
        }
        save();
        return this;
    }

    public List<ManifestBatchCargo> getManifestBatchCargo() {
        return manifestBatchCargo != null ? new ArrayList<>(manifestBatchCargo) : new ArrayList<>();
    }

    public void addCargo(Tugboat tugboat) {
        if (manifestBatchId == null) {
            throw new IllegalStateException("Batch must be created or retrieved before cargo may be added");
        }
        if (tugboat == null) {
            throw new IllegalArgumentException("tugboat is required");
        }
        IRate selectedRate = tugboat.getSelectedRate();
        if (tugboat.getParcels().size() > 1) {
            IOrder selectedOrder = getSelectedOrder(tugboat);
            List<String> trackingCodes = selectedOrder.getShipments().stream()
                .map(IShipment::getTrackingCode)
                .toList();
            CarrierService carrierService = new CarrierService(selectedRate.getCarrier(), selectedRate.getService());
            ManifestBatchCargo cargo = new ManifestBatchCargo(tugboat.getCargoId(), selectedOrder.getId(), carrierService, trackingCodes);
            addCargo(cargo);
        } else {
            IShipment selectedShipment = getSelectedShipment(tugboat);
            ManifestBatchCargo cargo = new ManifestBatchCargo(tugboat.getCargoId(), selectedRate, selectedShipment);
            addCargo(cargo);
        }
    }

    private IOrder getSelectedOrder(Tugboat tugboat) {
        IRate selectedRate = tugboat.getSelectedRate();
        return tugboat.getOrderRateResponses().stream()
            .filter(order -> order.getShipments().stream()
                .anyMatch(shipment -> shipment.getId().equals(selectedRate.getShipmentId())))
            .findFirst()
            .orElse(null);
    }

    private IShipment getSelectedShipment(Tugboat tugboat) {
        IRate selectedRate = tugboat.getSelectedRate();
        return tugboat.getShipmentRateResponses().stream()
            .filter(shipment -> shipment.getId().equals(selectedRate.getShipmentId()))
            .findFirst()
            .orElse(null);
    }

    void addCargo(ManifestBatchCargo manifestBatchCargo) {
        if (manifestBatchId == null) {
            throw new IllegalStateException("Batch must be created or retrieved before cargo may be added");
        }
        if (this.manifestBatchCargo == null) {
            this.manifestBatchCargo = new HashSet<>();
        }
        this.batchStatus = BatchStatuses.OPEN.toString();
        boolean newCargo = this.manifestBatchCargo.add(manifestBatchCargo);
        if (!newCargo) {
            this.manifestBatchCargo.remove(manifestBatchCargo);
            this.manifestBatchCargo.add(manifestBatchCargo);
        }
        save();
    }

    public void addCargo(List<ManifestBatchCargo> cargo) {
        if (manifestBatchId == null) {
            throw new IllegalStateException("Batch must be created or retrieved before cargo may be added");
        }
        if (cargo == null || cargo.isEmpty()) {
            return;
        }
        if (manifestBatchCargo == null) {
            manifestBatchCargo = new HashSet<>();
        }
        this.batchStatus = BatchStatuses.OPEN.toString();
        manifestBatchCargo.addAll(cargo);
        save();
    }

    public void markCargoConfirmed(String cargoId) {
        this.manifestBatchCargo.stream()
            .filter(mbcargo -> mbcargo.getCargoId().equals(cargoId))
            .findFirst()
            .ifPresent(batchCargo -> batchCargo.setStatus(CONFIRMED));
        this.save();
    }

    public void markCargoConfirmed(List<String> cargoIds) {
        this.manifestBatchCargo.stream()
            .filter(mbcargo -> cargoIds.contains(mbcargo.getCargoId()))
            .forEach(batchCargo -> batchCargo.setStatus(CONFIRMED));
        this.save();
    }

    public void removeCargo(String cargoId) {
        if (manifestBatchId == null) {
            throw new IllegalStateException("Batch must be created or retrieved before cargo may be removed");
        }
        manifestBatchCargo.removeIf(cargo -> cargo.getCargoId().equals(cargoId));
        save();
    }

    public void removeCargo(List<String> cargoIds) {
        if (manifestBatchId == null) {
            throw new IllegalStateException("Batch must be created or retrieved before cargo may be removed");
        }
        if (cargoIds == null || cargoIds.isEmpty() || manifestBatchCargo == null) {
            return;
        }
        cargoIds.forEach(cargoId -> manifestBatchCargo.removeIf(cargo -> cargo.getCargoId().equals(cargoId)));
        save();
    }

    EngineConfig getEngineConfig() {
        return engineConfig;
    }

    void setEngineConfig(EngineConfig engineConfig) {
        this.engineConfig = engineConfig;
    }

    public String getManifestBatchId() {
        return manifestBatchId;
    }

    public String getManifestGroupId() {
        return manifestGroupId;
    }

    public String getBatchStatus() {
        return batchStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    String getShippingClientKey() {
        return shippingClientKey;
    }

    void setShippingClientKey(String shippingClientKey) {
        this.shippingClientKey = shippingClientKey;
    }

    public Manifest getManifest() {
        return manifest;
    }

    public boolean isClosed() {
        return BatchStatuses.CLOSED.toString().equals(batchStatus);
    }

    public boolean isOpen() {
        return BatchStatuses.OPEN.toString().equals(batchStatus);
    }

    public boolean isNew() {
        return BatchStatuses.NEW.toString().equals(batchStatus);
    }

    public String getScanFormId() {
        return scanFormId;
    }

    public void setScanFormId(String scanFormId) {
        this.scanFormId = scanFormId;
    }

    private void tryCreateScanForm() {
        if (engineConfig.getShippingClients().isEmpty()) {
            return;
        }

        List<String> shipmentIds = manifestBatchCargo.stream()
            .map(ManifestBatchCargo::getExternalPackageId)
            .filter(id -> id != null && !id.trim().isEmpty())
            .collect(Collectors.toList());

        if (shipmentIds.isEmpty()) {
            return;
        }

        IShippingClient shippingClient = engineConfig.getShippingClients().get(shippingClientKey);
        if (shippingClient == null) {
            return;
        }

        try {
            // ep max batch size is 1000
            for (int i = 0; i < shipmentIds.size(); i += 1000) {
                List<String> batch = shipmentIds.subList(i, Math.min(i + 1000, shipmentIds.size()));
                Map<String, Object> params = new HashMap<>();
                params.put("shipments", batch);

                IScanForm scanForm = shippingClient.getScanFormService().create(params);
                if (scanForm != null && scanForm.getId() != null) {
                    this.scanFormId = scanForm.getId();
                }
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
            // continue -- we don't care.  ScanForms aren't supported by many carriers
        }
    }
}
