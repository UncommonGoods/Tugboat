// Copyright (c) 2025 Uncommon Goods LLC
// SPDX-License-Identifier: MPL-2.0

package com.uncommongoods.tugboat.engine.state;

import com.uncommongoods.tugboat.engine.Tugboat;
import com.uncommongoods.tugboat.engine.ports.shipping.model.*;
import com.uncommongoods.tugboat.engine.exception.TugboatException;

import java.util.List;

public class PurchasedTugboatState extends TugboatStateBase {
    public PurchasedTugboatState(Tugboat tugboat) {
        super(tugboat);
        this.packageState = State.PURCHASED;
    }

    @Override
    public TugboatStateBase initialize() throws TugboatException {
        throw new IllegalStateException("Cannot initialize from purchased state. Already past initialization.");
    }

    @Override
    public TugboatStateBase rate() {
        throw new IllegalStateException("Cannot rate from purchased state. Already rated.");
    }

    @Override
    public TugboatStateBase shop() {
        throw new IllegalStateException("Cannot shop from purchased state. Already shopped.");
    }

    @Override
    public TugboatStateBase purchase() {
        throw new IllegalStateException("Cannot purchase from purchased state. Already purchased.");
    }

    @Override
    public List<IPostageLabel> print() throws TugboatException {
        Tugboat tugboat = getTugboat();
        tugboat.setPackageState(new PrintedTugboatState(tugboat));
        if (tugboat.getPickupFacility() != null) {
            tugboat.getPickupFacility().markManifestBatchCargoConfirmed(tugboat);
        }
        return tugboat.getPostageLabels();
    }

    @Override
    public List<IPostageLabel> reprint() {
        throw new IllegalStateException("Cannot reprint from purchased state. Must print first.");
    }

    @Override
    public TugboatStateBase voidLabel() throws TugboatException {
        Utils.voidShipment(this.getTugboat());
        if (this.getTugboat().getPickupFacility() != null) {
            this.getTugboat().getPickupFacility().removeTugboatFromGroup(this.getTugboat());
        }
        return new VoidedTugboatState(this.getTugboat());
    }
}
