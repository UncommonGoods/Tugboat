// Copyright (c) 2025 Uncommon Goods LLC
// SPDX-License-Identifier: MPL-2.0

package com.uncommongoods.tugboat.engine.state;

import com.uncommongoods.tugboat.engine.Tugboat;
import com.uncommongoods.tugboat.engine.ports.shipping.model.IPostageLabel;
import com.uncommongoods.tugboat.engine.exception.TugboatException;

import java.util.List;

public class PrintedTugboatState extends TugboatStateBase {
    public PrintedTugboatState(Tugboat tugboat) {
        super(tugboat);
        this.packageState = State.PRINTED;
    }

    @Override
    public TugboatStateBase initialize() throws TugboatException {
        throw new IllegalStateException("Cannot initialize from printed state. Already past initialization.");
    }

    @Override
    public TugboatStateBase rate() {
        throw new IllegalStateException("Cannot rate from printed state. Already rated.");
    }

    @Override
    public TugboatStateBase shop() {
        throw new IllegalStateException("Cannot shop from printed state. Already shopped.");
    }

    @Override
    public TugboatStateBase purchase() {
        throw new IllegalStateException("Cannot purchase from printed state. Already purchased.");
    }

    @Override
    public List<IPostageLabel> print() {
        throw new IllegalStateException("Cannot print from printed state. Already printed.");
    }

    @Override
    public List<IPostageLabel> reprint() throws TugboatException {
        return this.getTugboat().getPostageLabels();
    }

    @Override
    public TugboatStateBase voidLabel() throws TugboatException {
        Utils.voidShipment(this.getTugboat());
        return new VoidedTugboatState(this.getTugboat());
    }
}
