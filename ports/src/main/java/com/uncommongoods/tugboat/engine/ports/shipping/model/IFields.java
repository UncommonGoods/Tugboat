package com.uncommongoods.tugboat.engine.ports.shipping.model;

import java.util.Map;

/**
 * Interface for Fields operations
 */
public interface IFields extends IEasyPostResource {
    /**
     * Get the credentials of the fields.
     *
     * @return the credentials of the fields
     */
    Map<String, IField> getCredentials();

    /**
     * Get the test credentials of the fields.
     *
     * @return the test credentials of the fields
     */
    Map<String, IField> getTestCredentials();

    /**
     * Check if auto link is enabled.
     *
     * @return true if auto link is enabled, otherwise false
     */
    boolean isAutoLink();

    /**
     * Check if custom workflow is enabled.
     *
     * @return true if custom workflow is enabled, otherwise false
     */
    boolean isCustomWorkflow();
}
