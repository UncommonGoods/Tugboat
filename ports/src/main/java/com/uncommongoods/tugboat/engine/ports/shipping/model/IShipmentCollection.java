package com.uncommongoods.tugboat.engine.ports.shipping.model;

import java.util.List;

/**
 * Interface for ShipmentCollection
 */
public interface IShipmentCollection extends IPaginatedCollection<IShipment> {
    /**
     * Get the shipments in this collection.
     *
     * @return the shipments in this collection
     */
    List<IShipment> getShipments();

    /**
     * Get the purchased status filter.
     *
     * @return the purchased status filter
     */
    Boolean getPurchased();

    /**
     * Set the purchased status filter.
     *
     * @param purchased the purchased status filter
     */
    void setPurchased(Boolean purchased);

    /**
     * Get the include children filter.
     *
     * @return the include children filter
     */
    Boolean getIncludeChildren();

    /**
     * Set the include children filter.
     *
     * @param includeChildren the include children filter
     */
    void setIncludeChildren(Boolean includeChildren);
}
