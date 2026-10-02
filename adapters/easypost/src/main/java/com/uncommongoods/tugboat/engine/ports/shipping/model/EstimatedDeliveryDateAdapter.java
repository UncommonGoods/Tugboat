package com.uncommongoods.tugboat.engine.ports.shipping.model;

import com.easypost.model.EstimatedDeliveryDate;

public class EstimatedDeliveryDateAdapter implements IEstimatedDeliveryDate {
    private final EstimatedDeliveryDate estimatedDeliveryDate;

    public EstimatedDeliveryDateAdapter(EstimatedDeliveryDate estimatedDeliveryDate) {
        this.estimatedDeliveryDate = estimatedDeliveryDate;
    }

    @Override
    public String getRateId() {
        return estimatedDeliveryDate.getRate() != null ? estimatedDeliveryDate.getRate().getId() : null;
    }

    @Override
    public String getService() {
        return estimatedDeliveryDate.getRate() != null ? estimatedDeliveryDate.getRate().getService() : null;
    }

    @Override
    public String getCarrier() {
        return estimatedDeliveryDate.getRate() != null ? estimatedDeliveryDate.getRate().getCarrier() : null;
    }

    @Override
    public String getCarrierAccountId() {
        return estimatedDeliveryDate.getRate() != null ? estimatedDeliveryDate.getRate().getCarrierAccountId() : null;
    }

    @Override
    public String getEdd() {
        return estimatedDeliveryDate.getEasypostTimeInTransitData() != null ?
            estimatedDeliveryDate.getEasypostTimeInTransitData().getEasypostEstimatedDeliveryDate() : null;
    }

    @Override
    public Double getOnTimePerformance() {
        return null;
    }

    @Override
    public Integer getDeliveryDays() {
        // Get from TimeInTransit percentile50 as a default
        try {
            return estimatedDeliveryDate.getEasypostTimeInTransitData() != null &&
                estimatedDeliveryDate.getEasypostTimeInTransitData().getDaysInTransit() != null ?
                estimatedDeliveryDate.getEasypostTimeInTransitData().getDaysInTransit().getPercentile50() : null;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public Boolean getDeliveryDateGuaranteed() {
        // Get from the Rate object if available
        return estimatedDeliveryDate.getRate() != null ?
            estimatedDeliveryDate.getRate().getDeliveryDateGuaranteed() : null;
    }

    public EstimatedDeliveryDate getEstimatedDeliveryDate() {
        return estimatedDeliveryDate;
    }
}
