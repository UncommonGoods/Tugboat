// Copyright (c) 2025 Uncommon Goods LLC
// SPDX-License-Identifier: MPL-2.0

package com.uncommongoods.tugboat.engine.state;

import com.uncommongoods.tugboat.engine.Tugboat;
import com.uncommongoods.tugboat.engine.ports.shipping.model.IOrder;
import com.uncommongoods.tugboat.engine.ports.shipping.model.IPostageLabel;
import com.uncommongoods.tugboat.engine.ports.shipping.model.IRate;
import com.uncommongoods.tugboat.engine.ports.shipping.model.IShipment;
import com.uncommongoods.tugboat.engine.ports.shipping.service.IShippingClient;
import com.uncommongoods.tugboat.engine.exception.TugboatException;
import com.uncommongoods.tugboat.engine.manifest.CarrierService;
import com.uncommongoods.tugboat.engine.manifest.PickupGroup;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.uncommongoods.tugboat.engine.state.Utils.getShippingClient;

public class ShoppedTugboatState extends TugboatStateBase {
    public ShoppedTugboatState(Tugboat tugboat) {
        super(tugboat);
        this.packageState = State.SHOPPED;
    }

    @Override
    public TugboatStateBase initialize() throws TugboatException {
        throw new IllegalStateException("Cannot initialize from shopped state. Already past initialization.");
    }

    @Override
    public TugboatStateBase rate() {
        throw new IllegalStateException("Cannot rate from shopped state. Already rated.");
    }

    @Override
    public TugboatStateBase shop() {
        throw new IllegalStateException("Cannot shop from shopped state. Already shopped.");
    }

    @Override
    public TugboatStateBase purchase() throws TugboatException {
        Tugboat tugboat = this.getTugboat();

        checkManifestDate(tugboat);
        buyPostage(tugboat);
        addTugboatToManifestGroup(tugboat);

        return new PurchasedTugboatState(getTugboat());
    }

    private void buyPostage(Tugboat tugboat) throws TugboatException {
        IShippingClient shippingClient = getShippingClient(tugboat);
        IRate selectedRate = tugboat.getSelectedRate();
        String selectedShipmentId = selectedRate.getShipmentId();
        if (tugboat.getParcels().size() > 1) {
            int selectedOrderIndex = IntStream.range(0, tugboat.getOrderRateResponses().size())
                .filter(i -> tugboat.getOrderRateResponses().get(i).getShipments().stream()
                    .anyMatch(shipment -> shipment.getId().equals(selectedShipmentId)))
                .findFirst()
                .orElse(-1);
            IOrder selectedOrder = tugboat.getOrderRateResponses().get(selectedOrderIndex);
            if (selectedOrder != null) {
                String selectedOrderId = selectedOrder.getId();
                IOrder purchasedOrder = shippingClient.getOrderService().buy(selectedOrderId, selectedRate);
                tugboat.getOrderRateResponses().set(selectedOrderIndex, purchasedOrder);
                List<IPostageLabel> labels = tugboat.getOrderRateResponses().get(selectedOrderIndex).getShipments().stream()
                        .map(IShipment::getPostageLabel)
                        .collect(Collectors.toList());
                List<String> trackingCodes = tugboat.getOrderRateResponses().get(selectedOrderIndex).getShipments().stream()
                        .map(IShipment::getTrackingCode)
                        .toList();
                tugboat.setPostageLabels(labels);
                tugboat.setTrackingCodes(trackingCodes);

            }
        } else {
            int selectedShipmentIndex = IntStream.range(0, tugboat.getShipmentRateResponses().size())
                .filter(i -> tugboat.getShipmentRateResponses().get(i).getId().equals(selectedShipmentId))
                .findFirst()
                .orElse(-1);
            IShipment selectedShipment = tugboat.getShipmentRateResponses().get(selectedShipmentIndex);
            if (selectedShipment != null) {
                shippingClient.getShipmentService().setShipment(selectedShipment);
                IShipment purchasedShipment = shippingClient.getShipmentService().buy(selectedShipmentId, selectedRate);
                tugboat.getShipmentRateResponses().set(selectedShipmentIndex, purchasedShipment);
                List<IPostageLabel> labels = new ArrayList<>(List.of(tugboat.getShipmentRateResponses().get(selectedShipmentIndex).getPostageLabel()));
                List<String> trackingCodes = List.of(tugboat.getShipmentRateResponses().get(selectedShipmentIndex).getTrackingCode());
                tugboat.setPostageLabels(labels);
                tugboat.setTrackingCodes(trackingCodes);
            }
        }
    }

    private void addTugboatToManifestGroup(Tugboat tugboat) {
        if (tugboat.getPickupFacility() != null && tugboat.getManifestBatchId() != null) {
            tugboat.getPickupFacility().addTugboatToGroup(tugboat);
        }
    }

    private void checkManifestDate(Tugboat tugboat) {
        if (tugboat.getPickupFacility() != null) {
            IRate selectedRate = tugboat.getSelectedRate();
            CarrierService selectedService = new CarrierService(selectedRate.getCarrier(), selectedRate.getService());
            PickupGroup pickupGroup = tugboat.getPickupFacility().getPickupGroups().stream()
                .filter(manGroup -> manGroup.getCarrierAccountId().equals(selectedRate.getCarrierAccountId()))
                .filter(manGroup -> manGroup.getAvailableServices().contains(selectedService))
                .findFirst().orElse(null);
            if (pickupGroup.getPickupDate().isBefore(LocalDate.now())) {
                throw new IllegalStateException("the manifest date (" + pickupGroup.getPickupDate() + ") on group " + pickupGroup.getPickupGroupName() + " is in the past");
            }
        }
    }

    private void removeTugboatFromManifestGroup(Tugboat tugboat) {
        if (tugboat.getPickupFacility() != null) {
            tugboat.getPickupFacility().removeTugboatFromGroup(tugboat);
        }
    }

    @Override
    public List<IPostageLabel> print() {
        throw new IllegalStateException("Cannot print from shopped state. Must purchase first.");
    }

    @Override
    public List<IPostageLabel> reprint() {
        throw new IllegalStateException("Cannot reprint from shopped state. Must purchase and print first.");
    }

    @Override
    public TugboatStateBase voidLabel() {
        throw new IllegalStateException("Cannot void label from shopped state. Must purchase and print first.");
    }
}
