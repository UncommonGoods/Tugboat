package com.uncommongoods.tugboat.engine.ports.shipping.model;

/**
 * Interface for CustomsItem operations
 */
public interface ICustomsItem extends IEasyPostResource {
    /**
     * Get the description of the customs item.
     *
     * @return the description of the customs item
     */
    String getDescription();

    /**
     * Get the HS tariff number of the customs item.
     *
     * @return the HS tariff number of the customs item
     */
    String getHsTariffNumber();

    /**
     * Get the origin country of the customs item.
     *
     * @return the origin country of the customs item
     */
    String getOriginCountry();

    /**
     * Get the quantity of the customs item.
     *
     * @return the quantity of the customs item
     */
    int getQuantity();

    /**
     * Get the value of the customs item.
     *
     * @return the value of the customs item
     */
    Float getValue();

    /**
     * Get the weight of the customs item.
     *
     * @return the weight of the customs item
     */
    Float getWeight();

    /**
     * Get the code of the customs item.
     *
     * @return the code of the customs item
     */
    String getCode();

    /**
     * Get the currency of the customs item.
     *
     * @return the currency of the customs item
     */
    String getCurrency();
}
