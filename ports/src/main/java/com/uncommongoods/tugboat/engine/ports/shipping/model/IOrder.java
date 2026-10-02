package com.uncommongoods.tugboat.engine.ports.shipping.model;

import java.util.List;
import java.util.Map;

import com.uncommongoods.tugboat.engine.exception.TugboatException;

/**
 * Interface for Order operations
 */
public interface IOrder extends IEasyPostResource, JsonSerializable, Mappable {
    /**
     * Get the service of the order.
     *
     * @return the service of the order
     */
    String getService();

    /**
     * Get the reference of the order.
     *
     * @return the reference of the order
     */
    String getReference();

    /**
     * Check if the order is a return.
     *
     * @return true if the order is a return, otherwise false
     */
    Boolean getIsReturn();

    /**
     * Get the to address of the order.
     *
     * @return the to address of the order
     */
    IAddress getToAddress();

    /**
     * Get the buyer address of the order.
     *
     * @return the buyer address of the order
     */
    IAddress getBuyerAddress();

    /**
     * Get the from address of the order.
     *
     * @return the from address of the order
     */
    IAddress getFromAddress();

    /**
     * Get the return address of the order.
     *
     * @return the return address of the order
     */
    IAddress getReturnAddress();

    /**
     * Get the customs info of the order.
     *
     * @return the customs info of the order
     */
    ICustomsInfo getCustomsInfo();

    /**
     * Get the shipments of the order.
     *
     * @return the shipments of the order
     */
    List<IShipment> getShipments();

    /**
     * Get the rates of the order.
     *
     * @return the rates of the order
     */
    List<IRate> getRates();

    /**
     * Get the options of the order.
     *
     * @return the options of the order
     */
    Map<String, Object> getOptions();

    /**
     * Get the messages of the order.
     *
     * @return the messages of the order
     */
    List<IShipmentMessage> getMessages();

    /**
     * Get the carrier accounts of the order.
     *
     * @return the carrier accounts of the order
     */
    List<ICarrierAccount> getCarrierAccounts();

    /**
     * Get the lowest rate for this Order.
     *
     * @return Lowest Rate object
     * @throws TugboatException when the request fails.
     */
    IRate lowestRate() throws TugboatException;

    /**
     * Get the lowest rate for this Order.
     *
     * @param carriers The carriers to use in the filter.
     * @param services The services to use in the filter.
     * @return Lowest Rate object
     * @throws TugboatException when the request fails.
     */
    IRate lowestRate(List<String> carriers, List<String> services) throws TugboatException;

    /**
     * Get the lowest rate for this order.
     *
     * @param carriers The carriers to use in the query.
     * @return Rate object
     * @throws TugboatException when the request fails.
     */
    IRate lowestRate(List<String> carriers) throws TugboatException;
}
