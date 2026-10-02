package com.uncommongoods.tugboat.engine.ports.shipping.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.easypost.exception.EasyPostException;
import com.uncommongoods.tugboat.engine.exception.TugboatException;
import com.uncommongoods.tugboat.engine.exception.EndOfPaginationException;
import com.easypost.model.EstimatedDeliveryDate;
import com.easypost.model.Rate;
import com.easypost.model.RecommendShipDateForShipmentResult;
import com.easypost.model.Shipment;
import com.easypost.model.ShipmentCollection;
import com.easypost.model.SmartRate;
import com.easypost.model.SmartRateAccuracy;
import com.easypost.service.EasyPostClient;
import com.easypost.service.ShipmentService;
import com.uncommongoods.tugboat.engine.ports.shipping.model.EstimatedDeliveryDateAdapter;
import com.uncommongoods.tugboat.engine.ports.shipping.model.RateAdapter;
import com.uncommongoods.tugboat.engine.ports.shipping.model.RecommendShipDateForShipmentResultAdapter;
import com.uncommongoods.tugboat.engine.ports.shipping.model.ShipmentAdapter;
import com.uncommongoods.tugboat.engine.ports.shipping.model.ShipmentCollectionAdapter;
import com.uncommongoods.tugboat.engine.ports.shipping.model.SmartRateAccuracyAdapter;
import com.uncommongoods.tugboat.engine.ports.shipping.model.SmartRateAdapter;
import com.uncommongoods.tugboat.engine.ports.shipping.model.IEstimatedDeliveryDate;
import com.uncommongoods.tugboat.engine.ports.shipping.model.IRate;
import com.uncommongoods.tugboat.engine.ports.shipping.model.IRecommendShipDateForShipmentResult;
import com.uncommongoods.tugboat.engine.ports.shipping.model.IShipment;
import com.uncommongoods.tugboat.engine.ports.shipping.model.IShipmentCollection;
import com.uncommongoods.tugboat.engine.ports.shipping.model.ISmartRate;
import com.uncommongoods.tugboat.engine.ports.shipping.model.ISmartRateAccuracy;

public class ShipmentServiceAdapter implements IShipmentService {
    private final ShipmentService shipmentService;

    public ShipmentServiceAdapter(EasyPostClient client) {
        this.shipmentService = client.shipment;
    }

    public ShipmentServiceAdapter(ShipmentService shipmentService) {
        this.shipmentService = shipmentService;
    }

    @Override
    public IShipment create(Map<String, Object> params) throws TugboatException {
        try {
            Shipment shipment = shipmentService.create(params);
            return shipment != null ? new ShipmentAdapter(shipment) : null;
        } catch (EasyPostException e) {
            throw new TugboatException(e.getMessage(), e);
        }
    }

    @Override
    public IShipment retrieve(String id) throws TugboatException {
        try {
            Shipment shipment = shipmentService.retrieve(id);
            return shipment != null ? new ShipmentAdapter(shipment) : null;
        } catch (EasyPostException e) {
            throw new TugboatException(e.getMessage(), e);
        }
    }

    @Override
    public IShipmentCollection all(Map<String, Object> params) throws TugboatException {
        try {
            ShipmentCollection collection = shipmentService.all(params);
            return collection != null ? new ShipmentCollectionAdapter(collection) : null;
        } catch (EasyPostException e) {
            throw new TugboatException(e.getMessage(), e);
        }
    }

    @Override
    public IShipmentCollection getNextPage(IShipmentCollection collection) throws EndOfPaginationException {
        if (!(collection instanceof ShipmentCollectionAdapter)) {
            throw new IllegalArgumentException("Collection must be an instance of ShipmentCollectionAdapter");
        }

        try {
            ShipmentCollection concreteCollection = ((ShipmentCollectionAdapter) collection).getCollection();
            ShipmentCollection nextPage = shipmentService.getNextPage(concreteCollection);
            return nextPage != null ? new ShipmentCollectionAdapter(nextPage) : null;
        } catch (EasyPostException e) {
            throw new EndOfPaginationException(e.getMessage(), e);
        }
    }

    @Override
    public IShipmentCollection getNextPage(IShipmentCollection collection, Integer pageSize) throws EndOfPaginationException {
        if (!(collection instanceof ShipmentCollectionAdapter)) {
            throw new IllegalArgumentException("Collection must be an instance of ShipmentCollectionAdapter");
        }

        try {
            ShipmentCollection concreteCollection = ((ShipmentCollectionAdapter) collection).getCollection();
            ShipmentCollection nextPage = shipmentService.getNextPage(concreteCollection, pageSize);
            return nextPage != null ? new ShipmentCollectionAdapter(nextPage) : null;
        } catch (EasyPostException e) {
            throw new EndOfPaginationException(e.getMessage(), e);
        }
    }

    @Override
    public IShipment newRates(String id) throws TugboatException {
        try {
            Shipment shipment = shipmentService.newRates(id);
            return shipment != null ? new ShipmentAdapter(shipment) : null;
        } catch (EasyPostException e) {
            throw new TugboatException(e.getMessage(), e);
        }
    }

    @Override
    public IShipment newRates(String id, Map<String, Object> params) throws TugboatException {
        try {
            Shipment shipment = shipmentService.newRates(id, params);
            return shipment != null ? new ShipmentAdapter(shipment) : null;
        } catch (EasyPostException e) {
            throw new TugboatException(e.getMessage(), e);
        }
    }

    @Override
    public List<ISmartRate> smartRates(String id) throws TugboatException {
        try {
            List<SmartRate> smartRates = shipmentService.smartRates(id);
            if (smartRates == null) {
                return null;
            }
            List<ISmartRate> adaptedSmartRates = new ArrayList<>();
            for (SmartRate smartRate : smartRates) {
                adaptedSmartRates.add(new SmartRateAdapter(smartRate));
            }
            return adaptedSmartRates;
        } catch (EasyPostException e) {
            throw new TugboatException(e.getMessage(), e);
        }
    }

    @Override
    public List<ISmartRate> smartRates(String id, Map<String, Object> params) throws TugboatException {
        try {
            List<SmartRate> smartRates = shipmentService.smartRates(id, params);
            if (smartRates == null) {
                return null;
            }
            List<ISmartRate> adaptedSmartRates = new ArrayList<>();
            for (SmartRate smartRate : smartRates) {
                adaptedSmartRates.add(new SmartRateAdapter(smartRate));
            }
            return adaptedSmartRates;
        } catch (EasyPostException e) {
            throw new TugboatException(e.getMessage(), e);
        }
    }

    @Override
    public IShipment buy(String id, Map<String, Object> params) throws TugboatException {
        try {
            Shipment shipment = shipmentService.buy(id, params);
            return shipment != null ? new ShipmentAdapter(shipment) : null;
        } catch (EasyPostException e) {
            String message = "easypost id: " + id + " - " + e.getMessage();
            throw new TugboatException(message, e);
        }
    }

    @Override
    public IShipment buy(String id, IRate rate) throws TugboatException {
        if (!(rate instanceof RateAdapter)) {
            Map<String, Object> params = new HashMap<>();
            params.put("rate", rate);
            return buy(id, params);
        }

        try {
            Rate concreteRate = ((RateAdapter) rate).getERate();
            Shipment shipment = shipmentService.buy(id, concreteRate);
            return shipment != null ? new ShipmentAdapter(shipment) : null;
        } catch (EasyPostException e) {
            String message = "easypost id: " + id + " - " + e.getMessage();
            throw new TugboatException(message, e);
        }
    }

    @Override
    public IShipment buy(String id, IRate rate, String endShipperId) throws TugboatException {
        if (!(rate instanceof RateAdapter)) {
            Map<String, Object> params = new HashMap<>();
            params.put("rate", rate);
            return buy(id, params, endShipperId);
        }

        try {
            Rate concreteRate = ((RateAdapter) rate).getERate();
            Shipment shipment = shipmentService.buy(id, concreteRate, endShipperId);
            return shipment != null ? new ShipmentAdapter(shipment) : null;
        } catch (EasyPostException e) {
            throw new TugboatException(e.getMessage(), e);
        }
    }

    @Override
    public IShipment buy(String id, Map<String, Object> params, String endShipperId) throws TugboatException {
        try {
            Shipment shipment = shipmentService.buy(id, params, endShipperId);
            return shipment != null ? new ShipmentAdapter(shipment) : null;
        } catch (EasyPostException e) {
            throw new TugboatException(e.getMessage(), e);
        }
    }

    @Override
    public IShipment refund(String id) throws TugboatException {
        try {
            Shipment shipment = shipmentService.refund(id);
            return shipment != null ? new ShipmentAdapter(shipment) : null;
        } catch (EasyPostException e) {
            throw new TugboatException(e.getMessage(), e);
        }
    }

    @Override
    public IShipment refund(String id, Map<String, Object> params) throws TugboatException {
        try {
            Shipment shipment = shipmentService.refund(id, params);
            return shipment != null ? new ShipmentAdapter(shipment) : null;
        } catch (EasyPostException e) {
            throw new TugboatException(e.getMessage(), e);
        }
    }

    @Override
    public IShipment label(String id, Map<String, Object> params) throws TugboatException {
        try {
            Shipment shipment = shipmentService.label(id, params);
            return shipment != null ? new ShipmentAdapter(shipment) : null;
        } catch (EasyPostException e) {
            throw new TugboatException(e.getMessage(), e);
        }
    }

    @Override
    public IShipment insure(String id, Map<String, Object> params) throws TugboatException {
        try {
            Shipment shipment = shipmentService.insure(id, params);
            return shipment != null ? new ShipmentAdapter(shipment) : null;
        } catch (EasyPostException e) {
            throw new TugboatException(e.getMessage(), e);
        }
    }

    @Override
    public ISmartRate lowestSmartRate(String id, int deliveryDay, ISmartRateAccuracy deliveryAccuracy) throws TugboatException {
        try {
            SmartRateAccuracy concreteAccuracy = null;
            if (deliveryAccuracy instanceof SmartRateAccuracyAdapter) {
                concreteAccuracy = ((SmartRateAccuracyAdapter) deliveryAccuracy).getSmartRateAccuracy();
            }
            SmartRate result = shipmentService.lowestSmartRate(id, deliveryDay, concreteAccuracy);
            return result != null ? new SmartRateAdapter(result) : null;
        } catch (EasyPostException e) {
            throw new TugboatException(e.getMessage(), e);
        }
    }

    @Override
    public ISmartRate findLowestSmartRate(List<ISmartRate> smartRates, int deliveryDay, ISmartRateAccuracy deliveryAccuracy) throws TugboatException {
        try {
            List<SmartRate> concreteRates = new ArrayList<>();
            if (smartRates != null) {
                for (ISmartRate smartRate : smartRates) {
                    if (smartRate instanceof SmartRateAdapter) {
                        concreteRates.add(((SmartRateAdapter) smartRate).getSmartRate());
                    }
                }
            }

            SmartRateAccuracy concreteAccuracy = null;
            if (deliveryAccuracy instanceof SmartRateAccuracyAdapter) {
                concreteAccuracy = ((SmartRateAccuracyAdapter) deliveryAccuracy).getSmartRateAccuracy();
            }

            SmartRate result = shipmentService.findLowestSmartRate(concreteRates, deliveryDay, concreteAccuracy);
            return result != null ? new SmartRateAdapter(result) : null;
        } catch (EasyPostException e) {
            throw new TugboatException(e.getMessage(), e);
        }
    }

    @Override
    public IShipment generateForm(String id, String formType) throws TugboatException {
        try {
            Shipment shipment = shipmentService.generateForm(id, formType);
            return shipment != null ? new ShipmentAdapter(shipment) : null;
        } catch (EasyPostException e) {
            throw new TugboatException(e.getMessage(), e);
        }
    }

    @Override
    public IShipment generateForm(String id, String formType, Map<String, Object> formOptions) throws TugboatException {
        try {
            Shipment shipment = shipmentService.generateForm(id, formType, formOptions);
            return shipment != null ? new ShipmentAdapter(shipment) : null;
        } catch (EasyPostException e) {
            throw new TugboatException(e.getMessage(), e);
        }
    }

    @Override
    public List<IEstimatedDeliveryDate> retrieveEstimatedDeliveryDate(String id, String plannedShipDate) throws TugboatException {
        try {
            List<EstimatedDeliveryDate> dates = shipmentService.retrieveEstimatedDeliveryDate(id, plannedShipDate);
            if (dates == null) {
                return null;
            }
            List<IEstimatedDeliveryDate> adaptedDates = new ArrayList<>();
            for (EstimatedDeliveryDate date : dates) {
                adaptedDates.add(new EstimatedDeliveryDateAdapter(date));
            }
            return adaptedDates;
        } catch (EasyPostException e) {
            throw new TugboatException(e.getMessage(), e);
        }
    }

    @Override
    public List<IRecommendShipDateForShipmentResult> recommendShipDate(String id, String desiredDeliveryDate) throws TugboatException {
        try {
            List<RecommendShipDateForShipmentResult> results = shipmentService.recommendShipDate(id, desiredDeliveryDate);
            if (results == null) {
                return null;
            }
            List<IRecommendShipDateForShipmentResult> adaptedResults = new ArrayList<>();
            for (RecommendShipDateForShipmentResult result : results) {
                adaptedResults.add(new RecommendShipDateForShipmentResultAdapter(result));
            }
            return adaptedResults;
        } catch (EasyPostException e) {
            throw new TugboatException(e.getMessage(), e);
        }
    }

    @Override
    public void setShipment(IShipment shipment) throws TugboatException {
        // no-op
    }

    public ShipmentService getShipmentService() {
        return shipmentService;
    }
}
