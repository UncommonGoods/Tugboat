// Copyright (c) 2025 Uncommon Goods LLC
// SPDX-License-Identifier: MPL-2.0

package com.uncommongoods.tugboat.engine.state;

import com.uncommongoods.tugboat.engine.Tugboat;
import com.uncommongoods.tugboat.engine.TugboatOptions;
import com.uncommongoods.tugboat.engine.ports.shipping.model.*;
import com.uncommongoods.tugboat.engine.exception.TugboatException;
import com.uncommongoods.tugboat.engine.ports.shipping.service.IShippingClient;
import com.uncommongoods.tugboat.engine.manifest.CarrierService;
import com.uncommongoods.tugboat.engine.manifest.CarrierServiceGroup;
import com.uncommongoods.tugboat.engine.manifest.PickupGroup;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.uncommongoods.tugboat.engine.config.EngineConfig.DEFAULT_CLIENT_KEY;

public class InitializedTugboatState extends TugboatStateBase {

    public InitializedTugboatState(Tugboat tugboat) {
        super(tugboat);
        this.packageState = State.INITIALIZED;
    }

    @Override
    public TugboatStateBase initialize() throws TugboatException {
        throw new IllegalStateException("Cannot initialize from initialized state. Already initialized.");
    }

    @Override
    public TugboatStateBase rate() throws TugboatException {
        Tugboat tugboat = getTugboat();

        createRateResponses(tugboat);

        setRates(tugboat);

        setGlobalCarrierServices(tugboat);

        return new RatedTugboatState(tugboat);
    }

    @Override
    public TugboatStateBase shop() {
        throw new IllegalStateException("Cannot shop from initialized state. Must rate first.");
    }

    @Override
    public TugboatStateBase purchase() {
        throw new IllegalStateException("Cannot purchase from initialized state. Must rate and shop first.");
    }

    @Override
    public List<IPostageLabel> print() {
        throw new IllegalStateException("Cannot print from initialized state. Must rate, shop, and purchase first.");
    }

    @Override
    public List<IPostageLabel> reprint() {
        throw new IllegalStateException("Cannot reprint from initialized state. Must print first.");
    }

    @Override
    public TugboatStateBase voidLabel() {
        throw new IllegalStateException("Cannot void label from initialized state. Must print first.");
    }

    private void createRateResponses(Tugboat tugboat) throws TugboatException {
        String selectedClient = tugboat.getOptions() != null ? tugboat.getShippingClientKey() : DEFAULT_CLIENT_KEY;
        IShippingClient shippingClient = tugboat.getEngineConfig().getShippingClients().get(selectedClient);
        if (shippingClient == null) {
            throw new TugboatException("Shipping client not found: " + selectedClient);
        }

        if (tugboat.getPickupFacility() == null) {
            createSingleRateResponse(tugboat, shippingClient);
        } else {
            createManifestBasedRateResponses(tugboat);
        }
    }

    private void createSingleRateResponse(Tugboat tugboat, IShippingClient shippingClient) throws TugboatException {
        List<IParcel> parcels = tugboat.getParcels();
        validateParcels(parcels);
        List<String> carrierAccountIds = Optional.ofNullable(tugboat.getOptions()).orElse(new TugboatOptions()).getCarrierAccountIds();
        Map<String, Object> params = buildShippingParams(tugboat, null, carrierAccountIds);
        if (parcels.size() > 1) {
            // EP Orders are only supported by AustraliaPost, DHLExpress, DPD, DPDUK, Fastway, FedEx, UPS, and Purolator
            IOrder order = shippingClient.getOrderService().create(params);
            if (order != null) {
                tugboat.setOrderRateResponses(new ArrayList<>(List.of(order)));
            }
        } else {
            IShipment shipment = shippingClient.getShipmentService().create(params);
            if(shipment != null) {
                tugboat.setShipmentRateResponses(new ArrayList<>(List.of(shipment)));
            }
        }
    }

    private void createManifestBasedRateResponses(Tugboat tugboat) throws TugboatException {
        List<PickupGroup> pickupGroups = tugboat.getPickupFacility().getPickupGroups();
        if (pickupGroups == null || pickupGroups.isEmpty()) {
            throw new TugboatException("Cannot create rate response: no manifest groups in facility");
        }

        // carrier account IDs grouped by unique (shipping client key, manifest date)
        Map<ManifestKey, List<String>> groupedCarrierAccountIds = pickupGroups.stream()
            .filter(group -> group.getPickupDate() != null)
            .filter(group -> tugboat.getOptions().getCarrierAccountIds() == null ||
                tugboat.getOptions().getCarrierAccountIds().contains(group.getCarrierAccountId()))
            .collect(Collectors.groupingBy(
                group -> new ManifestKey(
                    group.getShippingClientKey() != null ? group.getShippingClientKey() : DEFAULT_CLIENT_KEY,
                    group.getPickupDate()),
                Collectors.mapping(PickupGroup::getCarrierAccountId, Collectors.toList())
            ));

        if (groupedCarrierAccountIds.isEmpty()) {
            throw new TugboatException("Cannot create rate response: no valid manifest dates found");
        }

        List<IShipment> shipments = new ArrayList<>();
        List<IOrder> orders = new ArrayList<>();

        for (Map.Entry<ManifestKey, List<String>> entry : groupedCarrierAccountIds.entrySet()) {
            ManifestKey manifestKey = entry.getKey();
            List<String> carrierAccountIds = entry.getValue();
            IShippingClient shippingClient = tugboat.getEngineConfig().getShippingClients().get(manifestKey.shippingClientKey());
            if (shippingClient == null) {
                throw new TugboatException("Shipping client not found: " + manifestKey.shippingClientKey());
            }
            Map<String, Object> params = buildShippingParams(tugboat, manifestKey.pickupDate(), carrierAccountIds);
            List<IParcel> parcels = tugboat.getParcels();
            validateParcels(parcels);
            if (tugboat.getParcels().size() > 1) {
                IOrder order = shippingClient.getOrderService().create(params);
                if(order != null) {
                    orders.add(order);
                }
            } else {
                IShipment shipment = shippingClient.getShipmentService().create(params);
                if(shipment != null) {
                    shipments.add(shipment);
                }
            }
        }

        if (tugboat.getParcels().size() > 1) {
            tugboat.setOrderRateResponses(orders);
        } else {
            tugboat.setShipmentRateResponses(shipments);
        }
    }

    private record ManifestKey(String shippingClientKey, LocalDate pickupDate) {}

    private Map<String, Object> buildShippingParams(Tugboat tugboat, LocalDate manifestDate, List<String> carrierAccountIds) {
        Map<String, Object> params = new java.util.HashMap<>();

        if (tugboat.getOriginAddress() != null) {
            params.put("from_address", tugboat.getOriginAddress().toMap());
        }
        if (tugboat.getDestinationAddress() != null) {
            params.put("to_address", tugboat.getDestinationAddress().toMap());
        }
        if (tugboat.getReturnAddress() != null) {
            params.put("return_address", tugboat.getReturnAddress().toMap());
        }

        if (carrierAccountIds != null && !carrierAccountIds.isEmpty()) {
            params.put("carrier_accounts", carrierAccountIds);
        }

        if (tugboat.getOptions() == null || tugboat.getOptions().toMap().isEmpty()) {
            tugboat.setOptions(new TugboatOptions());
        }
        TugboatOptions tugboatOptions = tugboat.getOptions().clone();
        if (manifestDate != null) {
            int dateAdvance = calculateDateAdvance(manifestDate);
            tugboatOptions.setDateAdvance(dateAdvance);
        }
        tugboat.getOptions().validateOptions();

        params.put("options", tugboatOptions.toMap());

        if (tugboat.getReference() != null) {
            params.put("reference", tugboat.getReference());
        }


        List<IParcel> parcels = tugboat.getParcels();
        if (parcels.size() > 1) {
            List<Map<String, Object>> parcelMaps = parcels.stream()
                .map(IParcel::toMap)
                .toList();
            params.put("shipments", parcelMaps.stream()
                .map(parcelMap -> {
                    Map<String, Object> shipmentMap = new java.util.HashMap<>();
                    shipmentMap.put("parcel", parcelMap);
                    if (tugboat.getOriginAddress() != null) {
                        shipmentMap.put("from_address", tugboat.getOriginAddress().toMap());
                    }
                    if (tugboat.getDestinationAddress() != null) {
                        shipmentMap.put("to_address", tugboat.getDestinationAddress().toMap());
                    }
                    if (tugboat.getReturnAddress() != null) {
                        shipmentMap.put("return_address", tugboat.getReturnAddress().toMap());
                    }
                    if (tugboat.getOptions() != null && !tugboatOptions.toMap().isEmpty()) {
                        shipmentMap.put("options", tugboatOptions.toMap());
                    }
                    if (tugboat.getReference() != null) {
                        params.put("reference", tugboat.getReference());
                    }
                    return shipmentMap;
                })
                .toList());
        } else {
            params.put("parcel", parcels.getFirst().toMap());
        }

        return params;
    }

    private int calculateDateAdvance(LocalDate manifestDate) {
        LocalDate today = LocalDate.now();
        return Math.max(0, (int) (manifestDate.toEpochDay() - today.toEpochDay()));
    }

    private void setRates(Tugboat tugboat) {
        List<IRate> rates;
        if (tugboat.getOrderRateResponses() != null && !tugboat.getOrderRateResponses().isEmpty()) {
            rates = tugboat.getOrderRateResponses().stream()
                .flatMap(rateRes -> rateRes.getRates().stream())
                .toList();
        } else if (tugboat.getShipmentRateResponses() != null && !tugboat.getShipmentRateResponses().isEmpty()) {
            rates = tugboat.getShipmentRateResponses().stream()
                .flatMap(rateRes -> rateRes.getRates().stream())
                .toList();
        } else {
            return;
        }
        tugboat.setRates(rates);
    }

    private void setGlobalCarrierServices(Tugboat tugboat) throws TugboatException {
        CompletableFuture.runAsync(() -> {
            try {
                if (tugboat.getRates() == null || tugboat.getRates().isEmpty()) {
                    return;
                }
                if (tugboat.getPickupFacility() == null) {
                    return;
                }

                IShippingClient shippingClient = tugboat.getEngineConfig().getShippingClient(tugboat.getShippingClientKey());
                List<ICarrierAccount> carrierAccounts = shippingClient.getCarrierAccountService().all();

                Set<String> carrierAccountIds = tugboat.getRates().stream()
                    .map(IRate::getCarrierAccountId)
                    .collect(Collectors.toSet());
                Map<String, ICarrierAccount> carrierAccountMap = carrierAccounts.stream()
                    .filter(carrierAccount -> carrierAccountIds.contains(carrierAccount.getId()))
                    .collect(Collectors.toMap(ICarrierAccount::getId, Function.identity()));

                Set<CarrierServiceGroup> carrierServiceGroups = new HashSet<>();
                for (IRate rate : tugboat.getRates()) {
                    ICarrierAccount carrierAccount = carrierAccountMap.get(rate.getCarrierAccountId());
                    if (carrierAccount == null) continue;
                    CarrierService carrierService = new CarrierService(rate.getCarrier(), rate.getService());
                    Optional<CarrierServiceGroup> carServiceGroupOpt = carrierServiceGroups.stream()
                        .filter(carServGroup -> carServGroup.getCarrierAccountIds().contains(carrierAccount.getId()))
                        .findFirst();
                    if (carServiceGroupOpt.isPresent()) {
                        CarrierServiceGroup carServiceGroup  = carServiceGroupOpt.get();
                        carServiceGroup.getCarrierAccountIds().add(rate.getCarrierAccountId());
                        carServiceGroup.getCarrierServices().add(carrierService);
                    } else {
                        Set<String> groupCarrierAccountIds = new HashSet<>(Set.of(carrierAccount.getId()));
                        Set<CarrierService> carrierServices = new HashSet<>(Set.of(carrierService));
                        carrierServiceGroups.add(new CarrierServiceGroup(carrierAccount.getReadable(), groupCarrierAccountIds, carrierServices));
                    }
                }
                tugboat.getPickupFacility().setGlobalCarrierServices(carrierServiceGroups);
            } catch (Exception e) {
                System.err.println("Failed to set global carrier services: " + e.getMessage());
            }
        });
    }

    private void validateParcels(List<IParcel> parcels) throws TugboatException {
        if (parcels == null || parcels.isEmpty()) {
            throw new TugboatException("no parcels provided");
        }
        for(IParcel parcel: parcels) {
            if (parcel.getLength() == null || parcel.getLength() <= 0) {
                throw new TugboatException("parcel length must be greater than zero");
            }
            if (parcel.getWidth() == null || parcel.getWidth() <= 0) {
                throw new TugboatException("parcel width must be greater than zero");
            }
            if (parcel.getHeight() == null || parcel.getHeight() <= 0) {
                throw new TugboatException("parcel height must be greater than zero");
            }
            if (parcel.getWeight() == null || parcel.getWeight() <= 0) {
                throw new TugboatException("parcel weight must be greater than zero");
            }
        }
    }
}
