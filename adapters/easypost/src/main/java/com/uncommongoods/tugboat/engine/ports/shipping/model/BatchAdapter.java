package com.uncommongoods.tugboat.engine.ports.shipping.model;

import com.easypost.model.*;

import java.util.List;
import java.util.stream.Collectors;

public class BatchAdapter extends EasyPostResource implements IBatch {
    private final Batch batch;

    public BatchAdapter(Batch batch) {
        super();
        this.batch = batch;
    }

    @Override
    public String getState() {
        return batch.getState();
    }

    @Override
    public IBatchStatus getStatus() {
        BatchStatus status = batch.getStatus();
        return status != null ? new BatchStatusAdapter(status) : null;
    }

    @Override
    public Number getNumShipments() {
        return batch.getNumShipments();
    }

    @Override
    public List<IShipment> getShipments() {
        List<Shipment> shipments = batch.getShipments();
        if (shipments == null) {
            return null;
        }

        return shipments.stream()
            .map(ShipmentAdapter::new)
            .collect(Collectors.toList());
    }

    @Override
    public String getLabelUrl() {
        return batch.getLabelUrl();
    }

    @Override
    public IScanForm getScanForm() {
        ScanForm scanForm = batch.getScanForm();
        return scanForm != null ? new ScanFormAdapter(scanForm) : null;
    }

    @Override
    public String getReference() {
        return batch.getReference();
    }

    public Batch getBatch() {
        return batch;
    }
}
