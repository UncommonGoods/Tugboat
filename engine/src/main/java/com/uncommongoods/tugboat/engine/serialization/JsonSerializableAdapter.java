// Copyright (c) 2025 Uncommon Goods LLC
// SPDX-License-Identifier: MPL-2.0

package com.uncommongoods.tugboat.engine.serialization;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.TypeAdapter;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.uncommongoods.tugboat.engine.ports.shipping.model.EntityType;
import com.uncommongoods.tugboat.engine.ports.shipping.model.JsonSerializable;
import com.uncommongoods.tugboat.engine.ports.shipping.model.ModelResolver;
import com.uncommongoods.tugboat.engine.ports.shipping.model.ModelTypeProvider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Reads and writes one kind of shipping model, whichever provider implemented it.
 *
 * <p>Identity travels in the JSON itself, under {@link EntityType#TYPE_KEY}: on the
 * way out the object's {@link JsonSerializable#getProviderType()} is written
 * alongside its fields, and on the way back in that token selects the
 * {@link ModelTypeProvider} that rebuilds it.
 *
 * <p>A token with no registered provider is an error. The previous implementation
 * fell back to trying every registered class until one's constructor did not
 * throw -- and since these constructors read whatever keys they recognize and
 * ignore the rest, one always succeeded, quietly returning some other provider's
 * object with most of its fields dropped.
 *
 * <p>The object's own {@link JsonSerializable#toJson()} decides its shape; only
 * the type tag is added. Models differ in what they render -- some serialize their
 * own fields, the EasyPost ones render the vendor object they wrap -- and that
 * choice belongs to the model.
 *
 * <p>Consequently a nested model is only tagged if its parent chose to render one.
 * When a child carries no tag it is rebuilt using its parent's provider, which is
 * what every current provider means: a shipment's address, parcel and rates come
 * from the same client response the shipment did. A provider that genuinely mixes
 * providers can emit {@link EntityType#TYPE_KEY} on the child and it is honored.
 */
public class JsonSerializableAdapter<T extends JsonSerializable> extends TypeAdapter<T> {

    /** Decodes an object into raw values; no model handling needed for Map. */
    private static final Gson PLAIN = new Gson();

    private final EntityType entityType;

    public JsonSerializableAdapter(EntityType entityType) {
        this.entityType = entityType;
    }

    @Override
    public void write(JsonWriter out, T value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }

        String token = value.getProviderType();
        if (ModelTypeRegistry.get().find(entityType, token).isEmpty()) {
            throw new IllegalStateException(
                value.getClass().getName() + " reports provider type '" + token
                    + "', which is not registered for " + entityType.wireName()
                    + ". Registered: " + ModelTypeRegistry.get().registeredTokens(entityType)
                    + ". A model's getProviderType() must match its ModelTypeProvider's"
                    + " providerType(), or the JSON it writes can never be read back.");
        }

        JsonElement element = value.toJson();
        if (element != null && element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            object.addProperty(EntityType.TYPE_KEY, token);
            Streams.write(object, out);
        } else if (element != null) {
            Streams.write(element, out);
        } else {
            out.nullValue();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public T read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }

        // Cargo written by older builds stored these as embedded JSON strings.
        JsonElement element = in.peek() == JsonToken.STRING
            ? JsonParser.parseString(in.nextString())
            : JsonParser.parseReader(in);

        if (element.isJsonNull()) {
            return null;
        }

        Map<String, Object> map = PLAIN.fromJson(element, Map.class);
        return (T) rebuild(entityType, map, null);
    }

    /**
     * Rebuild one model from its decoded fields.
     *
     * @param parentToken provider of the object this one was nested in, used only
     *     when the value itself carries no tag
     */
    private static JsonSerializable rebuild(EntityType type, Map<String, Object> map, String parentToken) {
        if (map == null) {
            return null;
        }

        Object rawToken = map.get(EntityType.TYPE_KEY);
        // Objects nested inside cargo written before nested tagging have no token
        // of their own. Inheriting the parent's reproduces what those blobs meant
        // when they were written, so they keep loading without a migration.
        String token = rawToken != null ? rawToken.toString() : parentToken;

        ModelTypeRegistry registry = ModelTypeRegistry.get();
        ModelTypeProvider provider = registry.find(type, token).orElseThrow(() ->
            new IllegalStateException(
                "No " + type.wireName() + " model is registered for provider '" + token + "'."
                    + " Registered: " + registry.registeredTokens(type)
                    + ". Adapter modules register their models by implementing"
                    + " ModelTypeProvider and declaring it in"
                    + " META-INF/services/com.uncommongoods.tugboat.engine.ports.shipping.model.ModelTypeProvider."));

        map.remove(EntityType.TYPE_KEY);
        return provider.fromMap(type, map, new NestedResolver(token));
    }

    /** Resolves a model's children, defaulting them to the parent's provider. */
    private static final class NestedResolver implements ModelResolver {
        private final String parentToken;

        NestedResolver(String parentToken) {
            this.parentToken = parentToken;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <C extends JsonSerializable> C resolve(EntityType type, Object rawValue) {
            if (!(rawValue instanceof Map)) {
                return null;
            }
            return (C) rebuild(type, (Map<String, Object>) rawValue, parentToken);
        }

        @Override
        public <C extends JsonSerializable> List<C> resolveList(EntityType type, Object rawList) {
            if (!(rawList instanceof List)) {
                return Collections.emptyList();
            }
            List<C> resolved = new ArrayList<>();
            for (Object item : (List<?>) rawList) {
                C model = resolve(type, item);
                if (model != null) {
                    resolved.add(model);
                }
            }
            return resolved;
        }
    }
}
