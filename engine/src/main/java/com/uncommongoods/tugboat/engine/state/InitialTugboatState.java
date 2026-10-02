// Copyright (c) 2025 Uncommon Goods LLC
// SPDX-License-Identifier: MPL-2.0

package com.uncommongoods.tugboat.engine.state;

import com.uncommongoods.tugboat.engine.Tugboat;
import com.uncommongoods.tugboat.engine.ports.shipping.model.*;
import com.uncommongoods.tugboat.engine.exception.TugboatException;
import com.uncommongoods.tugboat.engine.manifest.PickupGroup;

import java.util.*;


public class InitialTugboatState extends TugboatStateBase {
    public InitialTugboatState(Tugboat tugboat) {
        super(tugboat);
        this.packageState = State.INITIAL;
    }

    @Override
    public TugboatStateBase initialize() throws TugboatException {
        Tugboat tugboat = getTugboat();
        validateAddressesAndParcel(tugboat);
        return new InitializedTugboatState(tugboat);
    }

    @Override
    public TugboatStateBase rate() throws TugboatException {
        throw new IllegalStateException("Cannot rate from initial state. Must initialize first.");
    }

    private void validateAddressesAndParcel(Tugboat tugboat) {
        if (tugboat.getPickupFacility() != null) {
            tugboat.setOriginAddress(tugboat.getPickupFacility().getAddress());
            if (tugboat.getOptions().getCarrierAccountIds() == null || tugboat.getOptions().getCarrierAccountIds().isEmpty()) {
                List<String> carrierAccountIds = tugboat.getPickupFacility().getPickupGroups().stream()
                        .map(PickupGroup::getCarrierAccountId)
                        .filter(carrierAccountId -> tugboat.getOptions().getCarrierAccountIds() == null ||
                            tugboat.getOptions().getCarrierAccountIds().contains(carrierAccountId))
                        .toList();
                tugboat.getOptions().setCarrierAccountIds(carrierAccountIds);
            }
        } else if (tugboat.getPickupFacility() == null && tugboat.getOriginAddress() == null) {
            throw new IllegalStateException("An origin address or PickupFacility is required");
        }

        if (tugboat.getDestinationAddress() == null) {
            throw new IllegalStateException("A destination address is required");
        }

        if (tugboat.getReturnAddress() != null) {
            tugboat.setReturnAddress(tugboat.getReturnAddress());
        } else {
            tugboat.setReturnAddress(tugboat.getOriginAddress());
        }

        if (tugboat.getParcels() == null || tugboat.getParcels().isEmpty()) {
            throw new IllegalStateException("A parcel is required");
        }
    }


    @Override
    public TugboatStateBase shop() {
        throw new IllegalStateException("Cannot shop from initial state. Must initialize and rate first.");
    }

    @Override
    public TugboatStateBase purchase() {
        throw new IllegalStateException("Cannot purchase from initial state. Must initialize, rate, and shop first.");
    }

    @Override
    public List<IPostageLabel> print() {
        throw new IllegalStateException("Cannot print from initial state. Must initialize, rate, shop, and purchase first.");
    }

    @Override
    public List<IPostageLabel> reprint() {
        throw new IllegalStateException("Cannot reprint from initial state. Must print first.");
    }

    @Override
    public TugboatStateBase voidLabel() {
        throw new IllegalStateException("Cannot void label from initial state. Must print first.");
    }
}
