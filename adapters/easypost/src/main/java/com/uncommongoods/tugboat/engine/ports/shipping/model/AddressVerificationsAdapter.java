package com.uncommongoods.tugboat.engine.ports.shipping.model;

import com.easypost.model.AddressVerification;
import com.easypost.model.AddressVerifications;
import com.easypost.model.EasyPostResource;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

import java.util.HashMap;
import java.util.Map;

public class AddressVerificationsAdapter extends EasyPostResource implements IAddressVerifications {
    private final AddressVerifications addressVerifications;

    public AddressVerificationsAdapter(AddressVerifications addressVerifications) {
        super();
        this.addressVerifications = addressVerifications;
    }

    @Override
    public IAddressVerification getZip4() {
        AddressVerification zip4 = addressVerifications.getZip4();
        return zip4 != null ? new AddressVerificationAdapter(zip4) : null;
    }

    @Override
    public IAddressVerification getDelivery() {
        AddressVerification delivery = addressVerifications.getDelivery();
        return delivery != null ? new AddressVerificationAdapter(delivery) : null;
    }

    public AddressVerifications getAddressVerifications() {
        return addressVerifications;
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
        map.put("id", getId());
        map.put("mode", getMode());
        map.put("object", getObject());
        map.put("created_at", getCreatedAt());
        map.put("updated_at", getUpdatedAt());

        if (getZip4() != null && getZip4() instanceof Mappable) {
            map.put("zip4", ((Mappable) getZip4()).toMap());
        }

        if (getDelivery() != null && getDelivery() instanceof Mappable) {
            map.put("delivery", ((Mappable) getDelivery()).toMap());
        }

        return map;
    }

    @Override
    public String getProviderType() {
        return "easypost";
    }
}
