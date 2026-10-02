package com.uncommongoods.tugboat.engine.ports.shipping.service;

import java.util.function.Function;

import com.uncommongoods.tugboat.engine.ports.shipping.model.IRequestHookResponses;
import com.uncommongoods.tugboat.engine.ports.shipping.model.IResponseHookResponses;

/**
 * Interface for a shipping client API
 */
public interface IShippingClient {
    /**
     * Get the address service
     *
     * @return address service
     */
    IAddressService getAddressService();

    /**
     * Get the address service
     *
     * @return address service
     */
    IBatchService getBatchService();

    /**
     * Get the parcel service
     *
     * @return parcel service
     */
    IParcelService getParcelService();

    /**
     * Get the rate service
     *
     * @return rate service
     */
    IRateService getRateService();

    /**
     * Get the shipment service
     *
     * @return shipment service
     */
    IShipmentService getShipmentService();

    /**
     * Get the carrier account service
     *
     * @return carrier account service
     */
    ICarrierAccountService getCarrierAccountService();

    /**
     * Get the order service
     *
     * @return order service
     */
    IOrderService getOrderService();

    /**
     * Get the refund service
     *
     * @return refund service
     */
    IRefundService getRefundService();

    /**
     * Get the scan form service
     *
     * @return scan form service
     */
    IScanFormService getScanFormService();

    /**
     * Get the tracker service
     *
     * @return tracker service
     */
    ITrackerService getTrackerService();

    /**
     * Subscribes to a request hook from the given function.
     *
     * @param function The function to be subscribed to the request hook
     */
    void subscribeToRequestHook(Function<IRequestHookResponses, Object> function);

    /**
     * Unsubscribes from a request hook.
     *
     * @param function The function to be unsubscribed from the request hook
     */
    void unsubscribeFromRequestHook(Function<IRequestHookResponses, Object> function);

    /**
     * Subscribes to a response hook from the given function.
     *
     * @param function The function to be subscribed to the response hook
     */
    void subscribeToResponseHook(Function<IResponseHookResponses, Object> function);

    /**
     * Unsubscribes from a response hook.
     *
     * @param function The function to be unsubscribed from the response hook
     */
    void unsubscribeFromResponseHook(Function<IResponseHookResponses, Object> function);

    /**
     * Get the connection timeout in milliseconds.
     *
     * @return the connection timeout in milliseconds
     */
    int getConnectionTimeoutMilliseconds();

    /**
     * Get the read timeout in milliseconds.
     *
     * @return the read timeout in milliseconds
     */
    int getReadTimeoutMilliseconds();

    /**
     * Get the API key.
     *
     * @return the API key
     */
    String getApiKey();

    /**
     * Get the API version.
     *
     * @return the API version
     */
    String getApiVersion();

    /**
     * Get the API base URL.
     *
     * @return the API base URL
     */
    String getApiBase();
}
