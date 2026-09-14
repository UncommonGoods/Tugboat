package com.uncommongoods.tugboat.engine.ports.auth;

/**
 * Decides whether an inbound request carries a valid credential, so a host can
 * turn away a caller it does not recognize before doing any work on its behalf.
 *
 * <p>Authentication is deployment-specific — the credential's format, and
 * whatever vouches for it, belong to whoever runs the host — so implementations
 * are discovered with {@link java.util.ServiceLoader} and registered under
 * {@code META-INF/services/com.uncommongoods.tugboat.engine.ports.auth.TokenValidator}.
 * A host with no implementation on its classpath serves every request, which is
 * what lets a build with no auth service in front of it run at all; hosts are
 * expected to announce that state loudly at startup rather than quietly.
 *
 * <p><strong>Validation fails closed.</strong> There are two ways to reject,
 * and the difference is worth preserving because it tells an operator which
 * side is broken:
 * <ul>
 *   <li>return {@code false} when the credential is definitively not valid —
 *       the host answers {@code 401}, and the caller's credential is at
 *       fault;</li>
 *   <li>throw when validity cannot be determined at all, because whatever
 *       vouches for the credential is unreachable or answered nonsense — the
 *       host answers {@code 500}, and the deployment is at fault.</li>
 * </ul>
 * Never return {@code true} to paper over an outage: an implementation that
 * cannot tell has to say so.
 *
 * <p>An implementation that holds resources (an HTTP client, say) should also
 * implement {@link AutoCloseable}; hosts release validators with an
 * {@code instanceof AutoCloseable} check, so one holding nothing needs no
 * teardown. Implementations must have a public no-arg constructor and should
 * defer acquiring resources until the first {@link #isValid(String)} call,
 * since {@code ServiceLoader} instantiates every provider it finds — including
 * in hosts that never validate anything.
 */
public interface TokenValidator {

    /**
     * Check a credential taken from the request.
     *
     * @param credential the raw value of the request's {@code Authorization}
     *                   header, or {@code null} when the request carried none.
     *                   A null or blank value is not valid, and an
     *                   implementation should say so without calling out to
     *                   anything.
     * @return whether the credential is valid
     * @throws RuntimeException when validity cannot be determined; see the
     *                          class javadoc — this is not the same as invalid
     */
    boolean isValid(String credential);
}
