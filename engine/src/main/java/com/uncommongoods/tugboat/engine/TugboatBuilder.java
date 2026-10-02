// Copyright (c) 2025 Uncommon Goods LLC
// SPDX-License-Identifier: MPL-2.0

package com.uncommongoods.tugboat.engine;

import com.uncommongoods.tugboat.engine.ports.shipping.model.IAddress;
import com.uncommongoods.tugboat.engine.ports.shipping.model.IParcel;
import com.uncommongoods.tugboat.engine.config.EngineConfig;
import com.uncommongoods.tugboat.engine.manifest.PickupFacility;
import com.uncommongoods.tugboat.engine.model.TugboatAddress;
import com.uncommongoods.tugboat.engine.model.TugboatParcel;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static com.uncommongoods.tugboat.engine.config.EngineConfig.DEFAULT_CLIENT_KEY;

public class TugboatBuilder {
    final EngineConfig engineConfig;
    final String cargoId;
    LocalDate expectedDeliveryDate;
    PickupFacility pickupFacility;
    IAddress originAddress;
    IAddress destinationAddress;
    IAddress returnAddress;
    List<IParcel> parcels;
    TugboatOptions options = new TugboatOptions();
    String shippingClientKey = DEFAULT_CLIENT_KEY;

    public TugboatBuilder(EngineConfig engineConfig, String cargoId) {
        this.engineConfig = engineConfig;
        this.cargoId = cargoId;
    }

    public TugboatBuilder expectedDeliveryDate(LocalDate expectedDeliveryDate) {
        this.expectedDeliveryDate = expectedDeliveryDate;
        return this;
    }

    public TugboatBuilder pickupFacility(PickupFacility pickupFacility) {
        this.pickupFacility = pickupFacility;
        return this;
    }

    public TugboatBuilder originAddress(Map<String,Object> originAddress) {
        this.originAddress = new TugboatAddress(originAddress);
        return this;
    }

    public TugboatBuilder originAddress(IAddress originAddress) {
        this.originAddress = originAddress;
        return this;
    }

    public TugboatBuilder destinationAddress(Map<String,Object> destinationAddress) {
        this.destinationAddress = new TugboatAddress(destinationAddress);
        return this;
    }

    public TugboatBuilder destinationAddress(IAddress destinationAddress) {
        this.destinationAddress = destinationAddress;
        return this;
    }

    public TugboatBuilder returnAddress(Map<String,Object> returnAddress) {
        this.returnAddress = new TugboatAddress(returnAddress);
        return this;
    }

    public TugboatBuilder returnAddress(IAddress returnAddress) {
        this.returnAddress = returnAddress;
        return this;
    }

    public TugboatBuilder parcel(Map<String,Object>  parcel) {
        this.parcels = List.of(new TugboatParcel(parcel));
        return this;
    }

    public TugboatBuilder parcel(IParcel parcel) {
        this.parcels = List.of(parcel);
        return this;
    }

    public TugboatBuilder parcels(List<IParcel> parcels) {
        this.parcels = parcels;
        return this;
    }

    public TugboatBuilder options(TugboatOptions options) {
        this.options = options;
        return this;
    }

    public TugboatBuilder addresses(Map<String, Object> origin, Map<String, Object> destination, Map<String, Object> returnAddr) {
        this.originAddress = new TugboatAddress(origin);
        this.destinationAddress = new TugboatAddress(destination);
        this.returnAddress = new TugboatAddress(returnAddr);
        return this;
    }

    public TugboatBuilder addresses(TugboatAddress origin, TugboatAddress destination, TugboatAddress returnAddr) {
        this.originAddress = origin;
        this.destinationAddress = destination;
        this.returnAddress = returnAddr;
        return this;
    }

    public TugboatBuilder shippingClientKey(String shippingClientKey) {
        this.shippingClientKey = shippingClientKey;
        return this;
    }

    public Tugboat build() {
        return new Tugboat(this);
    }
}
