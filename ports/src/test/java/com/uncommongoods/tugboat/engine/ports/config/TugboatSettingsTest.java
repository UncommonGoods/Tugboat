package com.uncommongoods.tugboat.engine.ports.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class TugboatSettingsTest {

    private static final String NAME = "TUGBOAT_SETTINGS_TEST_KEY";

    @AfterEach
    void cleanup() {
        TugboatSettings.publish(Map.of());
        System.clearProperty(NAME);
    }

    @Test
    void publishedValueWins() {
        System.setProperty(NAME, "from-sysprop");
        TugboatSettings.publish(Map.of(NAME, "from-published"));
        assertEquals("from-published", TugboatSettings.get(NAME));
    }

    @Test
    void fallsBackToSystemProperty() {
        System.setProperty(NAME, "from-sysprop");
        assertEquals("from-sysprop", TugboatSettings.get(NAME));
    }

    @Test
    void publishReplacesAllPreviousValues() {
        TugboatSettings.publish(Map.of("A", "1", "B", "2"));
        TugboatSettings.publish(Map.of("A", "3"));
        assertEquals("3", TugboatSettings.get("A"));
        assertNull(TugboatSettings.get("B"));
    }

    @Test
    void unsetNameReturnsDefault() {
        assertNull(TugboatSettings.get(NAME));
        assertEquals("fallback", TugboatSettings.get(NAME, "fallback"));
    }

    @Test
    void normalizesNamesForEnvLookup() {
        assertEquals("CACHE_URL", TugboatSettings.normalize("Cache URL"));
        assertEquals("MY_DB_URL", TugboatSettings.normalize("my.db-url"));

        // A name that normalizes to a real environment variable resolves via
        // the normalized-env fallback ("home" -> "HOME").
        assumeTrue(System.getenv("HOME") != null && System.getenv("home") == null);
        assertEquals(System.getenv("HOME"), TugboatSettings.get("home"));
    }
}
