package com.uncommongoods.tugboat.engine.ports.shipping.service;

import com.easypost.exception.General.MissingParameterError;

import java.util.List;
import java.util.Map;

public class EasyPostClientFactory implements ShippingClientFactory {

    public static final String API_KEY = "API Key";

    @Override
    public String type() {
        return "easypost";
    }

    @Override
    public List<String> configKeys() {
        return List.of(API_KEY);
    }

    @Override
    public IShippingClient create(Map<String, String> config) {
        String apiKey = config.get(API_KEY);
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalArgumentException("easypost client requires '" + API_KEY + "'");
        }
        try {
            return new ShippingClientAdapter(apiKey);
        } catch (MissingParameterError e) {
            throw new IllegalArgumentException("easypost client configuration invalid: " + e.getMessage(), e);
        }
    }
}
