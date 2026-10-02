// Copyright (c) 2025 Uncommon Goods LLC
// SPDX-License-Identifier: MPL-2.0

package com.uncommongoods.tugboat.xo;

import com.uncommongoods.tugboat.engine.ports.auth.TokenValidator;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ServiceLoader;

/**
 * Gate in front of every routed endpoint that does real work.
 *
 * <p>The {@link TokenValidator} is discovered with {@link ServiceLoader}, the
 * same way the shipping, cache and box-catalog providers are, so which auth
 * service stands behind this host — or whether one does at all — is a
 * deployment decision rather than a compile-time one.
 *
 * <p><strong>With no validator registered, every request is served.</strong>
 * That is deliberate: an OSS build has no UG authenticator to call, and
 * refusing to start would make the host unusable outside UG. It is also
 * exactly the state a misconfigured deployment lands in, which is why
 * {@link #loadValidator()} says so at WARN rather than burying it at INFO next
 * to the ordinary provider-discovery lines.
 */
@Singleton
public class RequestAuthenticator {

    private static final Logger logger = LoggerFactory.getLogger(RequestAuthenticator.class);

    /** Optional; null when no {@link TokenValidator} is on the classpath. */
    private TokenValidator validator;

    @PostConstruct
    void initialize() {
        this.validator = loadValidator();
    }

    @PreDestroy
    void shutdown() {
        // A validator may hold an HTTP client; one that holds nothing needs no teardown.
        if (validator instanceof AutoCloseable closeable) {
            try {
                closeable.close();
            } catch (Exception e) {
                logger.warn("Failed to close token validator: {}", e.getMessage());
            }
        }
    }

    private TokenValidator loadValidator() {
        for (TokenValidator candidate : ServiceLoader.load(TokenValidator.class)) {
            logger.info("Using token validator {}", candidate.getClass().getName());
            return candidate;
        }
        logger.warn("******************************************************************");
        logger.warn("* NO TOKEN VALIDATOR REGISTERED                                  *");
        logger.warn("* Every request to this service will be served UNAUTHENTICATED.  *");
        logger.warn("* This is expected for a build with no auth service in front of  *");
        logger.warn("* it. If that is not this deployment, put a TokenValidator       *");
        logger.warn("* adapter on the classpath (see adapters/README.md) and restart. *");
        logger.warn("******************************************************************");
        return null;
    }

    /**
     * Authorize one request.
     *
     * @param authorization the request's {@code Authorization} header, if any
     * @param operation     what the caller is asking to do, for the log line
     * @param subject       what it is asking to do it to, for the log line;
     *                      null for operations that name no single subject
     * @return {@code null} when the request may proceed, or the response to
     *         return to the caller instead of doing the work
     * @throws RuntimeException when validity could not be determined, which
     *                          Micronaut renders as a 500 — an auth outage
     *                          refuses requests, it does not wave them through
     */
    @Nullable
    public HttpResponse<String> authorize(@Nullable String authorization,
                                          String operation,
                                          @Nullable String subject) {
        if (validator == null) {
            return null;
        }

        boolean isValid;
        try {
            isValid = validator.isValid(authorization);
        } catch (Exception e) {
            String errorMsg = String.format(
                "Authentication token could not be validated -- %s", e.getMessage());
            logger.error(errorMsg, e);
            throw new RuntimeException(errorMsg, e);
        }

        if (!isValid) {
            if (subject != null) {
                logger.error("could not authenticate {} request for {}", operation, subject);
            } else {
                logger.error("could not authenticate {} request", operation);
            }
            return HttpResponse.status(HttpStatus.UNAUTHORIZED);
        }
        return null;
    }
}
