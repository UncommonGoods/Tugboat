package com.uncommongoods.tugboat.engine.ports.shipping.model;

import com.easypost.model.AddressVerificationFieldError;

import java.util.HashMap;
import java.util.Map;

public class AddressVerificationFieldErrorAdapter implements IAddressVerificationFieldError, Mappable {
    private final AddressVerificationFieldError error;

    public AddressVerificationFieldErrorAdapter(AddressVerificationFieldError error) {
        this.error = error;
    }

    @Override
    public String getMessage() {
        return error.getMessage();
    }

    @Override
    public String getCode() {
        return error.getCode();
    }

    @Override
    public String getField() {
        return error.getField();
    }

    @Override
    public String getSuggestion() {
        return error.getSuggestion();
    }

    public AddressVerificationFieldError getError() {
        return error;
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("message", getMessage());
        map.put("code", getCode());
        map.put("field", getField());
        map.put("suggestion", getSuggestion());
        return map;
    }
}
