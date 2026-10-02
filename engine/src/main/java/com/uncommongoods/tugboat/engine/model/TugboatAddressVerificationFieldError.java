// Copyright (c) 2025 Uncommon Goods LLC
// SPDX-License-Identifier: MPL-2.0

package com.uncommongoods.tugboat.engine.model;

import com.google.gson.annotations.Expose;
import com.uncommongoods.tugboat.engine.ports.shipping.model.IAddressVerificationFieldError;

import java.util.Map;

public class TugboatAddressVerificationFieldError implements IAddressVerificationFieldError {
    @Expose
    private String message;
    @Expose
    private String code;
    @Expose
    private String field;
    @Expose
    private String suggestion;

    public TugboatAddressVerificationFieldError(Map<String, Object> errorMap) {
        Object messageObj = errorMap.get("message");
        if (messageObj != null) {
            this.message = messageObj.toString();
        }

        Object codeObj = errorMap.get("code");
        if (codeObj != null) {
            this.code = codeObj.toString();
        }

        Object fieldObj = errorMap.get("field");
        if (fieldObj != null) {
            this.field = fieldObj.toString();
        }

        Object suggestionObj = errorMap.get("suggestion");
        if (suggestionObj != null) {
            this.suggestion = suggestionObj.toString();
        }
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    @Override
    public String getSuggestion() {
        return suggestion;
    }

    public void setSuggestion(String suggestion) {
        this.suggestion = suggestion;
    }
}
