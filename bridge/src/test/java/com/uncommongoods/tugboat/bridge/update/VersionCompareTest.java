// Copyright (c) 2025 Uncommon Goods LLC
// SPDX-License-Identifier: MPL-2.0

package com.uncommongoods.tugboat.bridge.update;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VersionCompareTest {

    @Test
    void comparesNumericallyNotLexically() {
        assertTrue(VersionCompare.isNewer("2.1.10", "2.1.9"));
        assertFalse(VersionCompare.isNewer("2.1.9", "2.1.10"));
    }

    @Test
    void equalVersionsAreNotNewer() {
        assertFalse(VersionCompare.isNewer("2.1.6", "2.1.6"));
        assertEquals(0, VersionCompare.compare("2.1.6", "2.1.6"));
    }

    @Test
    void missingSegmentsCountAsZero() {
        assertTrue(VersionCompare.isNewer("2.1.1", "2.1"));
        assertFalse(VersionCompare.isNewer("2.1", "2.1.0"));
        assertTrue(VersionCompare.isNewer("3", "2.9.9"));
    }

    @Test
    void majorAndMinorDominate() {
        assertTrue(VersionCompare.isNewer("3.0.0", "2.99.99"));
        assertTrue(VersionCompare.isNewer("2.2.0", "2.1.99"));
    }

    @Test
    void toleratesNonNumericSuffixes() {
        assertTrue(VersionCompare.isNewer("2.1.7-beta", "2.1.6"));
        assertFalse(VersionCompare.isNewer("2.1.6-hotfix", "2.1.6"));
    }
}
