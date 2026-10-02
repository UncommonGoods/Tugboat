// Copyright (c) 2025 Uncommon Goods LLC
// SPDX-License-Identifier: MPL-2.0

package com.uncommongoods.tugboat.bridge.update;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UpdateManifestTest {

    @Test
    void parsesPublishedLatestJson() {
        String json = """
            {
              "version": "2.1.7",
              "releaseDate": "2026-06-11T18:00:00Z",
              "installerUrl": "https://tugboat-updates.s3.us-east-1.amazonaws.com/updates/windows/bridge/TugboatBridge-2.1.7.msi",
              "sha256": "a3f5c2d1e4b6978012345678901234567890123456789012345678901234abcd",
              "size": 98765432,
              "releaseNotes": "Fixed label printing"
            }
            """;
        UpdateManifest manifest = new Gson().fromJson(json, UpdateManifest.class);
        assertTrue(manifest.isValid());
        assertEquals("2.1.7", manifest.version);
        assertEquals(98765432L, manifest.size);
        assertTrue(manifest.installerUrl.endsWith("TugboatBridge-2.1.7.msi"));
        assertEquals("Fixed label printing", manifest.releaseNotes);
    }

    @Test
    void incompleteManifestIsInvalid() {
        UpdateManifest manifest = new Gson().fromJson("{\"version\": \"2.1.7\"}", UpdateManifest.class);
        assertFalse(manifest.isValid());
    }
}
