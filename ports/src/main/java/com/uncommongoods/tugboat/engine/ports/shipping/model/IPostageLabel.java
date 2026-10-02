package com.uncommongoods.tugboat.engine.ports.shipping.model;

/**
 * Interface for PostageLabel operations
 */
public interface IPostageLabel extends IEasyPostResource, JsonSerializable {
    /**
     * Get the date advance of the postage label.
     *
     * @return the date advance of the postage label
     */
    int getDateAdvance();

    /**
     * Get the integrated form of the postage label.
     *
     * @return the integrated form of the postage label
     */
    String getIntegratedForm();

    /**
     * Get the label resolution of the postage label.
     *
     * @return the label resolution of the postage label
     */
    int getLabelResolution();

    /**
     * Get the label size of the postage label.
     *
     * @return the label size of the postage label
     */
    String getLabelSize();

    /**
     * Get the label type of the postage label.
     *
     * @return the label type of the postage label
     */
    String getLabelType();

    /**
     * Get the label URL of the postage label.
     *
     * @return the label URL of the postage label
     */
    String getLabelUrl();

    /**
     * Get the label file of the postage label.
     *
     * @return the label file of the postage label
     */
    String getLabelFile();

    void setLabelFile(String labelFile);
    /**
     * Get the label file type of the postage label.
     *
     * @return the label file type of the postage label
     */
    String getLabelFileType();

    /**
     * Get the label PDF size of the postage label.
     *
     * @return the label PDF size of the postage label
     */
    String getLabelPdfSize();

    /**
     * Get the label PDF type of the postage label.
     *
     * @return the label PDF type of the postage label
     */
    String getLabelPdfType();

    /**
     * Get the label PDF URL of the postage label.
     *
     * @return the label PDF URL of the postage label
     */
    String getLabelPdfUrl();

    /**
     * Get the label PDF file type of the postage label.
     *
     * @return the label PDF file type of the postage label
     */
    String getLabelPdfFileType();

    /**
     * Get the label EPL2 size of the postage label.
     *
     * @return the label EPL2 size of the postage label
     */
    String getLabelEpl2Size();

    /**
     * Get the label EPL2 type of the postage label.
     *
     * @return the label EPL2 type of the postage label
     */
    String getLabelEpl2Type();

    /**
     * Get the label EPL2 URL of the postage label.
     *
     * @return the label EPL2 URL of the postage label
     */
    String getLabelEpl2Url();

    /**
     * Get the label EPL2 file type of the postage label.
     *
     * @return the label EPL2 file type of the postage label
     */
    String getLabelEpl2FileType();

    /**
     * Get the label ZPL size of the postage label.
     *
     * @return the label ZPL size of the postage label
     */
    String getLabelZplSize();

    /**
     * Get the label ZPL type of the postage label.
     *
     * @return the label ZPL type of the postage label
     */
    String getLabelZplType();

    /**
     * Get the label ZPL URL of the postage label.
     *
     * @return the label ZPL URL of the postage label
     */
    String getLabelZplUrl();

    /**
     * Get the label ZPL file type of the postage label.
     *
     * @return the label ZPL file type of the postage label
     */
    String getLabelZplFileType();
}
