package com.uncommongoods.tugboat.engine.serialization;

import com.uncommongoods.tugboat.engine.ports.shipping.model.*;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Registration validation.
 *
 * <p>A misregistered provider used to be invisible: a wrong class or a duplicate
 * token quietly overwrote an entry, and the damage only showed up later as a model
 * coming back wrong. These are startup-time failures now.
 */
class ModelTypeRegistryTest {

    @Test
    void discoversProvidersDeclaredInServiceFiles() {
        ModelTypeRegistry registry = ModelTypeRegistry.get();

        assertTrue(registry.registeredTokens(EntityType.ADDRESS).contains("tugboat"));
        assertTrue(registry.registeredTokens(EntityType.ADDRESS).contains("vendor"));
        assertTrue(registry.find(EntityType.ADDRESS, "vendor").isPresent());
    }

    @Test
    void rejectsAClassThatDoesNotImplementItsEntityTypesInterface() {
        // Registers an address class under PARCEL.
        ModelTypeProvider wrong = new StubProvider("wrong", EnumSet.of(EntityType.PARCEL)) {
            @Override
            public Class<? extends JsonSerializable> modelClass(EntityType type) {
                return VendorModels.VendorAddress.class;
            }
        };

        IllegalStateException thrown = assertThrows(IllegalStateException.class,
            () -> ModelTypeRegistry.of(List.of(wrong)));

        assertTrue(thrown.getMessage().contains("does not implement"), thrown.getMessage());
    }

    @Test
    void rejectsTwoProvidersClaimingTheSameTokenForOneEntityType() {
        ModelTypeProvider first = new StubProvider("clash", EnumSet.of(EntityType.ADDRESS));
        ModelTypeProvider second = new StubProvider("clash", EnumSet.of(EntityType.ADDRESS));

        IllegalStateException thrown = assertThrows(IllegalStateException.class,
            () -> ModelTypeRegistry.of(List.of(first, second)));

        assertTrue(thrown.getMessage().contains("clash"), thrown.getMessage());
    }

    @Test
    void allowsTheSameTokenAcrossDifferentEntityTypes() {
        ModelTypeProvider provider =
            new StubProvider("fine", EnumSet.of(EntityType.ADDRESS)) {
                @Override
                public Set<EntityType> supportedTypes() {
                    return EnumSet.of(EntityType.ADDRESS, EntityType.RATE);
                }

                @Override
                public Class<? extends JsonSerializable> modelClass(EntityType type) {
                    return type == EntityType.RATE
                        ? VendorModels.VendorRate.class
                        : VendorModels.VendorAddress.class;
                }
            };

        ModelTypeRegistry registry = ModelTypeRegistry.of(List.of(provider));

        assertTrue(registry.find(EntityType.ADDRESS, "fine").isPresent());
        assertTrue(registry.find(EntityType.RATE, "fine").isPresent());
    }

    @Test
    void rejectsABlankProviderToken() {
        assertThrows(IllegalStateException.class,
            () -> ModelTypeRegistry.of(List.of(new StubProvider("  ", EnumSet.of(EntityType.ADDRESS)))));
    }

    @Test
    void rejectsAnEntityTypeWithNoModelClass() {
        ModelTypeProvider incomplete = new StubProvider("gap", EnumSet.of(EntityType.ADDRESS)) {
            @Override
            public Class<? extends JsonSerializable> modelClass(EntityType type) {
                return null;
            }
        };

        IllegalStateException thrown = assertThrows(IllegalStateException.class,
            () -> ModelTypeRegistry.of(List.of(incomplete)));

        assertTrue(thrown.getMessage().contains("modelClass"), thrown.getMessage());
    }

    private static class StubProvider implements ModelTypeProvider {
        private final String token;
        private final Set<EntityType> types;

        StubProvider(String token, Set<EntityType> types) {
            this.token = token;
            this.types = types;
        }

        @Override public String providerType() { return token; }
        @Override public Set<EntityType> supportedTypes() { return types; }

        @Override
        public Class<? extends JsonSerializable> modelClass(EntityType type) {
            return VendorModels.VendorAddress.class;
        }

        @Override
        public JsonSerializable fromMap(EntityType type, Map<String, Object> map, ModelResolver resolver) {
            return new VendorModels.VendorAddress(map);
        }
    }
}
