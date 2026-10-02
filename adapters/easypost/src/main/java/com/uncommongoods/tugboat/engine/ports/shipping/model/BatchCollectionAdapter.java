package com.uncommongoods.tugboat.engine.ports.shipping.model;

import com.uncommongoods.tugboat.engine.exception.EndOfPaginationException;
import com.easypost.model.Batch;
import com.easypost.model.BatchCollection;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BatchCollectionAdapter extends PaginatedCollectionAdapter<IBatch, Batch, BatchCollection> implements IBatchCollection {

    public BatchCollectionAdapter(BatchCollection collection) {
        super(collection);
    }

    @Override
    public List<IBatch> getBatches() {
        List<Batch> batches = collection.getBatches();
        if (batches == null) {
            return null;
        }

        List<IBatch> adaptedBatches = new ArrayList<>();
        for (Batch batch : batches) {
            adaptedBatches.add(new BatchAdapter(batch));
        }

        return adaptedBatches;
    }

    @Override
    protected List<Batch> convertInterfaceToConcreteList(List<IBatch> interfaceEntries) {
        if (interfaceEntries == null) {
            return null;
        }

        List<Batch> concreteEntries = new ArrayList<>();
        for (IBatch item : interfaceEntries) {
            if (item instanceof BatchAdapter) {
                concreteEntries.add(((BatchAdapter) item).getBatch());
            } else {
                throw new IllegalArgumentException("Batch must be a BatchAdapter");
            }
        }

        return concreteEntries;
    }

    @Override
    public Map<String, Object> buildNextPageParameters(List<IBatch> entries, Integer pageSize) throws EndOfPaginationException {
        throw new EndOfPaginationException("BatchCollection does not support pagination");
    }
}
