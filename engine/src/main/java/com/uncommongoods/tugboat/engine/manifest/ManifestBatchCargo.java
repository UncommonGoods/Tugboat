// Copyright (c) 2025 Uncommon Goods LLC
// SPDX-License-Identifier: MPL-2.0

package com.uncommongoods.tugboat.engine.manifest;

import com.google.gson.annotations.Expose;
import com.uncommongoods.tugboat.engine.ports.shipping.model.IRate;
import com.uncommongoods.tugboat.engine.ports.shipping.model.IShipment;

import java.util.List;

public class ManifestBatchCargo {
    @Expose
    private String cargoId;
    @Expose
    private String externalPackageId;
    @Expose
    private CarrierService carrierService;
    @Expose
    private List<String> trackingCodes;
    enum Status {
        PENDING, CONFIRMED
    }
    @Expose
    private Status status;

    ManifestBatchCargo(String cargoId, String externalPackageId, CarrierService carrierService, List<String> trackingCodes) {
        this.cargoId = cargoId;
        this.externalPackageId = externalPackageId;
        this.carrierService = carrierService;
        this.trackingCodes = trackingCodes;
        this.status = Status.PENDING;
    }

    ManifestBatchCargo(String cargoId, IRate selectedRate, IShipment shipment) {
        this.cargoId = cargoId;
        this.externalPackageId = selectedRate.getShipmentId();
        this.carrierService = new CarrierService(selectedRate.getCarrier(), selectedRate.getService());
        this.trackingCodes = List.of(shipment.getTrackingCode());
        this.status = Status.PENDING;
    }

    public String getCargoId() {
        return cargoId;
    }

    public void setCargoId(String cargoId) {
        this.cargoId = cargoId;
    }

    public String getExternalPackageId() {
        return externalPackageId;
    }

    public void setExternalPackageId(String externalPackageId) {
        this.externalPackageId = externalPackageId;
    }

    public CarrierService getCarrierService() {
        return carrierService;
    }

    public void setCarrierService(CarrierService carrierService) {
        this.carrierService = carrierService;
    }

    public List<String> getTrackingCodes() {
        return trackingCodes;
    }

    public void setTrackingCodes(List<String> trackingCodes) {
        this.trackingCodes = trackingCodes;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ManifestBatchCargo cargo = (ManifestBatchCargo) o;
        return getCargoId().equals(cargo.getCargoId());
    }

    @Override
    public int hashCode() {
        return getCargoId().hashCode();
    }
}
