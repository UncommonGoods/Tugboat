package com.uncommongoods.tugboat.engine.ports.shipping.model;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Registers the EasyPost model implementations with the engine.
 *
 * <p>Each of these wraps an EasyPost SDK object and rebuilds it by re-binding the
 * decoded fields to the SDK type, so nested values are handled by the SDK's own
 * mapping and the resolver is not needed here.
 */
public class EasyPostModelTypeProvider implements ModelTypeProvider {

    private static final Set<EntityType> SUPPORTED = EnumSet.allOf(EntityType.class);

    @Override
    public String providerType() {
        return "easypost";
    }

    @Override
    public Set<EntityType> supportedTypes() {
        return SUPPORTED;
    }

    @Override
    public Class<? extends JsonSerializable> modelClass(EntityType type) {
        switch (type) {
            case ADDRESS:       return AddressAdapter.class;
            case PARCEL:        return ParcelAdapter.class;
            case SHIPMENT:      return ShipmentAdapter.class;
            case ORDER:         return OrderAdapter.class;
            case RATE:          return RateAdapter.class;
            case POSTAGE_LABEL: return PostageLabelAdapter.class;
            default:            return null;
        }
    }

    @Override
    public JsonSerializable fromMap(EntityType type, Map<String, Object> map, ModelResolver resolver) {
        switch (type) {
            case ADDRESS:       return new AddressAdapter(map);
            case PARCEL:        return new ParcelAdapter(map);
            case SHIPMENT:      return new ShipmentAdapter(map);
            case ORDER:         return new OrderAdapter(map);
            case RATE:          return new RateAdapter(map);
            case POSTAGE_LABEL: return new PostageLabelAdapter(map);
            default:
                throw new IllegalArgumentException("easypost provider does not handle " + type);
        }
    }
}
