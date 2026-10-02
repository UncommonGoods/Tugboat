package com.uncommongoods.tugboat.engine.ports.shipping.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.easypost.model.AddressDetail;
import com.easypost.model.AddressVerification;
import com.easypost.model.AddressVerificationFieldError;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

public class AddressVerificationAdapter implements IAddressVerification {
    private final AddressVerification addressVerification;

    public AddressVerificationAdapter(AddressVerification addressVerification) {
        this.addressVerification = addressVerification;
    }

    @Override
    public Boolean getSuccess() {
        return addressVerification.getSuccess();
    }

    @Override
    public List<IAddressVerificationFieldError> getErrors() {
        List<AddressVerificationFieldError> errors = addressVerification.getErrors();
        if (errors == null) {
            return null;
        }

        return errors.stream()
            .map(error -> new AddressVerificationFieldErrorAdapter(error))
            .collect(Collectors.toList());
    }

    @Override
    public IAddressDetail getDetails() {
        AddressDetail details = addressVerification.getDetails();
        return details != null ? new AddressDetailAdapter(details) : null;
    }

    public AddressVerification getAddressVerification() {
        return addressVerification;
    }

    @Override
    public JsonElement toJson() {
        return new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
            .create()
            .toJsonTree(this);
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("success", getSuccess());

        List<IAddressVerificationFieldError> errors = getErrors();
        if (errors != null) {
            map.put("errors", errors.stream()
                .map(error -> error instanceof Mappable ? ((Mappable) error).toMap() : error)
                .collect(Collectors.toList()));
        }

        if (getDetails() != null && getDetails() instanceof Mappable) {
            map.put("details", ((Mappable) getDetails()).toMap());
        }

        return map;
    }

    @Override
    public String getProviderType() {
        return "easypost";
    }
}
