package com.uncommongoods.tugboat.engine.ports.shipping.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;

/**
 * Coercion helpers for the decoded-JSON maps handed to
 * {@link ModelTypeProvider#fromMap}.
 *
 * <p>JSON decoded without a target type carries no Java types: every number
 * arrives as a {@code Double} regardless of the field it is destined for, and
 * every date as a {@code String}. Model classes that cast straight to the field
 * type therefore fail in two ways -- {@code (Float) map.get("weight")} throws, and
 * a {@code value instanceof Date} guard silently never fires, dropping the field.
 * These helpers absorb both.
 */
public final class MapValues {

    /** The date format Tugboat writes; matches the engine's Date type adapter. */
    private static final String ISO_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    private MapValues() {}

    public static String asString(Map<String, Object> map, String key) {
        Object value = map == null ? null : map.get(key);
        return value == null ? null : value.toString();
    }

    /**
     * Read the first of several keys that is present, for fields whose serialized
     * name has changed. Lets a model accept both spellings so blobs written by an
     * older build still load.
     */
    public static String asString(Map<String, Object> map, String key, String... fallbackKeys) {
        String value = asString(map, key);
        if (value != null) {
            return value;
        }
        for (String fallback : fallbackKeys) {
            value = asString(map, fallback);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    public static Float asFloat(Map<String, Object> map, String key) {
        Number value = asNumber(map, key);
        return value == null ? null : value.floatValue();
    }

    public static Double asDouble(Map<String, Object> map, String key) {
        Number value = asNumber(map, key);
        return value == null ? null : value.doubleValue();
    }

    public static Integer asInt(Map<String, Object> map, String key) {
        Number value = asNumber(map, key);
        return value == null ? null : value.intValue();
    }

    public static Long asLong(Map<String, Object> map, String key) {
        Number value = asNumber(map, key);
        return value == null ? null : value.longValue();
    }

    public static Boolean asBool(Map<String, Object> map, String key) {
        Object value = map == null ? null : map.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return Boolean.valueOf(value.toString());
    }

    /**
     * Parse a date written in Tugboat's ISO format. Already-parsed {@link Date}
     * values pass through, so this is safe on maps that did not come from JSON.
     *
     * @return {@code null} when absent or unparseable -- a malformed timestamp is
     *     not worth failing an entire shipment over
     */
    public static Date asDate(Map<String, Object> map, String key) {
        Object value = map == null ? null : map.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Date) {
            return (Date) value;
        }
        SimpleDateFormat format = new SimpleDateFormat(ISO_FORMAT);
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            return format.parse(value.toString());
        } catch (ParseException e) {
            return null;
        }
    }

    /** As {@link #asDate(Map, String)}, accepting alternative spellings of the key. */
    public static Date asDate(Map<String, Object> map, String key, String... fallbackKeys) {
        Date value = asDate(map, key);
        if (value != null) {
            return value;
        }
        for (String fallback : fallbackKeys) {
            value = asDate(map, fallback);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> asMap(Map<String, Object> map, String key) {
        Object value = map == null ? null : map.get(key);
        return value instanceof Map ? (Map<String, Object>) value : null;
    }

    private static Number asNumber(Map<String, Object> map, String key) {
        Object value = map == null ? null : map.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return (Number) value;
        }
        try {
            return Double.valueOf(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
