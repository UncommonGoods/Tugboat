// Copyright (c) 2025 Uncommon Goods LLC
// SPDX-License-Identifier: MPL-2.0

package com.uncommongoods.tugboat.engine;

import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import com.uncommongoods.tugboat.engine.ports.shipping.model.*;
import com.uncommongoods.tugboat.engine.manifest.PickupFacility;

import java.time.LocalDate;
import java.util.List;

import static com.uncommongoods.tugboat.engine.config.EngineConfig.DEFAULT_CLIENT_KEY;

/**
 * Base data class is here to de-clutter the Tugboat class and keep its focus on state transitions
 */
abstract class TugboatBase {
    @Expose
    String cargoId;
    @Expose
    String reference;
    @Expose
    LocalDate expectedDeliveryDate;
    @Expose
    String manifestBatchId;
    @Expose
    PickupFacility pickupFacility;
    @Expose
    IAddress originAddress;
    @Expose
    IAddress destinationAddress;
    @Expose
    IAddress returnAddress;
    @Expose
    List<IParcel> parcels;
    @Expose
    List<IShipment> shipmentRateResponses;
    @Expose
    List<IOrder> orderRateResponses;
    @Expose
    List<IRate> rates;
    @Expose
    IRate selectedRate;
    @Expose
    List<IPostageLabel> postageLabels;
    @Expose
    List<String> trackingCodes;
    @Expose
    TugboatOptions options = new TugboatOptions();
    @Expose
    JsonObject metadata;
    @Expose
    String shippingClientKey = DEFAULT_CLIENT_KEY;


    TugboatBase(String cargoId, LocalDate expectedDeliveryDate, PickupFacility pickupFacility,
                         IAddress originAddress, IAddress destinationAddress, IAddress returnAddress,
                         List<IParcel> parcels, TugboatOptions options, String shippingClientKey) {
        this.cargoId = cargoId;
        this.expectedDeliveryDate = expectedDeliveryDate;
        this.pickupFacility = pickupFacility;
        this.originAddress = originAddress;
        this.destinationAddress = destinationAddress;
        this.returnAddress = returnAddress;
        this.parcels = parcels;
        this.options = options != null ? options : new TugboatOptions();
        this.shippingClientKey = shippingClientKey;
    }

    TugboatBase() {
    }

    public String getCargoId() {
        return cargoId;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public LocalDate getExpectedDeliveryDate() {
        return expectedDeliveryDate;
    }

    public void setExpectedDeliveryDate(LocalDate expectedDeliveryDate) {
        this.expectedDeliveryDate = expectedDeliveryDate;
    }

    public String getManifestBatchId() {
        return manifestBatchId;
    }

    public void setManifestBatchId(String manifestBatchId) {
        this.manifestBatchId = manifestBatchId;
    }

    public PickupFacility getPickupFacility() {
        return pickupFacility;
    }

    public IAddress getOriginAddress() {
        return originAddress;
    }

    public void setOriginAddress(IAddress originAddress) {
        this.originAddress = originAddress;
    }

    public IAddress getDestinationAddress() {
        return destinationAddress;
    }

    public void setDestinationAddress(IAddress destinationAddress) {
        this.destinationAddress = destinationAddress;
    }

    public IAddress getReturnAddress() {
        return returnAddress;
    }

    public void setReturnAddress(IAddress returnAddress) {
        this.returnAddress = returnAddress;
    }

    public List<IParcel> getParcels() {
        return parcels;
    }

    public void setParcels(List<IParcel> parcels) {
        this.parcels = parcels;
    }

    public List<IShipment> getShipmentRateResponses() {
        return shipmentRateResponses;
    }

    public void setShipmentRateResponses(List<IShipment> shipmentRateResponses) {
        this.shipmentRateResponses = shipmentRateResponses;
    }

    public List<IOrder> getOrderRateResponses() {
        return orderRateResponses;
    }

    public void setOrderRateResponses(List<IOrder> orderRateResponses) {
        this.orderRateResponses = orderRateResponses;
    }

    public List<IRate> getRates() {
        return this.rates;
    }

    public void setRates(List<IRate> rates) {
        this.rates = rates;
    }

    public IRate getSelectedRate() {
        return this.selectedRate;
    }

    public void setSelectedRate(IRate selectedRate) {
        this.selectedRate = selectedRate;
    }

    public List<IPostageLabel> getPostageLabels() {
        return postageLabels;
    }

    public void setPostageLabels(List<IPostageLabel> postageLabels) {
        this.postageLabels = postageLabels;
    }

    public List<String> getTrackingCodes() {
        return trackingCodes;
    }

    public void setTrackingCodes(List<String> trackingCodes) {
        this.trackingCodes = trackingCodes;
    }

    public TugboatOptions getOptions() {
        return options;
    }

    public void setOptions(TugboatOptions options) {
        this.options = options;
    }

    public JsonObject getMetadata() {
        return metadata;
    }

    public void setMetadata(JsonObject metadata) {
        this.metadata = metadata;
    }

    public String getShippingClientKey() {
        return shippingClientKey;
    }

    public void setShippingClientKey(String shippingClientKey) {
        this.shippingClientKey = shippingClientKey;
    }
}
