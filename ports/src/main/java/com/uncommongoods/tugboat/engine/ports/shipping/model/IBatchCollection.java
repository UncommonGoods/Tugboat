package com.uncommongoods.tugboat.engine.ports.shipping.model;

import java.util.List;

/**
 * Interface for BatchCollection
 */
public interface IBatchCollection extends IPaginatedCollection<IBatch> {
    /**
     * Get the batches in this collection.
     *
     * @return the batches in this collection
     */
    List<IBatch> getBatches();
}
