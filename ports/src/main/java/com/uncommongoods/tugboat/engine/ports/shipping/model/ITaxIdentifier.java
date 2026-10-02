package com.uncommongoods.tugboat.engine.ports.shipping.model;

/**
 * Interface for TaxIdentifier operations
 */
public interface ITaxIdentifier extends IEasyPostResource {
    /**
     * Get the entity type.
     *
     * @return the entity type
     */
    String getEntity();

    /**
     * Get the tax identifier.
     *
     * @return the tax identifier
     */
    String getTaxId();

    /**
     * Get the tax identifier type.
     *
     * @return the tax identifier type
     */
    String getTaxIdType();

    /**
     * Get the issuing country.
     *
     * @return the issuing country
     */
    String getIssuingCountry();
}
