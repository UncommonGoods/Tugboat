// Copyright (c) 2025 Uncommon Goods LLC
// SPDX-License-Identifier: MPL-2.0

package com.uncommongoods.tugboat.bridge.update;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.Optional;

/** Fetches update metadata and downloads installers. */
public class UpdateClient {

    public interface ProgressListener {
        void onProgress(long bytesRead, long totalBytes);
    }

    private final Gson gson = new Gson();
    private final HttpClient http = HttpClient.newBuilder()
        .followRedirects(HttpClient.Redirect.NORMAL)
        .connectTimeout(Duration.ofSeconds(10))
        .build();

    /**
     * Fetches latest.json for this app's OS channel and returns it only if it
     * describes a version newer than the running one.
     */
    public Optional<UpdateManifest> checkForUpdate(Duration timeout) throws IOException, InterruptedException {
        String currentVersion = UpdateEnvironment.currentVersion();
        if (currentVersion == null) {
            return Optional.empty();
        }

        String url = UpdateEnvironment.latestManifestUrl() + "?t=" + System.currentTimeMillis();
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
            .timeout(timeout)
            .GET()
            .build();
        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("Update check failed: HTTP " + response.statusCode() + " from " + UpdateEnvironment.latestManifestUrl());
        }
        UpdateManifest manifest;
        try {
            manifest = gson.fromJson(response.body(), UpdateManifest.class);
        } catch (JsonSyntaxException e) {
            throw new IOException("Update check failed: malformed latest.json", e);
        }
        if (manifest == null || !manifest.isValid()) {
            throw new IOException("Update check failed: incomplete latest.json");
        }
        if (VersionCompare.isNewer(manifest.version, currentVersion)) {
            return Optional.of(manifest);
        }
        return Optional.empty();
    }

    /**
     * Downloads the installer to ~/.tugboat/updates/ and verifies the SHA-256
     * checksum, throwing if it doesn't match the manifest.
     */
    public Path download(UpdateManifest manifest, ProgressListener listener) throws IOException, InterruptedException {
        Path dir = UpdateEnvironment.updatesDir();
        Files.createDirectories(dir);
        cleanStaleInstallers(dir);

        String fileName = fileNameFromUrl(manifest.installerUrl);
        Path target = dir.resolve(fileName);
        Path partial = dir.resolve(fileName + ".part");

        HttpRequest request = HttpRequest.newBuilder(URI.create(manifest.installerUrl))
            .timeout(Duration.ofMinutes(15))
            .GET()
            .build();
        HttpResponse<InputStream> response = http.send(request, HttpResponse.BodyHandlers.ofInputStream());
        if (response.statusCode() != 200) {
            throw new IOException("Installer download failed: HTTP " + response.statusCode());
        }

        MessageDigest digest = sha256Digest();
        long totalBytes = manifest.size > 0 ? manifest.size
            : response.headers().firstValueAsLong("Content-Length").orElse(-1);
        long bytesRead = 0;
        try (DigestInputStream in = new DigestInputStream(response.body(), digest);
             var out = Files.newOutputStream(partial)) {
            byte[] buffer = new byte[1 << 16];
            int read;
            while ((read = in.read(buffer)) >= 0) {
                out.write(buffer, 0, read);
                bytesRead += read;
                if (listener != null) {
                    listener.onProgress(bytesRead, totalBytes);
                }
            }
        } catch (IOException e) {
            Files.deleteIfExists(partial);
            throw e;
        }

        String actualSha256 = HexFormat.of().formatHex(digest.digest());
        if (!actualSha256.equalsIgnoreCase(manifest.sha256.trim())) {
            Files.deleteIfExists(partial);
            throw new IOException("Installer checksum mismatch: expected " + manifest.sha256 + " but was " + actualSha256);
        }
        Files.move(partial, target, StandardCopyOption.REPLACE_EXISTING);
        return target;
    }

    private static void cleanStaleInstallers(Path dir) {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.{msi,part}")) {
            for (Path stale : stream) {
                Files.deleteIfExists(stale);
            }
        } catch (IOException e) {
            // A leftover installer is harmless; the new download replaces it.
            System.err.println("Warning: could not clean stale installers: " + e.getMessage());
        }
    }

    private static String fileNameFromUrl(String url) {
        String path = URI.create(url).getPath();
        String name = path.substring(path.lastIndexOf('/') + 1);
        if (name.isBlank()) {
            throw new IllegalArgumentException("Installer URL has no file name: " + url);
        }
        return name;
    }

    private static MessageDigest sha256Digest() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }
}
