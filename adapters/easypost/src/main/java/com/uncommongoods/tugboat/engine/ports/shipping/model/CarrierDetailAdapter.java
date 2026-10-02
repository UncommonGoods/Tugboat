package com.uncommongoods.tugboat.engine.ports.shipping.model;

import com.easypost.model.CarrierDetail;

public class CarrierDetailAdapter implements ICarrierDetail {
    private final CarrierDetail carrierDetail;

    public CarrierDetailAdapter(CarrierDetail carrierDetail) {
        this.carrierDetail = carrierDetail;
    }

    @Override
    public String getService() {
        return carrierDetail.getService();
    }

    @Override
    public String getContainerType() {
        return carrierDetail.getContainerType();
    }

    @Override
    public String getEstDeliveryDateLocal() {
        return carrierDetail.getEstDeliveryDateLocal();
    }

    @Override
    public String getEstDeliveryTimeLocal() {
        return carrierDetail.getEstDeliveryTimeLocal();
    }

    @Override
    public String getOriginLocation() {
        return carrierDetail.getOriginLocation();
    }

    @Override
    public String getDestinationLocation() {
        return carrierDetail.getDestinationLocation();
    }

    @Override
    public String getGuaranteedDeliveryDate() {
        return carrierDetail.getGuaranteedDeliveryDate();
    }

    @Override
    public String getAlternateIdentifier() {
        return carrierDetail.getAlternateIdentifier();
    }

    @Override
    public String getInitialDeliveryAttempt() {
        return carrierDetail.getInitialDeliveryAttempt();
    }

    public CarrierDetail getCarrierDetail() {
        return carrierDetail;
    }
}
