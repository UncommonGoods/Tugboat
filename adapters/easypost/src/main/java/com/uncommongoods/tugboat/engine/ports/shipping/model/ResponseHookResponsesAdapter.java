package com.uncommongoods.tugboat.engine.ports.shipping.model;

import com.easypost.hooks.ResponseHookResponses;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

public class ResponseHookResponsesAdapter implements IResponseHookResponses {
    private final ResponseHookResponses responseHookResponses;

    public ResponseHookResponsesAdapter(ResponseHookResponses responseHookResponses) {
        this.responseHookResponses = responseHookResponses;
    }

    @Override
    public int getHttpStatus() {
        return responseHookResponses.getHttpStatus();
    }

    @Override
    public Map<String, String> getHeaders() {
        return responseHookResponses.getHeaders();
    }

    @Override
    public String getPath() {
        return responseHookResponses.getPath();
    }


    @Override
    public String getResponseBody() {
        return responseHookResponses.getResponseBody();
    }

    @Override
    public String getResponseTimestamp() {
        return responseHookResponses.getRequestTimestamp();
    }

    @Override
    public String getRequestTimestamp() {
        return responseHookResponses.getRequestTimestamp();
    }

    @Override
    public String getRequestUuid() {
        return responseHookResponses.getRequestUuid();
    }

    /**
     * Get the underlying ResponseHookResponses object.
     *
     * @return the underlying ResponseHookResponses object
     */
    public ResponseHookResponses getResponseHookResponses() {
        return responseHookResponses;
    }
}
