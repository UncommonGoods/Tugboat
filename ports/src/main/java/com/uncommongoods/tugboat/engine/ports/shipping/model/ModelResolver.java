package com.uncommongoods.tugboat.engine.ports.shipping.model;

import java.util.List;

/**
 * Materializes nested models on behalf of a {@link ModelTypeProvider}.
 *
 * <p>A shipment holds an address, a parcel, rates and a label. Those children are
 * themselves provider-specific, so a provider rebuilding a shipment must not name
 * their concrete classes: doing so hardcodes the assumption that a shipment's
 * children come from the same provider as the shipment, which is what previously
 * made a mixed-provider object graph impossible to round-trip. Ask the resolver
 * instead and each child is rebuilt as whatever actually produced it.
 *
 * <p>Values passed in are raw decoded JSON, so a nested object arrives as a
 * {@code Map<String, Object>} and a nested array as a {@code List<Object>}.
 */
public interface ModelResolver {

    /**
     * Rebuild one nested model.
     *
     * @param type the kind expected at this position
     * @param rawValue the decoded JSON value, normally a {@code Map}; {@code null}
     *     and JSON null both yield {@code null}
     * @throws IllegalStateException if the value identifies a provider that is not
     *     registered for {@code type}
     */
    <T extends JsonSerializable> T resolve(EntityType type, Object rawValue);

    /**
     * Rebuild a nested list of models. A {@code null} or absent list yields an
     * empty list, never {@code null}, so callers can assign it directly.
     */
    <T extends JsonSerializable> List<T> resolveList(EntityType type, Object rawList);
}
