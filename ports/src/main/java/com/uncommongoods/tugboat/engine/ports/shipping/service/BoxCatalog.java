package com.uncommongoods.tugboat.engine.ports.shipping.service;

import com.uncommongoods.tugboat.engine.ports.shipping.model.BoxSpec;

import java.util.Optional;

/**
 * Resolves a packing box id to the box's dimensions, so a host can swap a
 * shipment's parcel to the box a packer actually used before buying postage.
 *
 * <p>Box catalogs are deployment-specific — the ids come from whatever system
 * the warehouse floor scans against — so implementations are discovered with
 * {@link java.util.ServiceLoader} and registered under
 * {@code META-INF/services/com.uncommongoods.tugboat.engine.ports.shipping.service.BoxCatalog}.
 * A host with no implementation on its classpath simply skips the swap.
 *
 * <p>An implementation that holds resources (a connection pool, say) should
 * also implement {@link AutoCloseable}; hosts release catalogs with an
 * {@code instanceof AutoCloseable} check, so one holding nothing needs no
 * teardown. Implementations must have a public no-arg constructor and should
 * defer acquiring resources until the first {@link #find(int)} call, since
 * {@code ServiceLoader} instantiates every provider it finds — including in
 * hosts that never make a lookup.
 */
public interface BoxCatalog {

    /**
     * Look up a box by id.
     *
     * @param boxId the catalog id of the box
     * @return the box's dimensions, or empty when the id is unknown or the box
     *         is not eligible for a dimension swap (the catalog decides what
     *         "eligible" means — a rigid corrugate box with real dimensions,
     *         for instance, as opposed to a poly mailer)
     */
    Optional<BoxSpec> find(int boxId);
}
