package com.uncommongoods.tugboat.engine.ports.shipping.model;

import java.util.Map;

/**
 * Interface for ResponseHookResponses operations
 */
public interface IResponseHookResponses {

    int getHttpStatus();

    Map<String, String> getHeaders();

    String getPath();

    String getResponseBody();

    String getResponseTimestamp();

    String getRequestTimestamp();

    String getRequestUuid();
}
