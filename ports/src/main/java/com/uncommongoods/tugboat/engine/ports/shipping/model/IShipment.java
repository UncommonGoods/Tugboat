package com.uncommongoods.tugboat.engine.ports.shipping.model;

import com.uncommongoods.tugboat.engine.exception.TugboatException;

import java.util.List;
import java.util.Map;

/**
 * Interface for Shipment operations
 */
public interface IShipment extends IEasyPostResource, JsonSerializable, Mappable {
    /**
     * Get the reference of the shipment.
     *
     * @return the reference of the shipment
     */
    String getReference();

    /**
     * Check if the shipment is a return.
     *
     * @return true if the shipment is a return, otherwise false
     */
    Boolean getIsReturn();

    /**
     * Get the to address of the shipment.
     *
     * @return the to address of the shipment
     */
    IAddress getToAddress();

    /**
     * Get the buyer address of the shipment.
     *
     * @return the buyer address of the shipment
     */
    IAddress getBuyerAddress();

    /**
     * Get the from address of the shipment.
     *
     * @return the from address of the shipment
     */
    IAddress getFromAddress();

    /**
     * Get the return address of the shipment.
     *
     * @return the return address of the shipment
     */
    IAddress getReturnAddress();

    /**
     * Get the parcel of the shipment.
     *
     * @return the parcel of the shipment
     */
    IParcel getParcel();

    /**
     * Get the customs info of the shipment.
     *
     * @return the customs info of the shipment
     */
    ICustomsInfo getCustomsInfo();

    /**
     * Get the selected rate of the shipment.
     *
     * @return the selected rate of the shipment
     */
    IRate getSelectedRate();

    void setSelectedRate(IRate rate);

    /**
     * Get the rates of the shipment.
     *
     * @return the rates of the shipment
     */
    List<IRate> getRates();

    /**
     * Get the postage label of the shipment.
     *
     * @return the postage label of the shipment
     */
    IPostageLabel getPostageLabel();

    /**
     * Get the scan form of the shipment.
     *
     * @return the scan form of the shipment
     */
    IScanForm getScanForm();

    /**
     * Get the order ID of the shipment.
     *
     * @return the order ID of the shipment
     */
    String getOrderId();

    /**
     * Get the forms of the shipment.
     *
     * @return the forms of the shipment
     */
    List<IForm> getForms();

    /**
     * Get the tracker of the shipment.
     *
     * @return the tracker of the shipment
     */
    ITracker getTracker();

    /**
     * Get the insurance of the shipment.
     *
     * @return the insurance of the shipment
     */
    String getInsurance();

    /**
     * Get the tracking code of the shipment.
     *
     * @return the tracking code of the shipment
     */
    String getTrackingCode();

    /**
     * Get the status of the shipment.
     *
     * @return the status of the shipment
     */
    String getStatus();

    /**
     * Get the refund status of the shipment.
     *
     * @return the refund status of the shipment
     */
    String getRefundStatus();

    /**
     * Get the batch ID of the shipment.
     *
     * @return the batch ID of the shipment
     */
    String getBatchId();

    /**
     * Get the batch status of the shipment.
     *
     * @return the batch status of the shipment
     */
    String getBatchStatus();

    /**
     * Get the batch message of the shipment.
     *
     * @return the batch message of the shipment
     */
    String getBatchMessage();

    /**
     * Get the USPS zone of the shipment.
     *
     * @return the USPS zone of the shipment
     */
    String getUspsZone();

    /**
     * Get the options of the shipment.
     *
     * @return the options of the shipment
     */
    Map<String, Object> getOptions();

    /**
     * Get the messages of the shipment.
     *
     * @return the messages of the shipment
     */
    List<IShipmentMessage> getMessages();

    /**
     * Get the tax identifiers of the shipment.
     *
     * @return the tax identifiers of the shipment
     */
    List<ITaxIdentifier> getTaxIdentifiers();

    /**
     * Get the carrier accounts of the shipment.
     *
     * @return the carrier accounts of the shipment
     */
    List<ICarrierAccount> getCarrierAccounts();

    /**
     * Get the service of the shipment.
     *
     * @return the service of the shipment
     */
    String getService();

    /**
     * Get the fees of the shipment.
     *
     * @return the fees of the shipment
     */
    List<IFee> getFees();

    /**
     * Get the lowest rate for this Shipment.
     *
     * @return lowest Rate object
     * @throws TugboatException when the request fails.
     */
    IRate lowestRate() throws TugboatException;

    /**
     * Get the lowest rate for this Shipment.
     *
     * @param carriers the carriers to use in the filter.
     * @param services the services to use in the filter.
     * @return lowest Rate object
     * @throws TugboatException when the request fails.
     */
    IRate lowestRate(List<String> carriers, List<String> services) throws TugboatException;

    /**
     * Get the lowest rate for this shipment.
     *
     * @param carriers the carriers to use in the query.
     * @return Rate object
     * @throws TugboatException when the request fails.
     */
    IRate lowestRate(List<String> carriers) throws TugboatException;
}
