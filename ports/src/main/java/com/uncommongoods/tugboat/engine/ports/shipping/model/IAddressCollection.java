package com.uncommongoods.tugboat.engine.ports.shipping.model;

import java.util.List;

/**
 * Interface for AddressCollection
 */
public interface IAddressCollection extends IPaginatedCollection<IAddress> {
    /**
     * Get the addresses in this collection.
     *
     * @return the addresses in this collection
     */
    List<IAddress> getAddresses();
}
