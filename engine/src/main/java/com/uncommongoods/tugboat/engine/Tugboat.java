// Copyright (c) 2025 Uncommon Goods LLC
// SPDX-License-Identifier: MPL-2.0

package com.uncommongoods.tugboat.engine;

import com.google.gson.*;
import com.google.gson.annotations.Expose;
import com.uncommongoods.tugboat.engine.ports.shipping.model.*;
import com.uncommongoods.tugboat.engine.config.EngineConfig;
import com.uncommongoods.tugboat.engine.exception.TugboatException;
import com.uncommongoods.tugboat.engine.serialization.*;
import com.uncommongoods.tugboat.engine.state.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static com.uncommongoods.tugboat.engine.state.ShipmentComponent.*;
import static com.uncommongoods.tugboat.engine.state.State.*;

public class Tugboat extends TugboatBase implements ITugboatState {
    private static final String TUGBOAT_CACHE_PREFIX = "TBCARGO:";
    private static final String TUGBOAT_LOCK_CACHE_PREFIX = "TBCARGOLOCK:";
    private static final Gson gson = TugboatGson.cargo()
        .newBuilder()
        .excludeFieldsWithoutExposeAnnotation()
        .addSerializationExclusionStrategy(new ManifestBatchCargoExclusionStrategy())
        .create();
    private EngineConfig engineConfig;
    @Expose
    private TugboatStateBase packageState;
    private boolean isPackageInitialized;
    @Expose
    private Instant lock;
    private TugboatBuilder builder;

    protected Tugboat(TugboatBuilder builder) {
        super(builder.cargoId, builder.expectedDeliveryDate, builder.pickupFacility,
              builder.originAddress, builder.destinationAddress, builder.returnAddress,
              builder.parcels, builder.options, builder.shippingClientKey);
        this.builder = builder;
        this.engineConfig = builder.engineConfig;
        this.packageState = new InitialTugboatState(this);
    }

    public Tugboat(String packageJson) {
        Tugboat tugboat = gson.fromJson(packageJson, Tugboat.class);
        copyFieldsFrom(tugboat, emptyList());
    }

    public static TugboatBuilder builder(EngineConfig engineConfig, String packageId) {
        return new TugboatBuilder(engineConfig, packageId);
    }

    public Tugboat initialize(BypassTugboatLock bypassLock, List<ShipmentComponent> shipmentComponentsToPreserve) throws TugboatException {
        try {
            retrieveIfUninitialized(shipmentComponentsToPreserve);
            return progressToState(INITIALIZED, bypassLock);
        } catch (Exception e) {
            if (this.getOptions().getErrorHook() != null) {
                this.getOptions().getErrorHook().execute(this, e);
            }
            throw e;
        }
    }

    public Tugboat initialize(List<ShipmentComponent> shipmentComponentsToPreserve) throws TugboatException {
        return this.initialize(BypassTugboatLock.DO_NOT_BYPASS_TUGBOAT_LOCK, shipmentComponentsToPreserve);
    }

    @Override
    public Tugboat initialize() throws TugboatException {
        return this.initialize(BypassTugboatLock.DO_NOT_BYPASS_TUGBOAT_LOCK, emptyList());
    }

    public Tugboat rate(BypassTugboatLock bypassLock, List<ShipmentComponent> shipmentComponentsToPreserve) throws TugboatException {
        try {
            retrieveIfUninitialized(shipmentComponentsToPreserve);
            return progressToState(State.RATED, bypassLock);
        } catch (Exception e) {
            if (this.getOptions().getErrorHook() != null) {
                this.getOptions().getErrorHook().execute(this, e);
            }
            throw e;
        }
    }

    public Tugboat rate(List<ShipmentComponent> shipmentComponentsToPreserve) throws TugboatException {
        return this.rate(BypassTugboatLock.DO_NOT_BYPASS_TUGBOAT_LOCK, shipmentComponentsToPreserve);
    }

    @Override
    public Tugboat rate() throws TugboatException {
        return this.rate(BypassTugboatLock.DO_NOT_BYPASS_TUGBOAT_LOCK, emptyList());
    }

    public Tugboat shop(BypassTugboatLock bypassLock, List<ShipmentComponent> shipmentComponentsToPreserve) throws TugboatException {
        try {
            retrieveIfUninitialized(shipmentComponentsToPreserve);
            rerateIfManifestChanged(shipmentComponentsToPreserve);
            return progressToState(State.SHOPPED, bypassLock);
        } catch (Exception e) {
            if (this.getOptions().getErrorHook() != null) {
                this.getOptions().getErrorHook().execute(this, e);
            }
            throw e;
        }
    }

    public Tugboat shop(List<ShipmentComponent> shipmentComponentsToPreserve) throws TugboatException {
        return this.shop(BypassTugboatLock.DO_NOT_BYPASS_TUGBOAT_LOCK, shipmentComponentsToPreserve);
    }

    @Override
    public Tugboat shop() throws TugboatException {
        return this.shop(BypassTugboatLock.DO_NOT_BYPASS_TUGBOAT_LOCK, emptyList());
    }

    public Tugboat purchase(BypassTugboatLock bypassLock, List<ShipmentComponent> shipmentComponentsToPreserve) throws TugboatException {
        try {
            retrieveIfUninitialized(shipmentComponentsToPreserve);
            rerateIfManifestChanged(shipmentComponentsToPreserve);
            return progressToState(State.PURCHASED, bypassLock);
        } catch (Exception e) {
            if (this.getOptions().getErrorHook() != null) {
                this.getOptions().getErrorHook().execute(this, e);
            }
            throw e;
        }
    }

    public Tugboat purchase(List<ShipmentComponent> shipmentComponentsToPreserve) throws TugboatException {
        return this.purchase(BypassTugboatLock.DO_NOT_BYPASS_TUGBOAT_LOCK, shipmentComponentsToPreserve);
    }

    @Override
    public Tugboat purchase() throws TugboatException {
        return this.purchase(BypassTugboatLock.DO_NOT_BYPASS_TUGBOAT_LOCK, emptyList());
    }

    public List<IPostageLabel> print(BypassTugboatLock bypassLock, List<ShipmentComponent> shipmentComponentsToPreserve) throws TugboatException {
        try {
            retrieveIfUninitialized(shipmentComponentsToPreserve);
            rerateIfManifestChanged(shipmentComponentsToPreserve);
            progressToState(State.PURCHASED, bypassLock);
            List<IPostageLabel> labels = this.packageState.print();
            if (this.getOptions() != null && this.getOptions().getPrintedHook() != null) {
                this.getOptions().getPrintedHook().execute(this);
            } else throw new TugboatException("printed hook not found -- notify dev");
            this.setPackageState(new PrintedTugboatState(this));
            this.save();
            return labels;
        } catch (Exception e) {
            if (this.getOptions().getErrorHook() != null) {
                this.getOptions().getErrorHook().execute(this, e);
            }
            throw e;
        }
    }

    public List<IPostageLabel> print(List<ShipmentComponent> shipmentComponentsToPreserve) throws TugboatException {
        return this.print(BypassTugboatLock.DO_NOT_BYPASS_TUGBOAT_LOCK, shipmentComponentsToPreserve);
    }

    @Override
    public List<IPostageLabel> print() throws TugboatException {
        return this.print(BypassTugboatLock.DO_NOT_BYPASS_TUGBOAT_LOCK, emptyList());
    }

    public List<IPostageLabel> reprint(List<ShipmentComponent> shipmentComponentsToPreserve) throws TugboatException {
        try {
            retrieveIfUninitialized(shipmentComponentsToPreserve);
            return this.packageState.reprint();
        } catch (Exception e) {
            if (this.getOptions().getErrorHook() != null) {
                this.getOptions().getErrorHook().execute(this, e);
            }
            throw e;
        }
    }

    @Override
    public List<IPostageLabel> reprint() throws TugboatException {
        return this.reprint(emptyList());
    }

    public Tugboat voidLabel(BypassTugboatLock bypassLock, List<ShipmentComponent> shipmentComponentsToPreserve) throws TugboatException {
        try {
            retrieveIfUninitialized(shipmentComponentsToPreserve);
            checkLock(bypassLock);
            setLock(bypassLock);
            try {
                this.packageState = this.packageState.voidLabel();
                if (this.getOptions().getVoidedHook() != null) {
                    this.getOptions().getVoidedHook().execute(this);
                }
                this.setPackageState(new VoidedTugboatState(this));
            } catch (Exception e) {
                removeLock(bypassLock);
                throw e;
            }
            removeLock(bypassLock);
            this.save();
            return this;
        } catch (Exception e) {
            if (this.getOptions().getErrorHook() != null) {
                this.getOptions().getErrorHook().execute(this, e);
            }
            throw e;
        }
    }

    public Tugboat voidLabel(List<ShipmentComponent> shipmentComponentsToPreserve) throws TugboatException {
        return this.voidLabel(BypassTugboatLock.DO_NOT_BYPASS_TUGBOAT_LOCK, shipmentComponentsToPreserve);
    }

    @Override
    public Tugboat voidLabel() throws TugboatException {
        return this.voidLabel(BypassTugboatLock.DO_NOT_BYPASS_TUGBOAT_LOCK, emptyList());
    }

    public Tugboat retrieve(List<ShipmentComponent> shipmentComponentsToPreserve) throws TugboatException {
        if (this.engineConfig.getCacheClient() != null) {
            String key = this.engineConfig.getCachePrefix() + TUGBOAT_CACHE_PREFIX + this.cargoId;
            String packageText = this.engineConfig.getCacheClient().get(key);

            if (packageText == null || packageText.trim().isEmpty()) {
                return this;
            }

            try {
                Tugboat cachedTugboat = gson.fromJson(packageText, Tugboat.class);
                this.copyFieldsFrom(cachedTugboat, shipmentComponentsToPreserve);
            } catch (Exception e) {
                throw new TugboatException("Failed to deserialize cached Tugboat data for cargoId: " + this.cargoId, e);
            }
            this.isPackageInitialized = true;
        }
        return this;
    }

    public Tugboat retrieve() throws TugboatException {
        return this.retrieve(emptyList());
    }

    private void retrieveIfUninitialized(List<ShipmentComponent> shipmentComponentsToPreserve) throws TugboatException {
        if (!this.isPackageInitialized) {
            this.retrieve(shipmentComponentsToPreserve);
        }
    }

    private void retrieveIfUninitialized() throws TugboatException {
        retrieveIfUninitialized(emptyList());
    }

    private void save() {
        if (this.engineConfig.getCacheClient() != null) {
            String key = this.engineConfig.getCachePrefix() + TUGBOAT_CACHE_PREFIX + this.cargoId;
            String packageJson = this.toString();
            this.engineConfig.getCacheClient().setEx(key, engineConfig.getCacheExpirySeconds(), packageJson);
        }
    }

    private boolean hasManifestFacilityChanged() {
        int state = this.packageState.getState().ordinal();
        if (this.pickupFacility != null) {
            int currentFacilityHash = this.pickupFacility.toJson().hashCode();
            this.pickupFacility.retrieve();
            if (state == RATED.ordinal()) {
                int newFacilityHash = this.pickupFacility.toJson().hashCode();
                return currentFacilityHash != newFacilityHash;
            } else if (state >= SHOPPED.ordinal() && state < PRINTED.ordinal()) {
                return Optional.ofNullable(this.pickupFacility
                    .getPickupGroupByBatchId(this.manifestBatchId)).isEmpty();
            }
        }
        return false;
    }

    private void rerateIfManifestChanged(List<ShipmentComponent> shipmentComponentsToPreserve) throws TugboatException {
        if (hasManifestFacilityChanged()) {
            State previousState = this.getPackageState().getState();
            if (previousState.equals(PURCHASED)) {
                this.voidLabel(shipmentComponentsToPreserve);
            }
            this.reset();
            this.progressToState(previousState, BypassTugboatLock.BYPASS_TUGBOAT_LOCK);
        }
    }

    private void removeLockAndSave(BypassTugboatLock bypassLock) {
        if (bypassLock.equals(BypassTugboatLock.DO_NOT_BYPASS_TUGBOAT_LOCK)) {
            removeLock(bypassLock);
        }
        this.save();
    }

    private Tugboat progressToState(State targetState, BypassTugboatLock bypassLock) throws TugboatException {
        checkLock(bypassLock);
        if (this.packageState.getState().equals(VOIDED)) {
            this.reset();
        }
        overrideAddressesAndParcel();
        State currentState = this.packageState.getState();

        if (targetState.ordinal() < currentState.ordinal()) {
            throw new TugboatException("Cannot go backwards from " + currentState + " to " + targetState +
                ". Use reset() to start over.");
        } else if (targetState.ordinal() > currentState.ordinal()) {
            setLock(bypassLock);

            try {
                while (currentState.ordinal() < targetState.ordinal()) {
                    switch (currentState) {
                        case INITIAL:
                            if (this.getOptions().getInitialHook() != null) {
                                this.getOptions().getInitialHook().execute(this);
                            }
                            this.packageState = this.packageState.initialize();
                            this.isPackageInitialized = true;
                            break;
                        case INITIALIZED:
                            this.packageState = this.packageState.rate();
                            if (this.getOptions().getRatedHook() != null) {
                                this.getOptions().getRatedHook().execute(this);
                            }
                            break;
                        case RATED:
                            this.packageState = this.packageState.shop();
                            if (this.getOptions().getShoppedHook() != null) {
                                this.getOptions().getShoppedHook().execute(this);
                            }
                            break;
                        case SHOPPED:
                            this.packageState = this.packageState.purchase();
                            if (this.getOptions().getPurchasedHook() != null) {
                                this.getOptions().getPurchasedHook().execute(this);
                            }
                            break;
                        case PURCHASED:
                            // print
                        default:
                            throw new TugboatException("Cannot progress beyond " + currentState);
                    }
                    currentState = this.packageState.getState();
                    removeLockAndSave(bypassLock);
                }
            } catch (Exception e) {
                removeLock(bypassLock);
                throw e;
            }
        }  // else no change, return state as-is

        return this;
    }

    public void reset() throws TugboatException {
        if (!this.isPackageInitialized) {
            this.retrieve();
        }
        if (this.getPackageState() != null &&
            (this.packageState.getState().ordinal() < PURCHASED.ordinal() ||
            this.packageState.getState().equals(VOIDED))) {

            this.shipmentRateResponses = null;
            this.orderRateResponses = null;
            this.rates = null;
            this.selectedRate = null;
            this.manifestBatchId = null;
            this.metadata = null;
            this.postageLabels = null;
            this.trackingCodes = null;
            this.reference = null;

            this.packageState = new InitialTugboatState(this);
        } else {
            throw new TugboatException("cannot reset from purchased or printed state. void first.");
        }
    }

    private void copyFieldsFrom(Tugboat other, List<ShipmentComponent> shipmentComponentsToPreserve) {
        this.cargoId = other.cargoId;
        this.expectedDeliveryDate = other.expectedDeliveryDate;
        if (this.options != null && other.options != null) {
            this.options.mergeFrom(other.options); // preserve user-provided options
        } else if (this.options == null && other.options != null) {
            this.options = other.options;
        }
        this.manifestBatchId = other.manifestBatchId;
        this.pickupFacility = other.pickupFacility;
        if (this.pickupFacility != null) {
            this.pickupFacility.setEngineConfig(this.engineConfig);
        }
        if (this.originAddress == null || !shipmentComponentsToPreserve.contains(ORIGIN_ADDRESS)) {
            this.originAddress = other.originAddress;
        }
        if (this.destinationAddress == null || !shipmentComponentsToPreserve.contains(DESTINATION_ADDRESS)) {
            this.destinationAddress = other.destinationAddress;
        }
        if (this.returnAddress == null || !shipmentComponentsToPreserve.contains(RETURN_ADDRESS)) {
            this.returnAddress = other.returnAddress;
        }
        if (this.parcels == null || this.parcels.isEmpty() || !shipmentComponentsToPreserve.contains(PARCELS)) {
            this.parcels = other.parcels;
        }
        this.shipmentRateResponses = other.shipmentRateResponses;
        this.orderRateResponses = other.orderRateResponses;
        this.rates = other.rates;
        this.selectedRate = other.selectedRate;
        this.postageLabels = other.postageLabels;
        this.trackingCodes = other.trackingCodes;

        this.metadata = other.metadata;
        this.reference = other.reference;
        this.lock = other.lock;

        State stateToRestore = other.packageState.getState();
        this.packageState = TugboatStateBase.createState(stateToRestore, this);
        if (!this.packageState.getState().equals(INITIAL)) {
            this.isPackageInitialized = true;
        }
        this.shippingClientKey = other.shippingClientKey;
    }

    /**
     * if the user provided addresses or parcels, use those instead of the ones that may be in the cache
     */
    private void overrideAddressesAndParcel() {
        State packageState = this.packageState.getState();
        if ((packageState.ordinal() < RATED.ordinal() || packageState.equals(VOIDED)) && this.builder != null) {
            if (this.builder.originAddress != null) {
                this.originAddress = this.builder.originAddress;
            }
            if (this.builder.destinationAddress != null) {
                this.destinationAddress = this.builder.destinationAddress;
            }
            if (this.builder.returnAddress != null) {
                this.returnAddress = this.builder.returnAddress;
            }
            if (this.builder.parcels != null) {
                this.parcels = this.builder.parcels;
            }
            if (this.options != null) {
                this.options = builder.options;
            }
        }
        this.builder = null;
    }

    /**
     * The cargo lock lives in the cache, so it is only available when a cache
     * client is configured. Without one there is nothing to coordinate through
     * and the lock methods no-op.
     */
    private boolean isLockable(BypassTugboatLock bypassLock) {
        return bypassLock.equals(BypassTugboatLock.DO_NOT_BYPASS_TUGBOAT_LOCK)
            && this.engineConfig.getCacheClient() != null;
    }

    private void setLock(BypassTugboatLock bypassLock) {
        if (isLockable(bypassLock)) {
            String key = this.engineConfig.getCachePrefix() + TUGBOAT_LOCK_CACHE_PREFIX + this.cargoId;
            int lockTimeout = 30;
            this.lock = Instant.now().plus(lockTimeout, ChronoUnit.SECONDS);
            String lockString = Long.toString(this.lock.toEpochMilli());
            this.engineConfig.getCacheClient().setEx(key, lockTimeout + 1, lockString);
        }
    }

    private void removeLock(BypassTugboatLock bypassLock) {
        if (isLockable(bypassLock) && this.lock != null) {
            String key = this.engineConfig.getCachePrefix() + TUGBOAT_LOCK_CACHE_PREFIX + this.cargoId;
            this.lock = null;
            this.engineConfig.getCacheClient().del(key);
        }
    }

    private void checkLock(BypassTugboatLock bypassLock) throws TugboatException {
        if (isLockable(bypassLock)) {
            String key = this.engineConfig.getCachePrefix() + TUGBOAT_LOCK_CACHE_PREFIX + this.cargoId;
            String lockString = this.engineConfig.getCacheClient().get(key);
            if (lockString != null && !lockString.isEmpty()) {
                this.lock = Instant.ofEpochMilli(Long.parseLong(lockString));
            }
            if (this.lock != null && Instant.now().isBefore(this.lock)) {
                throw new TugboatException("Tugboat locked.  Please try again in 30 seconds.");
            }
        }
    }

    public IShipment getSelectedShipment() {
        if (this.selectedRate != null) {
            String shipmentId = this.selectedRate.getShipmentId();
            if (this.parcels.size() > 1) {
                return this.orderRateResponses.stream()
                    .flatMap(order -> order.getShipments().stream())
                    .filter(shipment -> shipment.getId().equals(shipmentId))
                    .findFirst()
                    .orElse(null);
            } else {
                return this.shipmentRateResponses.stream()
                    .filter(shipment -> shipment.getId().equals(shipmentId))
                    .findFirst()
                    .orElse(null);
            }
        } else return null;
    }

    public IOrder getSelectedOrder() {
        if (this.selectedRate != null) {
            String shipmentId = this.selectedRate.getShipmentId();
            return this.orderRateResponses.stream()
                .filter(order -> order.getShipments().stream().anyMatch(shipment -> shipment.getId().equals(shipmentId)))
                .findFirst()
                .orElse(null);
        } else return null;
    }

    public EngineConfig getEngineConfig() {
        return engineConfig;
    }

    public void setEngineConfig(EngineConfig engineConfig) {
        this.engineConfig = engineConfig;
        if (this.pickupFacility != null) {
            this.pickupFacility.setEngineConfig(this.engineConfig);
        }
    }

    public TugboatStateBase getPackageState() {
        return packageState;
    }

    public void setPackageState(TugboatStateBase packageState) {
        this.packageState = packageState;
    }

    public Instant getLock() {
        return lock;
    }

    @Override
    public String toString() {
        return gson.toJson(this);
    }

    public JsonElement toJson() {
        return gson.toJsonTree(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Tugboat that = (Tugboat) o;
        return Objects.equals(getCargoId(), that.getCargoId()) && Objects.equals(getExpectedDeliveryDate(), that.getExpectedDeliveryDate()) && Objects.equals(getManifestBatchId(), that.getManifestBatchId()) && Objects.equals(getOriginAddress(), that.getOriginAddress()) && Objects.equals(getDestinationAddress(), that.getDestinationAddress()) && Objects.equals(getReturnAddress(), that.getReturnAddress()) && Objects.equals(getParcels(), that.getParcels()) && Objects.equals(getShipmentRateResponses(), that.getShipmentRateResponses()) && Objects.equals(getOrderRateResponses(), that.getOrderRateResponses()) && Objects.equals(getPackageState(), that.getPackageState()) && Objects.equals(getOptions(), that.getOptions()) && Objects.equals(getLock(), that.getLock());
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(getCargoId());
        result = 31 * result + Objects.hashCode(getExpectedDeliveryDate());
        result = 31 * result + Objects.hashCode(getManifestBatchId());
        result = 31 * result + Objects.hashCode(getOriginAddress());
        result = 31 * result + Objects.hashCode(getDestinationAddress());
        result = 31 * result + Objects.hashCode(getReturnAddress());
        result = 31 * result + Objects.hashCode(getParcels());
        result = 31 * result + Objects.hashCode(getShipmentRateResponses());
        result = 31 * result + Objects.hashCode(getOrderRateResponses());
        result = 31 * result + Objects.hashCode(getPackageState());
        result = 31 * result + Objects.hashCode(getOptions());
        result = 31 * result + Objects.hashCode(getLock());
        return result;
    }
}
