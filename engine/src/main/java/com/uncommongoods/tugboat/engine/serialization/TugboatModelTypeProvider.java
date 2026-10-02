// Copyright (c) 2025 Uncommon Goods LLC
// SPDX-License-Identifier: MPL-2.0

package com.uncommongoods.tugboat.engine.serialization;

import com.uncommongoods.tugboat.engine.model.TugboatAddress;
import com.uncommongoods.tugboat.engine.model.TugboatParcel;
import com.uncommongoods.tugboat.engine.ports.shipping.model.EntityType;
import com.uncommongoods.tugboat.engine.ports.shipping.model.JsonSerializable;
import com.uncommongoods.tugboat.engine.ports.shipping.model.ModelResolver;
import com.uncommongoods.tugboat.engine.ports.shipping.model.ModelTypeProvider;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Registers the engine's own carrier-neutral models.
 *
 * <p>These are built in, but they are not special-cased: they arrive through the
 * same {@link ModelTypeProvider} service declaration an adapter module uses, so
 * there is exactly one registration path to understand.
 */
public class TugboatModelTypeProvider implements ModelTypeProvider {

    private static final Set<EntityType> SUPPORTED =
        EnumSet.of(EntityType.ADDRESS, EntityType.PARCEL);

    @Override
    public String providerType() {
        return "tugboat";
    }

    @Override
    public Set<EntityType> supportedTypes() {
        return SUPPORTED;
    }

    @Override
    public Class<? extends JsonSerializable> modelClass(EntityType type) {
        switch (type) {
            case ADDRESS:
                return TugboatAddress.class;
            case PARCEL:
                return TugboatParcel.class;
            default:
                return null;
        }
    }

    @Override
    public JsonSerializable fromMap(EntityType type, Map<String, Object> map, ModelResolver resolver) {
        switch (type) {
            case ADDRESS:
                return new TugboatAddress(map);
            case PARCEL:
                return new TugboatParcel(map);
            default:
                throw new IllegalArgumentException("tugboat provider does not handle " + type);
        }
    }
}
