package com.uncommongoods.tugboat.engine.ports.shipping.model;

import java.util.Map;

/**
 * Interface for RequestHookResponses operations
 */
public interface IRequestHookResponses {
    /**
     * Get the HTTP method.
     *
     * @return the HTTP method
     */
    String getMethod();

    /**
     * Get the request URI.
     *
     * @return the request URI
     */
    String getUri();

    /**
     * Get the request headers.
     *
     * @return the request headers
     */
    Map<String, String> getHeaders();

    /**
     * Get the request body.
     *
     * @return the request body
     */
    String getBody();
}
