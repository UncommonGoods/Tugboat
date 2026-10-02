package com.uncommongoods.tugboat.engine.ports.shipping.model;

/**
 * Interface for ShipmentMessage operations
 */
public interface IShipmentMessage {
    /**
     * Get the carrier of the shipment message.
     *
     * @return the carrier of the shipment message
     */
    String getCarrier();

    /**
     * Get the carrier account ID of the shipment message.
     *
     * @return the carrier account ID of the shipment message
     */
    String getCarrierAccountId();

    /**
     * Get the type of the shipment message.
     *
     * @return the type of the shipment message
     */
    String getType();

    /**
     * Get the message of the shipment message.
     *
     * @return the message of the shipment message
     */
    Object getMessage();
}
