package com.uncommongoods.tugboat.engine.ports.shipping.model;

import com.easypost.model.EasyPostResource;
import com.easypost.model.Refund;

public class RefundAdapter extends EasyPostResource implements IRefund {
    private final Refund refund;

    public RefundAdapter(Refund refund) {
        super();
        this.refund = refund;
    }

    @Override
    public String getTrackingCode() {
        return refund.getTrackingCode();
    }

    @Override
    public String getConfirmationNumber() {
        return refund.getConfirmationNumber();
    }

    @Override
    public String getStatus() {
        return refund.getStatus();
    }

    @Override
    public String getCarrier() {
        return refund.getCarrier();
    }

    @Override
    public String getShipmentId() {
        return refund.getShipmentId();
    }

    public Refund getRefund() {
        return refund;
    }
}
