package com.uncommongoods.tugboat.engine.ports.shipping.service;

import com.uncommongoods.tugboat.engine.exception.TugboatException;
import com.easypost.model.Rate;
import com.easypost.service.EasyPostClient;
import com.easypost.service.RateService;
import com.uncommongoods.tugboat.engine.ports.shipping.model.RateAdapter;
import com.uncommongoods.tugboat.engine.ports.shipping.model.IRate;

public class RateServiceAdapter implements IRateService {
    private final RateService rateService;

    public RateServiceAdapter(EasyPostClient client) {
        this.rateService = client.rate;
    }

    public RateServiceAdapter(RateService rateService) {
        this.rateService = rateService;
    }

    @Override
    public IRate retrieve(String id) throws TugboatException {
        try {
            Rate rate = rateService.retrieve(id);
            return rate != null ? new RateAdapter(rate) : null;
        } catch (com.easypost.exception.EasyPostException e) {
            throw new TugboatException(e.getMessage(), e);
        }
    }

    public RateService getRateService() {
        return rateService;
    }
}
