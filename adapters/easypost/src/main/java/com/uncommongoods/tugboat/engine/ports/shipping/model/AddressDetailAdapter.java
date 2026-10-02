package com.uncommongoods.tugboat.engine.ports.shipping.model;

import com.easypost.model.AddressDetail;

import java.util.HashMap;
import java.util.Map;

public class AddressDetailAdapter implements IAddressDetail, Mappable {
    private final AddressDetail addressDetail;

    public AddressDetailAdapter(AddressDetail addressDetail) {
        this.addressDetail = addressDetail;
    }

    @Override
    public Float getLatitude() {
        return addressDetail.getLatitude();
    }

    @Override
    public Float getLongitude() {
        return addressDetail.getLongitude();
    }

    @Override
    public String getTimeZone() {
        return addressDetail.getTimeZone();
    }

    public AddressDetail getAddressDetail() {
        return addressDetail;
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("latitude", getLatitude());
        map.put("longitude", getLongitude());
        map.put("time_zone", getTimeZone());
        return map;
    }
}
