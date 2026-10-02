package com.uncommongoods.tugboat.engine.ports.shipping.model;

import com.easypost.model.SmartRateAccuracy;

public class SmartRateAccuracyAdapter implements ISmartRateAccuracy {
    private final SmartRateAccuracy smartRateAccuracy;

    public SmartRateAccuracyAdapter(SmartRateAccuracy smartRateAccuracy) {
        this.smartRateAccuracy = smartRateAccuracy;
    }

    @Override
    public Integer getPercentile50() {
        return null;
    }

    @Override
    public Integer getPercentile75() {
        return null;
    }

    @Override
    public Integer getPercentile85() {
        return null;
    }

    @Override
    public Integer getPercentile90() {
        return null;
    }

    @Override
    public Integer getPercentile95() {
        return null;
    }

    @Override
    public Integer getPercentile97() {
        return null;
    }

    @Override
    public Integer getPercentile99() {
        return null;
    }

    public SmartRateAccuracy getSmartRateAccuracy() {
        return smartRateAccuracy;
    }
}
