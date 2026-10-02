package com.uncommongoods.tugboat.engine.ports.shipping.model;

/**
 * Interface for AddressVerificationFieldError operations
 */
public interface IAddressVerificationFieldError {
    /**
     * Get the message of the address verification field error.
     *
     * @return the message of the address verification field error
     */
    String getMessage();

    /**
     * Get the code of the address verification field error.
     *
     * @return the code of the address verification field error
     */
    String getCode();

    /**
     * Get the field of the address verification field error.
     *
     * @return the field of the address verification field error
     */
    String getField();

    /**
     * Get the suggestion of the address verification field error.
     *
     * @return the suggestion of the address verification field error
     */
    String getSuggestion();
}
