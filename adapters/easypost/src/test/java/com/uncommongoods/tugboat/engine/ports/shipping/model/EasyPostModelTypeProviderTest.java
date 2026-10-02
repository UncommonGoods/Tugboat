package com.uncommongoods.tugboat.engine.ports.shipping.model;

import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The engine writes {@link JsonSerializable#getProviderType()} into each model's
 * JSON and looks it back up by the provider's token. If the two ever disagree this
 * adapter would emit JSON nothing could read, so pin them together here.
 */
class EasyPostModelTypeProviderTest {

    private final EasyPostModelTypeProvider provider = new EasyPostModelTypeProvider();

    @Test
    void everyRegisteredTypeReportsTheProvidersToken() {
        for (EntityType type : provider.supportedTypes()) {
            JsonSerializable model =
                provider.fromMap(type, new HashMap<>(), ModelResolvers.within(provider));
            assertEquals(provider.providerType(), model.getProviderType(), type + " model");
        }
    }

    @Test
    void everyRegisteredTypeExposesAModelClassImplementingItsInterface() {
        for (EntityType type : provider.supportedTypes()) {
            Class<? extends JsonSerializable> modelClass = provider.modelClass(type);
            assertNotNull(modelClass, type + " has no model class");
            assertTrue(type.modelInterface().isAssignableFrom(modelClass),
                modelClass + " does not implement " + type.modelInterface());
        }
    }
}
