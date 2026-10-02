package com.uncommongoods.tugboat.engine.ports.shipping.model;

/**
 * The shipping model kinds that Tugboat serializes polymorphically.
 *
 * <p>Each constant pairs the name used on the wire with the port interface that
 * every implementation of that kind must satisfy. Both halves are load-bearing:
 * {@link #wireName()} is the key an adapter registers under, and
 * {@link #modelInterface()} lets the engine reject a provider that registers a
 * class under the wrong kind.
 *
 * <p>These names used to be bare string literals repeated in three places (the
 * engine's built-in type switch, one adapter class per kind, and the keys of
 * {@code adapter-types.properties}). Adding a constant here is the only place a
 * new kind should ever be named.
 */
public enum EntityType {
    ADDRESS("address", IAddress.class),
    PARCEL("parcel", IParcel.class),
    SHIPMENT("shipment", IShipment.class),
    ORDER("order", IOrder.class),
    RATE("rate", IRate.class),
    POSTAGE_LABEL("postageLabel", IPostageLabel.class);

    /**
     * JSON key carrying the provider token that identifies which implementation
     * produced an object. Written by the engine on every serialized model and
     * consumed on the way back in.
     */
    public static final String TYPE_KEY = "_type";

    private final String wireName;
    private final Class<? extends JsonSerializable> modelInterface;

    EntityType(String wireName, Class<? extends JsonSerializable> modelInterface) {
        this.wireName = wireName;
        this.modelInterface = modelInterface;
    }

    /** The name this kind is known by on the wire, e.g. {@code "postageLabel"}. */
    public String wireName() {
        return wireName;
    }

    /** The port interface every implementation of this kind must implement. */
    public Class<? extends JsonSerializable> modelInterface() {
        return modelInterface;
    }

    /**
     * @throws IllegalArgumentException if no kind uses that wire name; callers
     *     get the valid set in the message rather than a silent miss.
     */
    public static EntityType fromWireName(String wireName) {
        for (EntityType type : values()) {
            if (type.wireName.equals(wireName)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown entity type '" + wireName + "'");
    }
}
