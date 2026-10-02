package com.uncommongoods.tugboat.engine.ports.shipping.model;

import java.util.Date;

/**
 * Interface for Address operations to make it easier to create custom implementations
 */
public interface IAddress extends IEasyPostResource, JsonSerializable, Mappable {
    /**
     * Get the ID of the address.
     *
     * @return the ID of the address
     */
    String getId();

    /**
     * Get the mode of the address.
     *
     * @return the mode of the address
     */
    String getMode();

    /**
     * Get the object type of the address.
     *
     * @return the object type of the address
     */
    String getObject();

    /**
     * Get the creation date of the address.
     *
     * @return the creation date of the address
     */
    Date getCreatedAt();

    /**
     * Get the last update date of the address.
     *
     * @return the last update date of the address
     */
    Date getUpdatedAt();

    /**
     * Get the name on the address.
     *
     * @return the name on the address
     */
    String getName();

    /**
     * Get the company name on the address.
     *
     * @return the company name on the address
     */
    String getCompany();

    /**
     * Get the first line of the street address.
     *
     * @return the first line of the street address
     */
    String getStreet1();

    /**
     * Get the second line of the street address.
     *
     * @return the second line of the street address
     */
    String getStreet2();

    /**
     * Get the city of the address.
     *
     * @return the city of the address
     */
    String getCity();

    /**
     * Get the state or province of the address.
     *
     * @return the state or province of the address
     */
    String getState();

    /**
     * Get the postal code of the address.
     *
     * @return the postal code of the address
     */
    String getZip();

    /**
     * Get the country code of the address.
     *
     * @return the country code of the address
     */
    String getCountry();

    /**
     * Get the phone number associated with the address.
     *
     * @return the phone number associated with the address
     */
    String getPhone();

    /**
     * Get the email associated with the address.
     *
     * @return the email associated with the address
     */
    String getEmail();

    /**
     * Get any message related to the address, often validation messages.
     *
     * @return any message related to the address
     */
    String getMessage();

    /**
     * Get the carrier facility for the address.
     *
     * @return the carrier facility for the address
     */
    String getCarrierFacility();

    /**
     * Get the federal tax ID associated with the address.
     *
     * @return the federal tax ID associated with the address
     */
    String getFederalTaxId();

    /**
     * Check if the address is for a residential location.
     *
     * @return true if the address is for a residential location, otherwise false
     */
    Boolean getResidential();

    /**
     * Get the verifications for the address.
     *
     * @return the verifications for the address
     */
    IAddressVerifications getVerifications();


    /**
     * Returns a string representation of the address.
     *
     * @return String representation of the address.
     */
    String toString();

    /**
     * Pretty print the JSON representation of the address.
     *
     * @return the JSON representation of the address.
     */
    String prettyPrint();
}
