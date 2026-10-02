// Copyright (c) 2025 Uncommon Goods LLC
// SPDX-License-Identifier: MPL-2.0

package com.uncommongoods.tugboat.engine.state;

import com.google.gson.annotations.Expose;
import com.uncommongoods.tugboat.engine.Tugboat;
import com.uncommongoods.tugboat.engine.ports.shipping.model.IPostageLabel;
import com.uncommongoods.tugboat.engine.exception.TugboatException;

import java.util.List;

public abstract class TugboatStateBase implements ITugboatState {
    private final Tugboat tugboat;
    @Expose
    protected State packageState;

    public TugboatStateBase(Tugboat tugboat) {
        this.tugboat = tugboat;
    }

    public Tugboat getTugboat() {
        return tugboat;
    }

    public State getState() {
        return packageState;
    }

    @Override
    public abstract TugboatStateBase initialize() throws TugboatException, IllegalStateException;

    @Override
    public abstract TugboatStateBase rate() throws TugboatException, IllegalStateException;

    @Override
    public abstract TugboatStateBase shop() throws TugboatException, IllegalStateException;

    @Override
    public abstract TugboatStateBase purchase() throws TugboatException, IllegalStateException;

    @Override
    public abstract List<IPostageLabel> print() throws TugboatException, IllegalStateException;

    @Override
    public abstract List<IPostageLabel> reprint() throws TugboatException, IllegalStateException;

    @Override
    public abstract TugboatStateBase voidLabel() throws TugboatException, IllegalStateException;

    /**
     * Factory method to create the appropriate state instance based on State enum
     */
    public static TugboatStateBase createState(State state, Tugboat pckg) {
        if (state == null) {
            return new InitialTugboatState(pckg);
        }

        return switch (state) {
            case INITIAL -> new InitialTugboatState(pckg);
            case INITIALIZED -> new InitializedTugboatState(pckg);
            case RATED -> new RatedTugboatState(pckg);
            case SHOPPED -> new ShoppedTugboatState(pckg);
            case PURCHASED -> new PurchasedTugboatState(pckg);
            case PRINTED -> new PrintedTugboatState(pckg);
            case VOIDED -> new VoidedTugboatState(pckg);
        };
    }
}
