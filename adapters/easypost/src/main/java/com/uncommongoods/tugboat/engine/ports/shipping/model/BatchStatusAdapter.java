package com.uncommongoods.tugboat.engine.ports.shipping.model;

import com.easypost.model.BatchStatus;

public class BatchStatusAdapter implements IBatchStatus {
    private final BatchStatus batchStatus;

    public BatchStatusAdapter(BatchStatus batchStatus) {
        this.batchStatus = batchStatus;
    }

    @Override
    public int getCreated() {
        return batchStatus.getCreated();
    }

    @Override
    public int getCreationFailed() {
        return batchStatus.getCreationFailed();
    }

    @Override
    public int getPostagePurchased() {
        return batchStatus.getPostagePurchased();
    }

    @Override
    public int getPostagePurchaseFailed() {
        return batchStatus.getPostagePurchaseFailed();
    }

    public BatchStatus getBatchStatus() {
        return batchStatus;
    }
}
