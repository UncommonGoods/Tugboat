// Copyright (c) 2025 Uncommon Goods LLC
// SPDX-License-Identifier: MPL-2.0

package com.uncommongoods.tugboat.engine.model;

import com.google.gson.annotations.Expose;
import com.uncommongoods.tugboat.engine.ports.shipping.model.IAddressDetail;

import java.util.Map;

public class TugboatAddressDetail implements IAddressDetail {
    @Expose
    private Float latitude;
    @Expose
    private Float longitude;
    @Expose
    private String timeZone;

    public TugboatAddressDetail(Map<String, Object> detailMap) {
        Object latitudeObj = detailMap.get("latitude");
        if (latitudeObj instanceof Number) {
            this.latitude = ((Number) latitudeObj).floatValue();
        }

        Object longitudeObj = detailMap.get("longitude");
        if (longitudeObj instanceof Number) {
            this.longitude = ((Number) longitudeObj).floatValue();
        }

        Object timeZoneObj = detailMap.get("time_zone");
        if (timeZoneObj != null) {
            this.timeZone = timeZoneObj.toString();
        }
    }

    @Override
    public Float getLatitude() {
        return latitude;
    }

    public void setLatitude(Float latitude) {
        this.latitude = latitude;
    }

    @Override
    public Float getLongitude() {
        return longitude;
    }

    public void setLongitude(Float longitude) {
        this.longitude = longitude;
    }

    @Override
    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }
}
