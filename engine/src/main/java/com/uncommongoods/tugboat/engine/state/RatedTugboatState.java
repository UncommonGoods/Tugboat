// Copyright (c) 2025 Uncommon Goods LLC
// SPDX-License-Identifier: MPL-2.0

package com.uncommongoods.tugboat.engine.state;

import com.uncommongoods.tugboat.engine.Tugboat;
import com.uncommongoods.tugboat.engine.TugboatOptions;
import com.uncommongoods.tugboat.engine.ports.shipping.model.IPostageLabel;
import com.uncommongoods.tugboat.engine.ports.shipping.model.IRate;
import com.uncommongoods.tugboat.engine.ports.shipping.model.IShipment;
import com.uncommongoods.tugboat.engine.exception.TugboatException;
import com.uncommongoods.tugboat.engine.manifest.CarrierService;
import com.uncommongoods.tugboat.engine.manifest.PickupFacility;
import com.uncommongoods.tugboat.engine.manifest.PickupGroup;

import java.time.*;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class RatedTugboatState extends TugboatStateBase {
    public RatedTugboatState(Tugboat tugboat) {
        super(tugboat);
        this.packageState = State.RATED;
    }

    @Override
    public TugboatStateBase initialize() throws TugboatException {
        throw new IllegalStateException("Cannot initialize from rated state. Already past initialization.");
    }

    @Override
    public TugboatStateBase rate() {
        throw new IllegalStateException("Cannot rate from rated state. Already rated.");
    }

    @Override
    public TugboatStateBase shop() throws TugboatException {
        Tugboat tugboat = getTugboat();
        eliminateNonManifestRates(tugboat);

        doRateShop(tugboat);

        setManifestBatchId(tugboat);

        return new ShoppedTugboatState(getTugboat());
    }

    private void eliminateNonManifestRates(Tugboat tugboat) {
        Set<CarrierService> rateServices = tugboat.getRates().stream()
            .map(rate  -> new CarrierService(rate.getCarrier(), rate.getService()))
            .collect(Collectors.toSet());
        if (tugboat.getPickupFacility() != null && tugboat.getPickupFacility().getPickupGroups() != null) {
            PickupFacility pickupFacility = tugboat.getPickupFacility();
            Set<CarrierService> availableServices = pickupFacility.getPickupGroups().stream()
                .map(PickupGroup::getPickupGroupId)
                .flatMap(groupId -> pickupFacility.getAvailableServices(groupId).stream())
                .collect(Collectors.toSet());
            rateServices.retainAll(availableServices);
        }
        List<IRate> availableRates = tugboat.getRates().stream()
            .filter(rate -> {
                CarrierService carrierService = new CarrierService(rate.getCarrier(), rate.getService());
                return rateServices.contains(carrierService);
            })
            .filter(rate -> rate.getRate() > 0.01f)
            .map(cloneRate())
            .collect(Collectors.toList());
        tugboat.setRates(availableRates);
    }

    private Function<IRate, IRate> cloneRate() {
        return rate -> {
            try {
                return rate.clone();
            } catch (CloneNotSupportedException e) {
                return rate;
            }
        };
    }


    private void doRateShop (Tugboat tugboat) throws TugboatException {
        boolean hasDeliverableService = tugboat.getRates().stream()
            .anyMatch(isRateDeliverable());

        boolean hasUserSelectedCarrierService = Optional.ofNullable(tugboat.getOptions())
            .orElse(new TugboatOptions()).getSelectedCarrierService() != null;

        boolean doGetLowestRate = Optional.ofNullable(tugboat.getOptions())
            .orElse(new TugboatOptions())
            .isSelectLowestRate();

        if (hasDeliverableService || hasUserSelectedCarrierService) {
            if (hasUserSelectedCarrierService) {
                CarrierService userSelectedService = tugboat.getOptions().getSelectedCarrierService();
                IRate rate = tugboat.getRates().stream()
                    .filter(irate -> {
                        CarrierService carrierService = new CarrierService(irate.getCarrier(), irate.getService());
                        return carrierService.equals(userSelectedService);
                    })
                    .min(Comparator.comparing(IRate::getRate))
                    .orElse(null);
                tugboat.setSelectedRate(rate);
            } else {
                IRate rate = tugboat.getRates().stream()
                    .filter(isRateDeliverable())
                    .min(Comparator.comparing(IRate::getRate))
                    .orElse(null);
                tugboat.setSelectedRate(rate);
            }
        } else if (doGetLowestRate) {
            IRate rate = tugboat.getRates().stream()
                .min(Comparator.comparing(IRate::getRate))
                .orElse(null);
            tugboat.setSelectedRate(rate);
        }
        if (tugboat.getSelectedRate() == null) {
            throw new TugboatException("No deliverable service found");
        }
    }


    private boolean rateWillArriveOnTime(IRate rate, IShipment shipment) {
        Tugboat tugboat = getTugboat();
        LocalDate expectedDeliveryUtc = tugboat.getExpectedDeliveryDate()
            .atStartOfDay(ZoneId.systemDefault())
            .withZoneSameInstant(ZoneOffset.UTC)
            .toLocalDate();

        if (rate.getDeliveryDate() != null) {
            Instant deliveryInstant = Instant.parse(rate.getDeliveryDate());
            LocalDate deliveryDateUtc = deliveryInstant.atZone(ZoneOffset.UTC).toLocalDate();

            return (deliveryDateUtc.isBefore(expectedDeliveryUtc) ||
                deliveryDateUtc.isEqual(expectedDeliveryUtc));
        } else if (rate.getDeliveryDays() != null && rate.getDeliveryDays().intValue() > 0 && shipment != null) {
            // if there is a manifest date that is in the future, (e.g., tomorrow) the date advance option will be set and the client should return a label date
            Instant labelDateInstant = Instant.parse(Optional.ofNullable(shipment.getOptions().get("label_date"))
                .orElse(Instant.now()).toString());
            LocalDate labelDateUtc = labelDateInstant.atZone(ZoneOffset.UTC).toLocalDate();

            LocalDate calculatedExpectedDate = labelDateUtc.plusDays(rate.getDeliveryDays().intValue() + 1);

            return (calculatedExpectedDate.isBefore(expectedDeliveryUtc) ||
                calculatedExpectedDate.isEqual(expectedDeliveryUtc));
        }
        return false;
    }

    private Predicate<IRate> isRateDeliverable () {
        return rate -> {
            IShipment shipment = this.getShipmentById(rate.getShipmentId());
            return this.getTugboat().getExpectedDeliveryDate() == null || rateWillArriveOnTime(rate, shipment);
        };
    }

    private IShipment getShipmentById(String id) {
        Tugboat tugboat = this.getTugboat();
        if (tugboat.getParcels() != null && tugboat.getParcels().size() > 1) {
            return tugboat.getOrderRateResponses().stream()
                .flatMap(orders -> orders.getShipments().stream())
                .filter(shipment -> shipment.getId().equals(id))
                .findFirst()
                .orElse(null);
        } else {
            return tugboat.getShipmentRateResponses().stream()
                .filter(shipment -> shipment.getId().equals(id))
                .findFirst()
                .orElse(null);
        }
    }

    private void setManifestBatchId(Tugboat tugboat) {
        if (tugboat.getPickupFacility() != null) {
            IRate selectedRate = tugboat.getSelectedRate();
            CarrierService selectedService = new CarrierService(selectedRate.getCarrier(), selectedRate.getService());
            tugboat.getPickupFacility().getPickupGroups().stream()
                .filter(manGroup -> manGroup.getCarrierAccountId().equals(selectedRate.getCarrierAccountId()))
                .filter(manGroup -> manGroup.getAvailableServices().contains(selectedService))
                .findFirst()
                .ifPresent(manGroup -> tugboat.setManifestBatchId(manGroup.getManifestBatchId()));
        }
    }

    @Override
    public TugboatStateBase purchase() {
        throw new IllegalStateException("Cannot purchase from rated state. Must shop first.");
    }

    @Override
    public List<IPostageLabel> print() {
        throw new IllegalStateException("Cannot print from rated state. Must shop and purchase first.");
    }

    @Override
    public List<IPostageLabel> reprint() {
        throw new IllegalStateException("Cannot reprint from rated state. Must print first.");
    }

    @Override
    public TugboatStateBase voidLabel() {
        throw new IllegalStateException("Cannot void label from rated state. Must print first.");
    }
}
