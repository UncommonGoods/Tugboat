// Copyright (c) 2025 Uncommon Goods LLC
// SPDX-License-Identifier: MPL-2.0

package com.uncommongoods.tugboat.engine.state;

import com.uncommongoods.tugboat.engine.Tugboat;
import com.uncommongoods.tugboat.engine.ports.shipping.model.IPostageLabel;
import com.uncommongoods.tugboat.engine.exception.TugboatException;

import java.util.List;

public class VoidedTugboatState extends TugboatStateBase {
    public VoidedTugboatState(Tugboat tugboat) {
        super(tugboat);
        this.packageState = State.VOIDED;
    }

    @Override
    public TugboatStateBase initialize() throws TugboatException {
        throw new IllegalStateException("Cannot initialize from voided state. Label has been voided.");
    }

    @Override
    public TugboatStateBase rate() {
        throw new IllegalStateException("Cannot rate from voided state. Label has been voided.");
    }

    @Override
    public TugboatStateBase shop() {
        throw new IllegalStateException("Cannot shop from voided state. Label has been voided.");
    }

    @Override
    public TugboatStateBase purchase() {
        throw new IllegalStateException("Cannot purchase from voided state. Label has been voided.");
    }

    @Override
    public List<IPostageLabel> print() {
        throw new IllegalStateException("Cannot print from voided state. Label has been voided.");
    }

    @Override
    public List<IPostageLabel> reprint() {
        throw new IllegalStateException("Cannot reprint from voided state. Label has been voided.");
    }

    @Override
    public TugboatStateBase voidLabel() {
        throw new IllegalStateException("Cannot void label from voided state. Already voided.");
    }
}
