// Copyright (c) 2025 Uncommon Goods LLC
// SPDX-License-Identifier: MPL-2.0

package com.uncommongoods.tugboat.bridge.update.installer;

import com.uncommongoods.tugboat.bridge.update.UpdateEnvironment;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Installs a per-user jpackage MSI by spawning a headless PowerShell helper
 * that waits for this process to exit, runs msiexec silently, and relaunches
 * the installed exe. Per-user MSIs need no elevation, and on Windows child
 * processes survive their parent, so the helper outlives the app.
 */
public class WindowsMsiInstaller implements UpdateInstaller {

    private static final String SCRIPT_RESOURCE = "/update/tugboat-updater.ps1";

    @Override
    public void installAndRestart(Path installerFile) throws IOException {
        String launcher = UpdateEnvironment.launcherPath();
        if (launcher == null) {
            throw new IOException("Cannot install update: launcher path unknown (not a packaged install)");
        }

        Path scriptDir = Files.createTempDirectory("tugboat-update");
        Path script = scriptDir.resolve("tugboat-updater.ps1");
        try (InputStream in = WindowsMsiInstaller.class.getResourceAsStream(SCRIPT_RESOURCE)) {
            if (in == null) {
                throw new IOException("Updater script resource missing: " + SCRIPT_RESOURCE);
            }
            Files.copy(in, script, StandardCopyOption.REPLACE_EXISTING);
        }

        Path logDir = UpdateEnvironment.updatesDir();
        Files.createDirectories(logDir);

        ProcessBuilder pb = new ProcessBuilder(
            "powershell.exe", "-NoProfile", "-ExecutionPolicy", "Bypass", "-WindowStyle", "Hidden",
            "-File", script.toAbsolutePath().toString(),
            "-AppProcessId", Long.toString(ProcessHandle.current().pid()),
            "-MsiPath", installerFile.toAbsolutePath().toString(),
            "-LaunchExe", launcher,
            "-LogDir", logDir.toAbsolutePath().toString());
        // Redirect to files, never inherit: a pipe to a dead parent can block
        // the helper.
        pb.redirectOutput(logDir.resolve("updater-stdout.log").toFile());
        pb.redirectError(logDir.resolve("updater-stderr.log").toFile());
        pb.start();
    }
}
