package com.uncommongoods.tugboat.engine.ports.shipping.service;

import java.util.List;
import java.util.Map;

import com.uncommongoods.tugboat.engine.exception.TugboatException;
import com.uncommongoods.tugboat.engine.exception.EndOfPaginationException;
import com.uncommongoods.tugboat.engine.ports.shipping.model.IRate;
import com.uncommongoods.tugboat.engine.ports.shipping.model.IShipment;
import com.uncommongoods.tugboat.engine.ports.shipping.model.IShipmentCollection;
import com.uncommongoods.tugboat.engine.ports.shipping.model.ISmartRate;
import com.uncommongoods.tugboat.engine.ports.shipping.model.ISmartRateAccuracy;
import com.uncommongoods.tugboat.engine.ports.shipping.model.IEstimatedDeliveryDate;
import com.uncommongoods.tugboat.engine.ports.shipping.model.IRecommendShipDateForShipmentResult;

public interface IShipmentService {
    /**
     * Create a new Shipment object from a map of parameters.
     *
     * @param params The map of parameters.
     * @return Shipment object
     * @throws TugboatException when the request fails.
     */
    IShipment create(Map<String, Object> params) throws TugboatException;

    /**
     * Retrieve a Shipment from the API.
     *
     * @param id The ID of the Shipment to retrieve.
     * @return Shipment object
     * @throws TugboatException when the request fails.
     */
    IShipment retrieve(String id) throws TugboatException;

    /**
     * Get a list of all Shipment objects.
     *
     * @param params The options for the query.
     * @return ShipmentCollection object
     * @throws TugboatException when the request fails.
     */
    IShipmentCollection all(Map<String, Object> params) throws TugboatException;

    /**
     * Get the next page of an ShipmentCollection.
     *
     * @param collection ShipmentCollection to get next page of.
     * @return ShipmentCollection object.
     * @throws EndOfPaginationException when there are no more pages to retrieve.
     */
    IShipmentCollection getNextPage(IShipmentCollection collection) throws EndOfPaginationException;

    /**
     * Get the next page of an ShipmentCollection.
     *
     * @param collection ShipmentCollection to get next page of.
     * @param pageSize   The number of results to return on the next page.
     * @return ShipmentCollection object.
     * @throws EndOfPaginationException when there are no more pages to retrieve.
     */
    IShipmentCollection getNextPage(IShipmentCollection collection, Integer pageSize) throws EndOfPaginationException;

    /**
     * Get new rates for this Shipment.
     *
     * @param id The ID of shipment.
     * @return Shipment object
     * @throws TugboatException when the request fails.
     */
    IShipment newRates(String id) throws TugboatException;

    /**
     * Get new rates for this Shipment.
     *
     * @param id     The ID of shipment.
     * @param params The options for the query.
     * @return Shipment object
     * @throws TugboatException when the request fails.
     */
    IShipment newRates(String id, Map<String, Object> params) throws TugboatException;

    /**
     * Get SmartRate for this Shipment.
     *
     * @param id The ID of shipment.
     * @return List of SmartRate objects
     * @throws TugboatException when the request fails.
     */
    List<ISmartRate> smartRates(String id) throws TugboatException;

    /**
     * Get SmartRates for this Shipment.
     *
     * @param id     The ID of shipment.
     * @param params The options for the query.
     * @return List of SmartRate objects
     * @throws TugboatException when the request fails.
     */
    List<ISmartRate> smartRates(String id, Map<String, Object> params) throws TugboatException;

    /**
     * Buy this Shipment.
     *
     * @param id     The ID of shipment.
     * @param params The options for the query.
     * @return Shipment object
     * @throws TugboatException when the request fails.
     */
    IShipment buy(String id, Map<String, Object> params) throws TugboatException;

    /**
     * Buy this Shipment.
     *
     * @param id   The ID of shipment.
     * @param rate The Rate to use for this Shipment.
     * @return Shipment object
     * @throws TugboatException when the request fails.
     */
    IShipment buy(String id, IRate rate) throws TugboatException;

    /**
     * Buy this Shipment.
     *
     * @param id           The ID of shipment.
     * @param rate         The Rate to use for this Shipment.
     * @param endShipperId The id of the end shipper to use for this purchase.
     * @return Shipment object
     * @throws TugboatException when the request fails.
     */
    IShipment buy(String id, IRate rate, String endShipperId) throws TugboatException;

    /**
     * Buy this Shipment.
     *
     * @param id           The ID of shipment.
     * @param params       The options for the query.
     * @param endShipperId The id of the end shipper to use for this purchase.
     * @return Shipment object
     * @throws TugboatException when the request fails.
     */
    IShipment buy(String id, Map<String, Object> params, String endShipperId) throws TugboatException;

    /**
     * Refund this Shipment.
     *
     * @param id The ID of shipment.
     * @return Shipment object
     * @throws TugboatException when the request fails.
     */
    IShipment refund(String id) throws TugboatException;

    /**
     * Refund this Shipment.
     *
     * @param id     The ID of shipment.
     * @param params The options for the query.
     * @return Shipment object
     * @throws TugboatException when the request fails.
     */
    IShipment refund(String id, Map<String, Object> params) throws TugboatException;

    /**
     * Label this Shipment.
     *
     * @param id     The ID of shipment.
     * @param params The options for the query.
     * @return Shipment object
     * @throws TugboatException when the request fails.
     */
    IShipment label(String id, Map<String, Object> params) throws TugboatException;

    /**
     * Insure this Shipment.
     *
     * @param id     The ID of shipment.
     * @param params The options for the query.
     * @return Shipment object
     * @throws TugboatException when the request fails.
     */
    IShipment insure(String id, Map<String, Object> params) throws TugboatException;

    /**
     * Get the lowest SmartRate for this Shipment.
     *
     * @param id               The ID of shipment.
     * @param deliveryDay      Delivery days restriction to use when filtering.
     * @param deliveryAccuracy Delivery days accuracy restriction to use when filtering.
     * @return lowest SmartRate object
     * @throws TugboatException when the request fails.
     */
    ISmartRate lowestSmartRate(String id, int deliveryDay, ISmartRateAccuracy deliveryAccuracy) throws TugboatException;

    /**
     * Find the lowest SmartRate from a list of SmartRates.
     *
     * @param smartRates       List of SmartRates to filter from.
     * @param deliveryDay      Delivery days restriction to use when filtering.
     * @param deliveryAccuracy Delivery days accuracy restriction to use when filtering.
     * @return lowest SmartRate object
     * @throws TugboatException when the request fails.
     */
    ISmartRate findLowestSmartRate(List<ISmartRate> smartRates, int deliveryDay, ISmartRateAccuracy deliveryAccuracy)
        throws TugboatException;

    /**
     * Generate a form for this shipment.
     *
     * @param id       The ID of shipment.
     * @param formType The form type for this shipment.
     * @return Return a shipment object.
     * @throws TugboatException when the request fails.
     */
    IShipment generateForm(String id, String formType) throws TugboatException;

    /**
     * Generate a form for this shipment.
     *
     * @param id          The ID of shipment.
     * @param formType    The form type for this shipment.
     * @param formOptions The form options for this shipment.
     * @return Return a shipment object.
     * @throws TugboatException when the request fails.
     */
    IShipment generateForm(String id, String formType, Map<String, Object> formOptions) throws TugboatException;

    /**
     * Retrieves the estimated delivery date of each Rate via SmartRate.
     *
     * @param id              The id of the shipment.
     * @param plannedShipDate The planned shipment date.
     * @return List of EstimatedDeliveryDate objects.
     * @throws TugboatException When the request fails.
     */
    List<IEstimatedDeliveryDate> retrieveEstimatedDeliveryDate(String id, String plannedShipDate)
        throws TugboatException;

    /**
     * Retrieve a recommended ship date for an existing Shipment via the Precision Shipping API,
     * based on a specific desired delivery date.
     *
     * @param id                  The id of the shipment.
     * @param desiredDeliveryDate The desired delivery date.
     * @return List of RecommendShipDateForShipmentResult objects.
     * @throws TugboatException When the request fails.
     */
    List<IRecommendShipDateForShipmentResult> recommendShipDate(String id, String desiredDeliveryDate)
        throws TugboatException;

    void setShipment(IShipment shipment) throws TugboatException;
}
