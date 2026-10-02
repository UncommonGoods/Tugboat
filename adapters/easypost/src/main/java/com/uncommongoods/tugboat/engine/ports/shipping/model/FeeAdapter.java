package com.uncommongoods.tugboat.engine.ports.shipping.model;

import com.easypost.model.Fee;

public class FeeAdapter implements IFee {
    private final Fee fee;

    public FeeAdapter(Fee fee) {
        this.fee = fee;
    }

    @Override
    public String getType() {
        return fee.getType();
    }

    @Override
    public float getAmount() {
        return fee.getAmount();
    }

    @Override
    public Boolean getCharged() {
        return fee.getCharged();
    }

    @Override
    public Boolean getRefunded() {
        return fee.getRefunded();
    }

    public Fee getFee() {
        return fee;
    }
}
