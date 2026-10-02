package com.uncommongoods.tugboat.engine.ports.shipping.model;

import java.util.Map;

/**
 * Interface for CarrierAccount operations
 */
public interface ICarrierAccount extends IEasyPostResource, JsonSerializable {
    /**
     * Get the type of the carrier account.
     *
     * @return the type of the carrier account
     */
    String getType();

    /**
     * Get the fields of the carrier account.
     *
     * @return the fields of the carrier account
     */
    IFields getFields();

    /**
     * Check if the carrier account is a clone.
     *
     * @return true if the carrier account is a clone, otherwise false
     */
    boolean isClone();

    /**
     * Get the logo of the carrier account.
     *
     * @return the logo of the carrier account
     */
    String getLogo();

    /**
     * Get the readable name of the carrier account.
     *
     * @return the readable name of the carrier account
     */
    String getReadable();

    /**
     * Get the description of the carrier account.
     *
     * @return the description of the carrier account
     */
    String getDescription();

    /**
     * Get the reference of the carrier account.
     *
     * @return the reference of the carrier account
     */
    String getReference();

    /**
     * Get the billing type of the carrier account.
     *
     * @return the billing type of the carrier account
     */
    String getBillingType();

    /**
     * Get the credentials of the carrier account.
     *
     * @return the credentials of the carrier account
     */
    Map<String, Object> getCredentials();

    /**
     * Get the test credentials of the carrier account.
     *
     * @return the test credentials of the carrier account
     */
    Map<String, Object> getTestCredentials();
}
