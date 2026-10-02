// Copyright (c) 2025 Uncommon Goods LLC
// SPDX-License-Identifier: MPL-2.0

package com.uncommongoods.tugboat.engine.manifest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.annotations.Expose;
import com.uncommongoods.tugboat.engine.Tugboat;
import com.uncommongoods.tugboat.engine.ports.shipping.model.IAddress;
import com.uncommongoods.tugboat.engine.ports.shipping.service.IShippingClient;
import com.uncommongoods.tugboat.engine.config.EngineConfig;
import com.uncommongoods.tugboat.engine.exception.TugboatException;
import com.uncommongoods.tugboat.engine.serialization.ManifestBatchCargoExclusionStrategy;
import com.uncommongoods.tugboat.engine.serialization.TugboatGson;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.uncommongoods.tugboat.engine.config.EngineConfig.DEFAULT_CLIENT_KEY;

public class PickupFacility {
    private static final String CACHE_PREFIX = "TBMANFACILITY:";
    private static final String CARRIER_SERVICE_GROUP_PREFIX = "CARRIERSERVICEGROUP";
    private EngineConfig engineConfig;
    @Expose
    private String pickupFacilityId;
    @Expose
    private IAddress address;
    @Expose
    private String pickupFacilityCode;
    @Expose
    private List<PickupGroup> pickupGroups = new ArrayList<>();
    private Gson gson;

    private Gson getGson() {
        if (gson == null) {
            // Same polymorphic model handling as cargo, plus this document's own
            // @Expose filtering and cargo exclusion.
            gson = TugboatGson.cargo()
                .newBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .addSerializationExclusionStrategy(new ManifestBatchCargoExclusionStrategy())
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
                .create();
        }
        return gson;
    }

    public PickupFacility(EngineConfig engineConfig, String pickupFacilityCode) {
        if (engineConfig.getCacheClient() == null) {
            throw new IllegalStateException("A cache client is required when creating Pickup Facilities");
        }
        if (pickupFacilityCode == null || pickupFacilityCode.trim().isEmpty()) {
            throw new IllegalArgumentException("manifestFacilityCode is required");
        }
        this.engineConfig = engineConfig;
        this.pickupFacilityCode = pickupFacilityCode;
        this.gson = getGson();
    }

    public PickupFacility(EngineConfig engineConfig, String pickupFacilityCode, IAddress address) {
        this(engineConfig, pickupFacilityCode);
        this.address = address;
        this.pickupGroups = new ArrayList<>();
    }

    public PickupFacility(String pickupFacilityCode, String pickupFacilityId, IAddress address, List<PickupGroup> pickupGroups) {
        if (pickupFacilityCode == null || pickupFacilityCode.trim().isEmpty()) {
            throw new IllegalArgumentException("manifestFacilityCode is required");
        }
        this.pickupFacilityCode = pickupFacilityCode;
        this.pickupFacilityId = pickupFacilityId;
        this.address = address;
        this.pickupGroups = pickupGroups != null ? pickupGroups : new ArrayList<>();
        this.gson = getGson();
    }

    public PickupFacility create() throws TugboatException {
        IShippingClient shippingClient = this.engineConfig.getShippingClients().get("default");
        this.address = shippingClient.getAddressService().create(this.address.toMap());
        this.pickupFacilityId = UUID.randomUUID().toString();
        save();
        return this;
    }

    public PickupFacility retrieve() {
        String cacheKey = engineConfig.getCachePrefix() + CACHE_PREFIX + pickupFacilityCode;
        String facilityJson = engineConfig.getCacheClient().get(cacheKey);

        if (facilityJson != null && !facilityJson.trim().isEmpty()) {
            PickupFacility facility = getGson().fromJson(facilityJson, PickupFacility.class);
            this.pickupGroups = facility.pickupGroups != null ? facility.pickupGroups : new ArrayList<>();
            this.address = facility.address;
            this.pickupFacilityId = facility.pickupFacilityId;
            this.pickupFacilityCode = facility.pickupFacilityCode;
            if (this.gson == null) {
                this.gson = getGson();
            }

            for (PickupGroup pickupGroup : this.pickupGroups) {
                pickupGroup.setEngineConfig(this.engineConfig);
                pickupGroup.retrieve();
            }
        }
        return this;
    }

    private void save() {
        if (pickupFacilityId == null) {
            throw new IllegalStateException("Pickup Facility must be created or retrieved before it can be saved");
        }
        String cacheKey = engineConfig.getCachePrefix() + CACHE_PREFIX + pickupFacilityCode;

        String facilityJson = gson.toJson(this);
        engineConfig.getCacheClient().set(cacheKey, facilityJson);
    }


    public String createPickupGroup(String pickupGroupName, String carrierAccountId, LocalDate pickupDate) {
        return createPickupGroup(pickupGroupName, carrierAccountId, pickupDate, DEFAULT_CLIENT_KEY);
    }

    public String createPickupGroup(String pickupGroupName, String carrierAccountId, LocalDate pickupDate, String shippingClientKey) {
        if (pickupFacilityId == null) {
            throw new IllegalStateException("Pickup Facility must be created or retrieved before creating Pickup Group");
        }
        if (pickupGroupName == null || pickupGroupName.trim().isEmpty()) {
            throw new IllegalArgumentException("pickupGroupName is required");
        }
        if (carrierAccountId == null || carrierAccountId.trim().isEmpty()) {
            throw new IllegalArgumentException("carrierAccountId is required");
        }
        if (pickupDate == null) {
            throw new IllegalArgumentException("pickupDate is required");
        }
        PickupGroup pickupGroup = this.getPickupGroupByName(pickupGroupName);
        if (pickupGroup != null) {
            throw new IllegalArgumentException("pickupGroupName " + pickupGroupName + " already exists");
        }

        String newPickupGroupId = UUID.randomUUID().toString();
        PickupGroup group = new PickupGroup(engineConfig, newPickupGroupId, pickupGroupName, carrierAccountId, pickupDate, shippingClientKey);
        group.create();
        pickupGroups.add(group);
        save();
        return group.getPickupGroupId();
    }

    public PickupGroup getPickupGroup(String pickupGroupId) {
        if (pickupGroupId == null) {
            return null;
        }
        return pickupGroups.stream()
            .filter(group -> pickupGroupId.equals(group.getPickupGroupId()))
            .findFirst()
            .orElse(null);
    }

    public PickupGroup getPickupGroupByBatchId(String manifestBatchId) {
        if (manifestBatchId == null) {
            return null;
        }
        return pickupGroups.stream()
            .filter(group -> manifestBatchId.equals(group.getManifestBatchId()))
            .findFirst()
            .orElse(null);
    }

    public PickupGroup getPickupGroupByName(String pickupGroupName) {
        if (pickupGroupName == null) {
            return null;
        }
        return pickupGroups.stream()
            .filter(group -> pickupGroupName.equalsIgnoreCase(group.getPickupGroupName()))
            .findFirst()
            .orElse(null);
    }

    public List<PickupGroup> getPickupGroups() {
        return new ArrayList<>(pickupGroups);
    }

    public void removePickupGroup(String pickupGroupId) {
        this.retrieve();
        if (pickupFacilityId == null) {
            throw new IllegalStateException("Pickup Facility must be created or retrieved before removing Manifest Group");
        }
        if (pickupGroupId == null) {
            return;
        }
        pickupGroups.removeIf(group -> pickupGroupId.equals(group.getPickupGroupId()));
        save();
    }

    public void removePickupGroupByName(String pickupGroupName) {
        this.retrieve();
        if (pickupFacilityId == null) {
            throw new IllegalStateException("Pickup Facility must be created or retrieved before removing Manifest Group");
        }
        if (pickupGroupName == null) {
            return;
        }
        pickupGroups.removeIf(group -> pickupGroupName.equals(group.getPickupGroupName()));
        save();
    }

    public void addServiceToGroup(String pickupGroupName, CarrierService service) {
        if (pickupFacilityId == null) {
            throw new IllegalStateException("Pickup Facility must be created or retrieved before a service may be added to a Pickup Group");
        }
        this.retrieve();
        PickupGroup group = getPickupGroupByName(pickupGroupName);
        if (group.getAvailableServices().contains(service)) {
            throw new IllegalArgumentException("Service " + service + " already exists in group");
        }
        this.getPickupGroups().forEach(pickupGroup -> {
            if (!pickupGroup.getPickupGroupId().equals(group.getPickupGroupId()) &&
                pickupGroup.getCarrierAccountId().equals(group.getCarrierAccountId()) &&
                pickupGroup.getAvailableServices().contains(service)) {
                throw new IllegalArgumentException(
                    "Pickup Group " + pickupGroup.getPickupGroupName() + " (" + pickupGroup.getPickupGroupId() + ") has a matching service and carrier account id. " +
                        "Groups with matching account IDs must have unique services"
                );
            }
        });

        group.addService(service);
        save();
    }

    public void removeServiceFromGroup(String pickupGroupId, CarrierService service) {
        if (pickupFacilityId == null) {
            throw new IllegalStateException("Pickup Facility must be created or retrieved before a service may be removed from a Pickup Group");
        }
        this.retrieve();
        PickupGroup group = getPickupGroup(pickupGroupId);
        if (group != null) {
            group.removeService(service);
            save();
        }
    }

    public void addTugboatToGroup(Tugboat tugboat) {
        if (pickupFacilityId == null) {
            throw new IllegalStateException("Pickup Facility must be created or retrieved before a Tugboat may be added to a Pickup Group");
        }
        this.retrieve();
        PickupGroup pickupGroup = getPickupGroupByBatchId(tugboat.getManifestBatchId());
        if (pickupGroup != null) {
            pickupGroup.addTugboatToBatch(tugboat);
            this.save();
        }
    }

    public void removeTugboatFromGroup(Tugboat tugboat) {
        if (pickupFacilityId == null) {
            throw new IllegalStateException("Pickup Facility must be created or retrieved before a Tugboat may be removed from a Pickup Group");
        }
        this.retrieve();
        PickupGroup pickupGroup = getPickupGroupByBatchId(tugboat.getManifestBatchId());
        if (pickupGroup != null) {
            pickupGroup.removeTugboatFromBatch(tugboat.getCargoId());
            this.save();
        }
    }

    public void markManifestBatchCargoConfirmed(Tugboat tugboat) {
        PickupGroup pickupGroup = getPickupGroupByBatchId(tugboat.getManifestBatchId());
        if (pickupGroup != null) {
            pickupGroup.markBatchCargoConfirmed(tugboat.getCargoId());
        }
    }

    public void markManifestBatchCargoConfirmed(String manifestBatchId, List<String> cargoIds) {
        PickupGroup pickupGroup = getPickupGroupByBatchId(manifestBatchId);
        if (pickupGroup != null) {
            pickupGroup.markBatchCargoConfirmed(cargoIds);
        }
    }

    public List<CarrierService> getAvailableServices(String pickupGroupId) {
        PickupGroup group = getPickupGroup(pickupGroupId);
        if (group != null) {
            return group.getAvailableServices();
        }
        return List.of();
    }

    public Set<CarrierServiceGroup> getGlobalCarrierServices() {
        final String hashKey = this.engineConfig.getCachePrefix() + CARRIER_SERVICE_GROUP_PREFIX;

        Set<CarrierServiceGroup> allServiceGroups = new HashSet<>();

        Map<String, String> carrierServiceGroupsJson = this.engineConfig.getCacheClient().hgetAll(hashKey);

        for (String groupJson : carrierServiceGroupsJson.values()) {
            if (groupJson != null && !groupJson.trim().isEmpty()) {
                CarrierServiceGroup group = getGson().fromJson(groupJson, CarrierServiceGroup.class);
                allServiceGroups.add(group);
            }
        }

        return allServiceGroups;
    }

    public void setGlobalCarrierServices(Set<CarrierServiceGroup> carrierServiceGroups) {
        final String hashKey = this.engineConfig.getCachePrefix() + CARRIER_SERVICE_GROUP_PREFIX;

        Set<CarrierServiceGroup> allServiceGroups = getGlobalCarrierServices();

        allServiceGroups.addAll(carrierServiceGroups);

        for (CarrierServiceGroup group : allServiceGroups) {
            String groupJson = getGson().toJson(group);
            this.engineConfig.getCacheClient().hset(hashKey, group.getReadableCarrier(), groupJson);
        }
    }

    public PickupGroup closePickupGroup(String pickupGroupName, LocalDate date) {
        if (pickupFacilityCode == null) {
            throw new IllegalStateException("Could not close pickup group " + pickupGroupName +
                " -- facility must be created or retrieved first");
        }
        if (pickupGroupName == null || pickupGroupName.trim().isEmpty()) {
            throw new IllegalArgumentException("pickupGroupName is required");
        }
        if (date == null) {
            throw new IllegalArgumentException("date is required");
        }

        PickupGroup group = getPickupGroupByName(pickupGroupName);
        if (group == null) {
            throw new IllegalArgumentException("Pickup group not found: " + pickupGroupName);
        }

        try {
            group.close(date);
        } catch (Exception e) {
            throw new RuntimeException("Failed to close pickup group " + pickupGroupName, e);
        }
        save();
        return group;
    }

    public String getPickupFacilityId() {
        return pickupFacilityId;
    }

    public IAddress getAddress() {
        return this.address;
    }

    public String getPickupFacilityCode() {
        return pickupFacilityCode;
    }

    public EngineConfig getEngineConfig() {
        return engineConfig;
    }

    public void setEngineConfig(EngineConfig engineConfig) {
        this.engineConfig = engineConfig;
        if (this.gson == null) {
            this.gson = getGson();
        }
        if (this.pickupGroups != null) {
            for (PickupGroup pickupGroup : this.pickupGroups) {
                pickupGroup.setEngineConfig(engineConfig);
            }
        }
    }

    public JsonElement toJson() {
        return gson.toJsonTree(this);
    }

    @Override
    public String toString() {
        return gson.toJson(this);
    }
}
