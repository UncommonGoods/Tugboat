package com.uncommongoods.tugboat.engine.ports.shipping.model;

import com.easypost.model.ShipmentMessage;

public class ShipmentMessageAdapter implements IShipmentMessage {
    private final ShipmentMessage shipmentMessage;

    public ShipmentMessageAdapter(ShipmentMessage shipmentMessage) {
        this.shipmentMessage = shipmentMessage;
    }

    @Override
    public String getCarrier() {
        return shipmentMessage.getCarrier();
    }

    @Override
    public String getCarrierAccountId() {
        return shipmentMessage.getCarrierAccountId();
    }

    @Override
    public String getType() {
        return shipmentMessage.getType();
    }

    @Override
    public Object getMessage() {
        return shipmentMessage.getMessage();
    }

    public ShipmentMessage getShipmentMessage() {
        return shipmentMessage;
    }
}
