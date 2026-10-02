package com.uncommongoods.tugboat.engine.ports.shipping.model;

/**
 * Interface for Form operations
 */
public interface IForm extends IEasyPostResource {
    /**
     * Get the form type.
     *
     * @return the form type
     */
    String getFormType();

    /**
     * Get the form URL.
     *
     * @return the form URL
     */
    String getFormUrl();

    /**
     * Get the submitted electronically flag.
     *
     * @return true if submitted electronically, false otherwise
     */
    Boolean getSubmittedElectronically();
}
