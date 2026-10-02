package com.uncommongoods.tugboat.engine.serialization;

import com.uncommongoods.tugboat.engine.TugboatTest;
import com.uncommongoods.tugboat.engine.manifest.PickupFacilityTest;
import com.uncommongoods.tugboat.engine.ports.shipping.model.EntityType;
import com.uncommongoods.tugboat.engine.ports.shipping.model.JsonSerializable;
import com.uncommongoods.tugboat.engine.ports.shipping.model.ModelResolver;
import com.uncommongoods.tugboat.engine.ports.shipping.model.ModelTypeProvider;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/** Registers the doubles the engine's own tests serialize, under {@code "test"}. */
public class TestModelTypeProvider implements ModelTypeProvider {

    private static final Set<EntityType> SUPPORTED =
        EnumSet.of(EntityType.ADDRESS, EntityType.SHIPMENT, EntityType.ORDER);

    @Override
    public String providerType() {
        return "test";
    }

    @Override
    public Set<EntityType> supportedTypes() {
        return SUPPORTED;
    }

    @Override
    public Class<? extends JsonSerializable> modelClass(EntityType type) {
        switch (type) {
            case ADDRESS:  return PickupFacilityTest.TestAddress.class;
            case SHIPMENT: return TugboatTest.TestShipment.class;
            case ORDER:    return TugboatTest.TestOrder.class;
            default:       return null;
        }
    }

    @Override
    public JsonSerializable fromMap(EntityType type, Map<String, Object> map, ModelResolver resolver) {
        switch (type) {
            case ADDRESS:  return new PickupFacilityTest.TestAddress(map);
            case SHIPMENT: return new TugboatTest.TestShipment(map);
            case ORDER:    return new TugboatTest.TestOrder(map);
            default:
                throw new IllegalArgumentException("test provider does not handle " + type);
        }
    }
}
