// Copyright (c) 2025 Uncommon Goods LLC
// SPDX-License-Identifier: MPL-2.0

package com.uncommongoods.tugboat.engine.model;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.annotations.Expose;
import com.uncommongoods.tugboat.engine.ports.shipping.model.IAddressDetail;
import com.uncommongoods.tugboat.engine.ports.shipping.model.IAddressVerification;
import com.uncommongoods.tugboat.engine.ports.shipping.model.IAddressVerificationFieldError;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TugboatAddressVerification implements IAddressVerification {
    @Expose
    private Boolean success;
    @Expose
    private List<TugboatAddressVerificationFieldError> errors;
    @Expose
    private TugboatAddressDetail details;

    private static final Gson gson = new Gson();

    public TugboatAddressVerification(Map<String, Object> verificationMap) {
        Object successObj = verificationMap.get("success");
        if (successObj instanceof Boolean) {
            this.success = (Boolean) successObj;
        }

        Object errorsObj = verificationMap.get("errors");
        if (errorsObj instanceof List) {
            this.errors = new ArrayList<>();
            for (Object errorObj : (List<?>) errorsObj) {
                if (errorObj instanceof Map) {
                    this.errors.add(new TugboatAddressVerificationFieldError((Map<String, Object>) errorObj));
                }
            }
        }

        Object detailsObj = verificationMap.get("details");
        if (detailsObj instanceof Map) {
            this.details = new TugboatAddressDetail((Map<String, Object>) detailsObj);
        }
    }

    @Override
    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    @Override
    public List<IAddressVerificationFieldError> getErrors() {
        return new ArrayList<>(errors);
    }

    public void setErrors(List<TugboatAddressVerificationFieldError> errors) {
        this.errors = errors;
    }

    @Override
    public IAddressDetail getDetails() {
        return details;
    }

    public void setDetails(TugboatAddressDetail details) {
        this.details = details;
    }

    @Override
    public JsonElement toJson() {
        return gson.toJsonTree(this);
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("success", success);
        if (errors != null) {
            List<Map<String, Object>> errorMaps = new ArrayList<>();
            for (TugboatAddressVerificationFieldError error : errors) {
                Map<String, Object> errorMap = new HashMap<>();
                errorMap.put("message", error.getMessage());
                errorMap.put("code", error.getCode());
                errorMap.put("field", error.getField());
                errorMap.put("suggestion", error.getSuggestion());
                errorMaps.add(errorMap);
            }
            map.put("errors", errorMaps);
        }
        if (details != null) {
            Map<String, Object> detailsMap = new HashMap<>();
            detailsMap.put("latitude", details.getLatitude());
            detailsMap.put("longitude", details.getLongitude());
            detailsMap.put("time_zone", details.getTimeZone());
            map.put("details", detailsMap);
        }
        return map;
    }

    @Override
    public String toString() {
        return toJsonString();
    }

    @Override
    public String getProviderType() {
        return "tugboat";
    }
}
