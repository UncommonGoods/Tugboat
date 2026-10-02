package com.uncommongoods.tugboat.engine.ports.shipping.model;

import java.util.List;

/**
 * Interface for CustomsInfo operations
 */
public interface ICustomsInfo extends IEasyPostResource, JsonSerializable, Mappable {
    /**
     * Get the contents type of the customs info.
     *
     * @return the contents type of the customs info
     */
    String getContentsType();

    /**
     * Get the contents explanation of the customs info.
     *
     * @return the contents explanation of the customs info
     */
    String getContentsExplanation();

    /**
     * Check if customs certify is enabled.
     *
     * @return true if customs certify is enabled, otherwise false
     */
    boolean isCustomsCertify();

    /**
     * Get the customs signer of the customs info.
     *
     * @return the customs signer of the customs info
     */
    String getCustomsSigner();

    /**
     * Get the non-delivery option of the customs info.
     *
     * @return the non-delivery option of the customs info
     */
    String getNonDeliveryOption();

    /**
     * Get the restriction type of the customs info.
     *
     * @return the restriction type of the customs info
     */
    String getRestrictionType();

    /**
     * Get the restriction comments of the customs info.
     *
     * @return the restriction comments of the customs info
     */
    String getRestrictionComments();

    /**
     * Get the customs items of the customs info.
     *
     * @return the customs items of the customs info
     */
    List<ICustomsItem> getCustomsItems();

    /**
     * Get the EEL/PFC of the customs info.
     *
     * @return the EEL/PFC of the customs info
     */
    String getEelPfc();

    /**
     * Get the declaration of the customs info.
     *
     * @return the declaration of the customs info
     */
    String getDeclaration();
}

