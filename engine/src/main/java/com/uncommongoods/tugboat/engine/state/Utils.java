// Copyright (c) 2025 Uncommon Goods LLC
// SPDX-License-Identifier: MPL-2.0

package com.uncommongoods.tugboat.engine.state;

import com.uncommongoods.tugboat.engine.Tugboat;
import com.uncommongoods.tugboat.engine.ports.shipping.model.IOrder;
import com.uncommongoods.tugboat.engine.ports.shipping.model.IRate;
import com.uncommongoods.tugboat.engine.ports.shipping.model.IShipment;
import com.uncommongoods.tugboat.engine.ports.shipping.service.IShippingClient;
import com.uncommongoods.tugboat.engine.exception.TugboatException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.uncommongoods.tugboat.engine.config.EngineConfig.DEFAULT_CLIENT_KEY;

public class Utils {
    public static void voidShipment(Tugboat tugboat) throws TugboatException {
        IShippingClient shippingClient = getShippingClient(tugboat);
        String carrier = tugboat.getSelectedRate().getCarrier();
        List<String> trackingCodes;
        List<String> shipmentIds;
        if (tugboat.getParcels().size() > 1) {
            IOrder selectedOrder = tugboat.getOrderRateResponses().stream()
                .filter(order -> order.getShipments().stream()
                    .anyMatch(shipment -> shipment.getId().equals(tugboat.getSelectedRate().getShipmentId())))
                .findFirst()
                .orElse(null);
            trackingCodes = selectedOrder.getShipments().stream()
                .map(IShipment::getTrackingCode)
                .toList();
            shipmentIds = selectedOrder.getShipments().stream()
                .map(IShipment::getId)
                .toList();
        } else {
            IShipment selectedShipment = tugboat.getShipmentRateResponses().stream()
                .filter(shipment -> shipment.getId().equals(tugboat.getSelectedRate().getShipmentId()))
                .findFirst()
                .orElse(null);
            trackingCodes = List.of(selectedShipment.getTrackingCode());
            shipmentIds = List.of(selectedShipment.getId());
        }
        Map<String, Object> refunds = new HashMap<>();
        refunds.put("carrier", carrier);
        refunds.put("tracking_codes", trackingCodes);
        refunds.put("shipment_ids", shipmentIds);
        try {
            shippingClient.getRefundService().create(refunds);
        } catch (TugboatException e) {
            // void should never fail, log out the error message
            System.out.println(e.getMessage());
        }
        if (tugboat.getPickupFacility() != null) {
            tugboat.getPickupFacility().removeTugboatFromGroup(tugboat);
        }
    }

    /**
     * Copy rates so edits made after rating (rated-hook price/service/date adjustments) stay
     * visible as a diff against the untouched shipment/order rate responses instead of writing
     * through into them.
     */
    public static List<IRate> copyRates(List<IRate> rates) throws TugboatException {
        List<IRate> copies = new ArrayList<>(rates.size());
        for (IRate rate : rates) {
            try {
                copies.add(rate.clone());
            } catch (CloneNotSupportedException e) {
                throw new TugboatException("could not copy rate " + rate.getId(), e);
            }
        }
        return Collections.unmodifiableList(copies);
    }

    public static IShippingClient getShippingClient(Tugboat tugboat) {
        String selectedClient = tugboat.getOptions() != null ? tugboat.getShippingClientKey() : DEFAULT_CLIENT_KEY;
        IRate selectedRate = tugboat.getSelectedRate();
        if (selectedRate != null && selectedRate.getProviderType() != null) {
            String providerKey = selectedRate.getProviderType();
            if (tugboat.getEngineConfig().getShippingClients().containsKey(providerKey)) {
                selectedClient = providerKey;
            }
        }
        return tugboat.getEngineConfig().getShippingClients().get(selectedClient);
    }
}
