package com.uncommongoods.tugboat.engine.ports.shipping.model;

/**
 * Interface for Fee operations
 */
public interface IFee {
    /**
     * Get the fee type.
     *
     * @return the fee type
     */
    String getType();

    /**
     * Get the fee amount.
     *
     * @return the fee amount
     */
    float getAmount();

    /**
     * Get the fee charged flag.
     *
     * @return true if charged, false otherwise
     */
    Boolean getCharged();

    /**
     * Get the fee refunded flag.
     *
     * @return true if refunded, false otherwise
     */
    Boolean getRefunded();
}
