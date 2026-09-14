package com.uncommongoods.tugboat.engine.ports.shipping.model;

/**
 * The dimensions of a named box from a {@link
 * com.uncommongoods.tugboat.engine.ports.shipping.service.BoxCatalog}.
 *
 * <p>Dimensions are in the same unit as {@link IParcel}, so a spec can be
 * compared against a parcel's length/width/height directly.
 *
 * @param name   the box's display name, for logging
 * @param length the box's length
 * @param width  the box's width
 * @param height the box's height
 */
public record BoxSpec(String name, float length, float width, float height) {
}
