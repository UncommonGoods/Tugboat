// Copyright (c) 2025 Uncommon Goods LLC
// SPDX-License-Identifier: MPL-2.0

package com.uncommongoods.tugboat.engine.serialization;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.uncommongoods.tugboat.engine.ports.shipping.model.EntityType;
import com.uncommongoods.tugboat.engine.ports.shipping.model.IAddress;
import com.uncommongoods.tugboat.engine.ports.shipping.model.IOrder;
import com.uncommongoods.tugboat.engine.ports.shipping.model.IParcel;
import com.uncommongoods.tugboat.engine.ports.shipping.model.IPostageLabel;
import com.uncommongoods.tugboat.engine.ports.shipping.model.IRate;
import com.uncommongoods.tugboat.engine.ports.shipping.model.IShipment;
import com.uncommongoods.tugboat.engine.state.TugboatStateBase;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * The Gson configuration Tugboat serializes documents with.
 *
 * <p>Knows how to handle the six polymorphic shipping model kinds, the state
 * machine's current state, and the date types the engine persists. Callers
 * needing extra options -- {@code @Expose} filtering, an exclusion strategy --
 * should layer them with {@code cargo().newBuilder()} rather than assembling a
 * builder from scratch, so model handling stays consistent across documents.
 */
public final class TugboatGson {

    private TugboatGson() {}

    public static Gson cargo() {
        return Holder.INSTANCE;
    }

    /** Holder idiom: initialization is lazy and thread-safe without locking. */
    private static final class Holder {
        static final Gson INSTANCE = new GsonBuilder()
            .registerTypeAdapter(IAddress.class, new JsonSerializableAdapter<IAddress>(EntityType.ADDRESS))
            .registerTypeAdapter(IParcel.class, new JsonSerializableAdapter<IParcel>(EntityType.PARCEL))
            .registerTypeAdapter(IRate.class, new JsonSerializableAdapter<IRate>(EntityType.RATE))
            .registerTypeAdapter(IShipment.class, new JsonSerializableAdapter<IShipment>(EntityType.SHIPMENT))
            .registerTypeAdapter(IPostageLabel.class, new JsonSerializableAdapter<IPostageLabel>(EntityType.POSTAGE_LABEL))
            .registerTypeAdapter(IOrder.class, new JsonSerializableAdapter<IOrder>(EntityType.ORDER))
            .registerTypeAdapter(TugboatStateBase.class, new TugboatStateAdapter())
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .registerTypeAdapter(Date.class, new DateAdapter())
            .registerTypeAdapter(Instant.class, new InstantTypeAdapter())
            .create();
    }
}
