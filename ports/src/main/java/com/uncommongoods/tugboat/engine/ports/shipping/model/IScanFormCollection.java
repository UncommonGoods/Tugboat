package com.uncommongoods.tugboat.engine.ports.shipping.model;

import java.util.List;

/**
 * Interface for ScanFormCollection operations
 */
public interface IScanFormCollection extends IPaginatedCollection<IScanForm> {
    /**
     * Get the scan forms in this collection.
     *
     * @return the scan forms in this collection
     */
    List<IScanForm> getScanForms();
}
