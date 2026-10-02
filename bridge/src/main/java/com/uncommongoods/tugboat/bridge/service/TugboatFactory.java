// Copyright (c) 2025 Uncommon Goods LLC
// SPDX-License-Identifier: MPL-2.0

package com.uncommongoods.tugboat.bridge.service;

import com.uncommongoods.tugboat.bridge.ConfigurationManager;
import com.uncommongoods.tugboat.engine.Tugboat;
import com.uncommongoods.tugboat.engine.TugboatBuilder;
import com.uncommongoods.tugboat.engine.TugboatOptions;
import com.uncommongoods.tugboat.engine.config.EngineConfig;
import com.uncommongoods.tugboat.engine.exception.TugboatException;
import com.uncommongoods.tugboat.engine.manifest.PickupFacility;
import com.uncommongoods.tugboat.engine.model.TugboatAddress;
import com.uncommongoods.tugboat.engine.ports.shipping.model.IParcel;

import java.util.List;

/**
 * Builds Tugboats wired to the active environment: the {@link EngineConfig},
 * {@link TugboatOptions} (hooks included) and {@link PickupFacility} held by
 * {@link ConfigurationManager}.
 */
public class TugboatFactory {

    private final ConfigurationManager configManager;

    public TugboatFactory(ConfigurationManager configManager) {
        this.configManager = configManager;
    }

    /**
     * A builder for {@code cargoId}, pre-wired to the active config, options and
     * origin.
     */
    public TugboatBuilder builderFor(String cargoId) {
        TugboatBuilder builder = Tugboat.builder(configManager.getActiveConfig(), cargoId)
            .options(configManager.getActiveTugboatOptions());

        PickupFacility pickupFacility = configManager.getPickupFacility();
        if (pickupFacility != null) {
            builder.pickupFacility(pickupFacility);
        } else {
            TugboatAddress originAddress = configManager.getOriginAddress();
            if (originAddress != null) {
                builder.originAddress(originAddress);
            }
        }
        return builder;
    }

    public Tugboat load(String cargoId) throws TugboatException {
        Tugboat tugboat = builderFor(cargoId).build();

        tugboat.retrieve();
        ensureHooksApplied(tugboat);

        if (tugboat.getDestinationAddress() == null) {
            tugboat.initialize();
        }

        System.out.println("Successfully loaded tugboat with cargo ID: " + cargoId);
        return tugboat;
    }

    /**
     * Build a fresh Tugboat for {@code cargoId} carrying user-supplied overrides.
     * Null or empty arguments are left off the builder, so the engine
     * fills them from the cache on the next retrieve.
     */
    public Tugboat rebuild(String cargoId, TugboatAddress address, List<IParcel> parcels) {
        TugboatBuilder builder = builderFor(cargoId);

        if (address != null) {
            builder.destinationAddress(address);
        }

        if (parcels != null && !parcels.isEmpty()) {
            builder.parcels(parcels);
        }

        return builder.build();
    }

    /**
     * Re-wire a Tugboat that came from somewhere other than this factory like
     * the direct cache lookup in History.
     */
    public void adopt(Tugboat tugboat) throws TugboatException {
        EngineConfig engineConfig = configManager.getActiveConfig();
        if (engineConfig == null) {
            throw new TugboatException("No active engine config");
        }
        tugboat.setEngineConfig(engineConfig);
        ensureHooksApplied(tugboat);
    }

    /**
     * make sure a Tugboat has the current env's hooks. Hooks are not serialized,
     * so a Tugboat that came out of the cache arrives without them.
     */
    public void ensureHooksApplied(Tugboat tugboat) {
        TugboatOptions currentOptions = tugboat.getOptions();

        if (currentOptions == null) {
            tugboat.setOptions(configManager.getActiveTugboatOptions());
            return;
        }

        boolean hooksMissing = currentOptions.getInitialHook() == null ||
                               currentOptions.getRatedHook() == null ||
                               currentOptions.getShoppedHook() == null ||
                               currentOptions.getPurchasedHook() == null ||
                               currentOptions.getPrintedHook() == null ||
                               currentOptions.getVoidedHook() == null ||
                               currentOptions.getErrorHook() == null;

        if (hooksMissing) {
            currentOptions.mergeFrom(configManager.getActiveTugboatOptions());
        }
    }
}
