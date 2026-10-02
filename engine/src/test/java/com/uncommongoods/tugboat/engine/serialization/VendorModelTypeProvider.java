package com.uncommongoods.tugboat.engine.serialization;

import com.uncommongoods.tugboat.engine.ports.shipping.model.EntityType;
import com.uncommongoods.tugboat.engine.ports.shipping.model.JsonSerializable;
import com.uncommongoods.tugboat.engine.ports.shipping.model.ModelResolver;
import com.uncommongoods.tugboat.engine.ports.shipping.model.ModelTypeProvider;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/** Registers {@link VendorModels}, standing in for a third-party adapter jar. */
public class VendorModelTypeProvider implements ModelTypeProvider {

    private static final Set<EntityType> SUPPORTED =
        EnumSet.of(EntityType.ADDRESS, EntityType.RATE, EntityType.SHIPMENT);

    @Override
    public String providerType() {
        return VendorModels.PROVIDER;
    }

    @Override
    public Set<EntityType> supportedTypes() {
        return SUPPORTED;
    }

    @Override
    public Class<? extends JsonSerializable> modelClass(EntityType type) {
        switch (type) {
            case ADDRESS:  return VendorModels.VendorAddress.class;
            case RATE:     return VendorModels.VendorRate.class;
            case SHIPMENT: return VendorModels.VendorShipment.class;
            default:       return null;
        }
    }

    @Override
    public JsonSerializable fromMap(EntityType type, Map<String, Object> map, ModelResolver resolver) {
        switch (type) {
            case ADDRESS:  return new VendorModels.VendorAddress(map);
            case RATE:     return new VendorModels.VendorRate(map);
            case SHIPMENT: return new VendorModels.VendorShipment(map, resolver);
            default:
                throw new IllegalArgumentException("vendor provider does not handle " + type);
        }
    }
}
