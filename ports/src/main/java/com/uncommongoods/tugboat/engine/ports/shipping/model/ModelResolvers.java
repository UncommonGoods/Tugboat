package com.uncommongoods.tugboat.engine.ports.shipping.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/** Ready-made {@link ModelResolver}s. */
public final class ModelResolvers {

    private ModelResolvers() {}

    /**
     * A resolver that builds every child from one provider.
     *
     * <p>For rebuilding a model from data that never went through Tugboat's
     * serialization -- a request map, or a vendor response being adapted -- where
     * the whole object graph is known to be this provider's and so carries no type
     * tags to dispatch on.
     */
    public static ModelResolver within(ModelTypeProvider provider) {
        return new WithinProvider(provider);
    }

    private static final class WithinProvider implements ModelResolver {
        private final ModelTypeProvider provider;

        WithinProvider(ModelTypeProvider provider) {
            this.provider = provider;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T extends JsonSerializable> T resolve(EntityType type, Object rawValue) {
            if (!(rawValue instanceof Map)) {
                return null;
            }
            return (T) provider.fromMap(type, (Map<String, Object>) rawValue, this);
        }

        @Override
        public <T extends JsonSerializable> List<T> resolveList(EntityType type, Object rawList) {
            if (!(rawList instanceof List)) {
                return Collections.emptyList();
            }
            List<T> resolved = new ArrayList<>();
            for (Object item : (List<?>) rawList) {
                T model = resolve(type, item);
                if (model != null) {
                    resolved.add(model);
                }
            }
            return resolved;
        }
    }
}
