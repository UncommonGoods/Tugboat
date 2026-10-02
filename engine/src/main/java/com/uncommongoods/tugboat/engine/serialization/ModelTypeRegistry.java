// Copyright (c) 2025 Uncommon Goods LLC
// SPDX-License-Identifier: MPL-2.0

package com.uncommongoods.tugboat.engine.serialization;

import com.uncommongoods.tugboat.engine.ports.shipping.model.EntityType;
import com.uncommongoods.tugboat.engine.ports.shipping.model.JsonSerializable;
import com.uncommongoods.tugboat.engine.ports.shipping.model.ModelTypeProvider;

import java.util.Collection;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.TreeSet;

/**
 * Which concrete model class stands behind each (kind, provider) pair.
 *
 * <p>Populated entirely from {@link ModelTypeProvider} implementations discovered
 * with {@link ServiceLoader}, so adding a provider never touches engine source.
 *
 * <p>Registrations are validated when the registry loads rather than when a
 * serialized object first needs them. A provider that claims a kind but hands
 * back a class not implementing that kind's interface, or two providers claiming
 * the same token for the same kind, is a packaging mistake -- surfacing it at
 * startup makes it a five-second fix instead of a data-shaped bug found later.
 */
public final class ModelTypeRegistry {

    private final Map<EntityType, Map<String, ModelTypeProvider>> byType;

    private ModelTypeRegistry(Map<EntityType, Map<String, ModelTypeProvider>> byType) {
        this.byType = byType;
    }

    /** Holder idiom: the JVM makes this lazy and thread-safe for us. */
    private static final class Holder {
        static final ModelTypeRegistry INSTANCE = load();
    }

    public static ModelTypeRegistry get() {
        return Holder.INSTANCE;
    }

    private static ModelTypeRegistry load() {
        // Keyed by class name so a provider reachable twice (a shaded jar next to
        // the original, say) registers once instead of tripping the duplicate check.
        Map<String, ModelTypeProvider> discovered = new LinkedHashMap<>();
        for (ModelTypeProvider provider : ServiceLoader.load(ModelTypeProvider.class)) {
            discovered.put(provider.getClass().getName(), provider);
        }
        return of(discovered.values());
    }

    /** Build a registry from an explicit provider list. Exposed for tests. */
    public static ModelTypeRegistry of(Collection<ModelTypeProvider> providers) {
        Map<EntityType, Map<String, ModelTypeProvider>> byType = new EnumMap<>(EntityType.class);
        for (EntityType type : EntityType.values()) {
            byType.put(type, new LinkedHashMap<>());
        }

        for (ModelTypeProvider provider : providers) {
            String token = provider.providerType();
            if (token == null || token.trim().isEmpty()) {
                throw new IllegalStateException(
                    provider.getClass().getName() + " returned a blank providerType()");
            }
            Set<EntityType> supported = provider.supportedTypes();
            if (supported == null) {
                throw new IllegalStateException(
                    provider.getClass().getName() + " returned null from supportedTypes()");
            }

            for (EntityType type : supported) {
                Class<? extends JsonSerializable> modelClass = provider.modelClass(type);
                if (modelClass == null) {
                    throw new IllegalStateException(
                        provider.getClass().getName() + " lists " + type
                            + " in supportedTypes() but modelClass(" + type + ") is null");
                }
                if (!type.modelInterface().isAssignableFrom(modelClass)) {
                    throw new IllegalStateException(
                        provider.getClass().getName() + " registers " + modelClass.getName()
                            + " for " + type + ", but it does not implement "
                            + type.modelInterface().getName());
                }

                ModelTypeProvider existing = byType.get(type).put(token, provider);
                if (existing != null && existing != provider) {
                    throw new IllegalStateException(
                        "Two providers claim " + type + " for '" + token + "': "
                            + existing.getClass().getName() + " and "
                            + provider.getClass().getName()
                            + ". Provider tokens must be unique per entity type.");
                }
            }
        }

        return new ModelTypeRegistry(byType);
    }

    /** The provider registered for a kind under {@code token}, if any. */
    public Optional<ModelTypeProvider> find(EntityType type, String token) {
        if (type == null || token == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(byType.get(type).get(token));
    }

    /** Tokens registered for a kind, sorted, for use in error messages. */
    public Set<String> registeredTokens(EntityType type) {
        return new TreeSet<>(byType.get(type).keySet());
    }
}
