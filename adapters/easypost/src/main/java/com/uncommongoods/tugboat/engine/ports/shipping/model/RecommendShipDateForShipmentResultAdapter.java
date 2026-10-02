package com.uncommongoods.tugboat.engine.ports.shipping.model;

import com.easypost.model.RecommendShipDateForShipmentResult;

public class RecommendShipDateForShipmentResultAdapter implements IRecommendShipDateForShipmentResult {
    private final RecommendShipDateForShipmentResult result;

    public RecommendShipDateForShipmentResultAdapter(RecommendShipDateForShipmentResult result) {
        this.result = result;
    }

    @Override
    public String getRateId() {
        return result.getRate() != null ? result.getRate().getId() : null;
    }

    @Override
    public String getService() {
        return result.getRate() != null ? result.getRate().getService() : null;
    }

    @Override
    public String getCarrier() {
        return result.getRate() != null ? result.getRate().getCarrier() : null;
    }

    @Override
    public String getCarrierAccountId() {
        return result.getRate() != null ? result.getRate().getCarrierAccountId() : null;
    }

    @Override
    public String getShipDate() {
        return result.getEasypostTimeInTransitData() != null ?
            result.getEasypostTimeInTransitData().getShipOnDate() : null;
    }

    @Override
    public String getDeliveryDate() {
        return result.getRate() != null ? result.getRate().getDeliveryDate() : null;
    }

    @Override
    public Integer getDeliveryDays() {
        if (result.getRate() != null && result.getRate().getDeliveryDays() != null) {
            Number days = result.getRate().getDeliveryDays();
            return days.intValue();
        }
        return null;
    }

    @Override
    public Boolean getDeliveryDateGuaranteed() {
        return result.getRate() != null ? result.getRate().getDeliveryDateGuaranteed() : null;
    }

    public RecommendShipDateForShipmentResult getResult() {
        return result;
    }
}
