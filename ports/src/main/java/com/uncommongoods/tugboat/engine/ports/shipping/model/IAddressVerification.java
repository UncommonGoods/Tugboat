package com.uncommongoods.tugboat.engine.ports.shipping.model;

import java.util.List;

/**
 * Interface for AddressVerification operations
 */
public interface IAddressVerification extends JsonSerializable, Mappable {
    /**
     * Get the success status of the address verification.
     *
     * @return the success status of the address verification
     */
    Boolean getSuccess();

    /**
     * Get the errors of the address verification.
     *
     * @return the errors of the address verification
     */
    List<IAddressVerificationFieldError> getErrors();

    /**
     * Get the details of the address verification.
     *
     * @return the details of the address verification
     */
    IAddressDetail getDetails();
}
