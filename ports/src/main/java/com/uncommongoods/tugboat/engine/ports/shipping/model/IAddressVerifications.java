package com.uncommongoods.tugboat.engine.ports.shipping.model;

/**
 * Interface for AddressVerifications operations
 */
public interface IAddressVerifications extends IEasyPostResource, JsonSerializable, Mappable {
    /**
     * Get the zip4 verification of the address verifications.
     *
     * @return the zip4 verification of the address verifications
     */
    IAddressVerification getZip4();

    /**
     * Get the delivery verification of the address verifications.
     *
     * @return the delivery verification of the address verifications
     */
    IAddressVerification getDelivery();
}
