package com.uncommongoods.tugboat.engine.ports.shipping.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.uncommongoods.tugboat.engine.exception.TugboatException;
import com.easypost.model.Batch;
import com.easypost.model.BatchCollection;
import com.easypost.model.Shipment;
import com.easypost.service.BatchService;
import com.easypost.service.EasyPostClient;
import com.uncommongoods.tugboat.engine.ports.shipping.model.BatchAdapter;
import com.uncommongoods.tugboat.engine.ports.shipping.model.BatchCollectionAdapter;
import com.uncommongoods.tugboat.engine.ports.shipping.model.ShipmentAdapter;
import com.uncommongoods.tugboat.engine.ports.shipping.model.IBatch;
import com.uncommongoods.tugboat.engine.ports.shipping.model.IBatchCollection;
import com.uncommongoods.tugboat.engine.ports.shipping.model.IShipment;


public class BatchServiceAdapter implements IBatchService {
    private final BatchService batchService;

    public BatchServiceAdapter(EasyPostClient client) {
        this.batchService = client.batch;
    }

    public BatchServiceAdapter(BatchService batchService) {
        this.batchService = batchService;
    }

    @Override
    public IBatch create() throws TugboatException {
        try {
            Batch batch = batchService.create();
            return batch != null ? new BatchAdapter(batch) : null;
        } catch (com.easypost.exception.EasyPostException e) {
            throw new TugboatException(e.getMessage(), e);
        }
    }

    @Override
    public IBatch create(Map<String, Object> params) throws TugboatException {
        try {
            Batch batch = batchService.create(params);
            return batch != null ? new BatchAdapter(batch) : null;
        } catch (com.easypost.exception.EasyPostException e) {
            throw new TugboatException(e.getMessage(), e);
        }
    }

    @Override
    public IBatch retrieve(String id) throws TugboatException {
        try {
            Batch batch = batchService.retrieve(id);
            return batch != null ? new BatchAdapter(batch) : null;
        } catch (com.easypost.exception.EasyPostException e) {
            throw new TugboatException(e.getMessage(), e);
        }
    }

    @Override
    public IBatchCollection all(Map<String, Object> params) throws TugboatException {
        try {
            BatchCollection batchCollection = batchService.all(params);
            return batchCollection != null ? new BatchCollectionAdapter(batchCollection) : null;
        } catch (com.easypost.exception.EasyPostException e) {
            throw new TugboatException(e.getMessage(), e);
        }
    }

    @Override
    public IBatch label(String id, Map<String, Object> params) throws TugboatException {
        try {
            Batch batch = batchService.label(id, params);
            return batch != null ? new BatchAdapter(batch) : null;
        } catch (com.easypost.exception.EasyPostException e) {
            throw new TugboatException(e.getMessage(), e);
        }
    }

    @Override
    public IBatch addShipments(String id, Map<String, Object> params) throws TugboatException {
        try {
            Batch batch = batchService.addShipments(id, params);
            return batch != null ? new BatchAdapter(batch) : null;
        } catch (com.easypost.exception.EasyPostException e) {
            throw new TugboatException(e.getMessage(), e);
        }
    }

    @Override
    public IBatch addShipments(String id, List<IShipment> shipments) throws TugboatException {
        List<Shipment> shipmentList = new ArrayList<>();
        for (IShipment shipment : shipments) {
            if (shipment instanceof ShipmentAdapter) {
                shipmentList.add(((ShipmentAdapter) shipment).getShipment());
            } else {
                throw new IllegalArgumentException("Shipment must be a ShipmentAdapter");
            }
        }

        try {
            Batch batch = batchService.addShipments(id, shipmentList);
            return batch != null ? new BatchAdapter(batch) : null;
        } catch (com.easypost.exception.EasyPostException e) {
            throw new TugboatException(e.getMessage(), e);
        }
    }

    @Override
    public IBatch removeShipments(String id, Map<String, Object> params) throws TugboatException {
        try {
            Batch batch = batchService.removeShipments(id, params);
            return batch != null ? new BatchAdapter(batch) : null;
        } catch (com.easypost.exception.EasyPostException e) {
            throw new TugboatException(e.getMessage(), e);
        }
    }

    @Override
    public IBatch removeShipments(String id, List<IShipment> shipments) throws TugboatException {
        List<Shipment> shipmentList = new ArrayList<>();
        for (IShipment shipment : shipments) {
            if (shipment instanceof ShipmentAdapter) {
                shipmentList.add(((ShipmentAdapter) shipment).getShipment());
            } else {
                throw new IllegalArgumentException("Shipment must be a ShipmentAdapter");
            }
        }

        try {
            Batch batch = batchService.removeShipments(id, shipmentList);
            return batch != null ? new BatchAdapter(batch) : null;
        } catch (com.easypost.exception.EasyPostException e) {
            throw new TugboatException(e.getMessage(), e);
        }
    }

    @Override
    public IBatch buy(String id) throws TugboatException {
        try {
            Batch batch = batchService.buy(id);
            return batch != null ? new BatchAdapter(batch) : null;
        } catch (com.easypost.exception.EasyPostException e) {
            throw new TugboatException(e.getMessage(), e);
        }
    }

    @Override
    public IBatch buy(String id, Map<String, Object> params) throws TugboatException {
        try {
            Batch batch = batchService.buy(id, params);
            return batch != null ? new BatchAdapter(batch) : null;
        } catch (com.easypost.exception.EasyPostException e) {
            throw new TugboatException(e.getMessage(), e);
        }
    }

    @Override
    public IBatch createScanForm(String id) throws TugboatException {
        try {
            Batch batch = batchService.createScanForm(id);
            return batch != null ? new BatchAdapter(batch) : null;
        } catch (com.easypost.exception.EasyPostException e) {
            throw new TugboatException(e.getMessage(), e);
        }
    }

    @Override
    public IBatch createScanForm(String id, Map<String, Object> params) throws TugboatException {
        try {
            Batch batch = batchService.createScanForm(id, params);
            return batch != null ? new BatchAdapter(batch) : null;
        } catch (com.easypost.exception.EasyPostException e) {
            throw new TugboatException(e.getMessage(), e);
        }
    }

    public BatchService getBatchService() {
        return batchService;
    }
}
