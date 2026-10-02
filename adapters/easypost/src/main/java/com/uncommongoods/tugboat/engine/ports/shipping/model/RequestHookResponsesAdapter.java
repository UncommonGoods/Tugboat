package com.uncommongoods.tugboat.engine.ports.shipping.model;

import com.easypost.hooks.RequestHookResponses;

import java.util.Map;

public class RequestHookResponsesAdapter implements IRequestHookResponses {
    private final RequestHookResponses requestHookResponses;

    public RequestHookResponsesAdapter(RequestHookResponses requestHookResponses) {
        this.requestHookResponses = requestHookResponses;
    }

    @Override
    public String getMethod() {
        return requestHookResponses.getMethod();
    }

    @Override
    public String getUri() {
        return requestHookResponses.getPath();
    }

    @Override
    public Map<String, String> getHeaders() {
        return requestHookResponses.getHeaders();
    }

    @Override
    public String getBody() {
        return requestHookResponses.getRequestBody() != null ?
            requestHookResponses.getRequestBody().toString() : null;
    }

    public RequestHookResponses getRequestHookResponses() {
        return requestHookResponses;
    }
}
