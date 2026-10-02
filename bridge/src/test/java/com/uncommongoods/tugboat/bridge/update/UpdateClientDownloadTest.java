// Copyright (c) 2025 Uncommon Goods LLC
// SPDX-License-Identifier: MPL-2.0

package com.uncommongoods.tugboat.bridge.update;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UpdateClientDownloadTest {

    private static final byte[] INSTALLER_BYTES = "fake msi payload".getBytes(StandardCharsets.UTF_8);

    private HttpServer server;
    private String installerUrl;

    @BeforeEach
    void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/TugboatBridge-9.9.9.msi", exchange -> {
            exchange.sendResponseHeaders(200, INSTALLER_BYTES.length);
            exchange.getResponseBody().write(INSTALLER_BYTES);
            exchange.close();
        });
        server.start();
        installerUrl = "http://127.0.0.1:" + server.getAddress().getPort() + "/TugboatBridge-9.9.9.msi";
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    @Test
    void downloadsAndVerifiesMatchingChecksum() throws Exception {
        UpdateManifest manifest = manifest(sha256(INSTALLER_BYTES));
        Path downloaded = new UpdateClient().download(manifest, null);
        try {
            assertEquals("TugboatBridge-9.9.9.msi", downloaded.getFileName().toString());
            assertEquals(INSTALLER_BYTES.length, Files.size(downloaded));
        } finally {
            Files.deleteIfExists(downloaded);
        }
    }

    @Test
    void rejectsChecksumMismatchAndLeavesNoFile() {
        UpdateManifest manifest = manifest("0".repeat(64));
        IOException e = assertThrows(IOException.class, () -> new UpdateClient().download(manifest, null));
        assertTrue(e.getMessage().contains("checksum mismatch"));
        assertTrue(Files.notExists(UpdateEnvironment.updatesDir().resolve("TugboatBridge-9.9.9.msi")));
    }

    private UpdateManifest manifest(String sha256) {
        UpdateManifest manifest = new UpdateManifest();
        manifest.version = "9.9.9";
        manifest.installerUrl = installerUrl;
        manifest.sha256 = sha256;
        manifest.size = INSTALLER_BYTES.length;
        return manifest;
    }

    private static String sha256(byte[] data) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(data));
    }
}
