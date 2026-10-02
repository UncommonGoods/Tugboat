// Copyright (c) 2025 Uncommon Goods LLC
// SPDX-License-Identifier: MPL-2.0

package com.uncommongoods.tugboat.engine.manifest;

import com.google.gson.annotations.Expose;
import java.util.Objects;
import java.util.Set;

public class CarrierServiceGroup {
    @Expose
    private final String readableCarrier;
    @Expose
    private final Set<String> carrierAccountIds;
    @Expose
    private final Set<CarrierService> carrierServices;

    public CarrierServiceGroup(String readableCarrier, Set<String> carrierAccountIds, Set<CarrierService> carrierServices) {
        this.readableCarrier = readableCarrier;
        this.carrierAccountIds = carrierAccountIds;
        this.carrierServices = carrierServices;
    }

    public String getReadableCarrier() {
        return readableCarrier;
    }

    public Set<String> getCarrierAccountIds() {
        return carrierAccountIds;
    }

    public Set<CarrierService> getCarrierServices() {
        return carrierServices;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CarrierServiceGroup that = (CarrierServiceGroup) o;
        return Objects.equals(readableCarrier, that.readableCarrier) &&
               Objects.equals(carrierServices, that.carrierServices);
    }

    @Override
    public int hashCode() {
        return Objects.hash(readableCarrier, carrierServices);
    }
}
