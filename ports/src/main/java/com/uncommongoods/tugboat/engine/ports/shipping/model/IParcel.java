package com.uncommongoods.tugboat.engine.ports.shipping.model;

/**
 * Interface for Parcel operations
 */
public interface IParcel extends IEasyPostResource, JsonSerializable, Mappable {
    /**
     * Get the predefined package of the parcel.
     *
     * @return the predefined package of the parcel
     */
    String getPredefinedPackage();

    /**
     * Set the predefined package of the parcel.
     *
     * @param predefinedPackage the predefined package of the parcel
     */
    void setPredefinedPackage(String predefinedPackage);

    /**
     * Get the weight of the parcel.
     *
     * @return the weight of the parcel
     */
    Float getWeight();

    /**
     * Set the weight of the parcel.
     *
     * @param weight the weight of the parcel
     */
    void setWeight(Float weight);

    /**
     * Get the length of the parcel.
     *
     * @return the length of the parcel
     */
    Float getLength();

    /**
     * Set the length of the parcel.
     *
     * @param length the length of the parcel
     */
    void setLength(Float length);

    /**
     * Get the width of the parcel.
     *
     * @return the width of the parcel
     */
    Float getWidth();

    /**
     * Set the width of the parcel.
     *
     * @param width the width of the parcel
     */
    void setWidth(Float width);

    /**
     * Get the height of the parcel.
     *
     * @return the height of the parcel
     */
    Float getHeight();

    /**
     * Set the height of the parcel.
     *
     * @param height the height of the parcel
     */
    void setHeight(Float height);
}
