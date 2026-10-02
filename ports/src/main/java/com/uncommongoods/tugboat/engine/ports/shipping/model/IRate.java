package com.uncommongoods.tugboat.engine.ports.shipping.model;

/**
 * Interface for Rate operations
 */
public interface IRate extends IEasyPostResource, JsonSerializable, Mappable, Cloneable {

    String getCarrier();

    void setCarrier(String carrier);

    String getService();

    void setService(String service);

    Float getRate();

    void setRate(Float rate);

    /**
     * Get the currency of the rate.
     *
     * @return the currency of the rate
     */
    String getCurrency();

    /**
     * Get the list rate amount.
     *
     * @return the list rate amount
     */
    Float getListRate();

    /**
     * Get the list currency of the rate.
     *
     * @return the list currency of the rate
     */
    String getListCurrency();

    /**
     * Get the retail rate amount.
     *
     * @return the retail rate amount
     */
    Float getRetailRate();

    /**
     * Get the retail currency of the rate.
     *
     * @return the retail currency of the rate
     */
    String getRetailCurrency();

    /**
     * Get the delivery days of the rate.
     *
     * @return the delivery days of the rate
     */
    Number getDeliveryDays();

    void setDeliveryDays(Number deliveryDays);

    /**
     * Get the delivery date of the rate.
     *
     * @return the delivery date of the rate
     */
    String getDeliveryDate();

    void setDeliveryDate(String deliveryDate);

    /**
     * Check if the delivery date is guaranteed.
     *
     * @return true if the delivery date is guaranteed, otherwise false
     */
    Boolean getDeliveryDateGuaranteed();

    /**
     * Get the estimated delivery days of the rate.
     *
     * @return the estimated delivery days of the rate
     */
    Number getEstDeliveryDays();

    /**
     * Get the shipment ID of the rate.
     *
     * @return the shipment ID of the rate
     */
    String getShipmentId();

    /**
     * Get the carrier account ID of the rate.
     *
     * @return the carrier account ID of the rate
     */
    String getCarrierAccountId();

    /**
     * Get the billing type of the rate.
     *
     * @return the billing type of the rate
     */
    String getBillingType();

    public IRate clone() throws CloneNotSupportedException;
}
