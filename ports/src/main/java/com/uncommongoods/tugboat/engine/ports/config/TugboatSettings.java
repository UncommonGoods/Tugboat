package com.uncommongoods.tugboat.engine.ports.config;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Process-wide, environment-variable-like settings for hooks, adapters, and
 * hosts.
 *
 * <p>Resolution order for {@link #get(String)}:
 * <ol>
 *   <li>values published by the host via {@link #publish(Map)} (e.g. the
 *       Bridge settings page)</li>
 *   <li>{@code System.getProperty(name)}</li>
 *   <li>{@code System.getenv(name)}</li>
 *   <li>{@code System.getenv(normalize(name))}, where normalization uppercases
 *       the name and replaces every non-alphanumeric character with {@code _}
 *       (so a settings row named "Cache URL" and an environment variable
 *       {@code CACHE_URL} resolve to the same value)</li>
 * </ol>
 *
 * <p>Hosts that own a settings store (like the Bridge) publish the active
 * values; hosts that don't (like XO) publish nothing and lookups fall through
 * to real environment variables. Hook and adapter code reads the same way in
 * both cases.
 */
public final class TugboatSettings {

    private static final Map<String, String> published = new ConcurrentHashMap<>();

    private TugboatSettings() {
    }

    /**
     * Replace all published settings with the given values. Null values are
     * skipped.
     */
    public static void publish(Map<String, String> values) {
        published.clear();
        if (values != null) {
            values.forEach((k, v) -> {
                if (k != null && v != null) {
                    published.put(k, v);
                }
            });
        }
    }

    /**
     * Resolve a setting by name, or {@code null} if it is not set anywhere.
     */
    public static String get(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        String value = published.get(name);
        if (value == null) {
            value = System.getProperty(name);
        }
        if (value == null) {
            value = System.getenv(name);
        }
        if (value == null) {
            value = System.getenv(normalize(name));
        }
        return value;
    }

    /**
     * Resolve a setting by name, falling back to {@code defaultValue} when it
     * is not set anywhere.
     */
    public static String get(String name, String defaultValue) {
        String value = get(name);
        return value != null ? value : defaultValue;
    }

    /**
     * Uppercase and replace non-alphanumeric characters with underscores:
     * {@code "Cache URL" -> "CACHE_URL"}.
     */
    public static String normalize(String name) {
        StringBuilder sb = new StringBuilder(name.length());
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            sb.append(Character.isLetterOrDigit(c) ? Character.toUpperCase(c) : '_');
        }
        return sb.toString();
    }
}
