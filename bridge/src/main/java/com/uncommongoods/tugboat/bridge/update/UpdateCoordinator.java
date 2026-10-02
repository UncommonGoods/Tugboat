// Copyright (c) 2025 Uncommon Goods LLC
// SPDX-License-Identifier: MPL-2.0

package com.uncommongoods.tugboat.bridge.update;

import javafx.application.Platform;
import com.uncommongoods.tugboat.bridge.update.installer.UpdateInstaller;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/** Schedules update checks and drives the install-and-restart handoff. */
public class UpdateCoordinator {

    private static final Duration STARTUP_CHECK_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration PERIODIC_CHECK_TIMEOUT = Duration.ofSeconds(15);

    private final UpdateClient client = new UpdateClient();
    private final AtomicBoolean updateInFlight = new AtomicBoolean(false);
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
        Thread thread = new Thread(runnable, "update-checker");
        thread.setDaemon(true);
        return thread;
    });

    public UpdateClient client() {
        return client;
    }

    /**
     * Synchronous check used during application init, while the splash screen
     * is visible. Bounded by a short timeout; any failure means "no update"
     * so a network problem can never block startup.
     */
    public Optional<UpdateManifest> blockingStartupCheck() {
        if (!UpdateEnvironment.updatesEnabled()) {
            return Optional.empty();
        }
        try {
            return client.checkForUpdate(STARTUP_CHECK_TIMEOUT);
        } catch (Exception e) {
            System.err.println("Startup update check failed (continuing): " + e.getMessage());
            return Optional.empty();
        }
    }

    public void startPeriodicChecks(Consumer<UpdateManifest> onUpdateAvailable) {
        if (!UpdateEnvironment.updatesEnabled()) {
            System.out.println("Auto-update disabled (not a packaged install or version unknown)");
            return;
        }
        long intervalMillis = (long) (UpdateEnvironment.checkIntervalHours() * 3_600_000);
        scheduler.scheduleWithFixedDelay(() -> {
            if (updateInFlight.get()) {
                return;
            }
            try {
                client.checkForUpdate(PERIODIC_CHECK_TIMEOUT).ifPresent(manifest -> {
                    if (updateInFlight.compareAndSet(false, true)) {
                        onUpdateAvailable.accept(manifest);
                    }
                });
            } catch (Exception e) {
                System.err.println("Periodic update check failed: " + e.getMessage());
            }
        }, intervalMillis, intervalMillis, TimeUnit.MILLISECONDS);
        System.out.println("Auto-update enabled: checking " + UpdateEnvironment.latestManifestUrl()
            + " every " + UpdateEnvironment.checkIntervalHours() + "h (current version "
            + UpdateEnvironment.currentVersion() + ")");
    }

    public void shutdown() {
        scheduler.shutdownNow();
    }

    /**
     * Spawns the headless installer helper and exits the application. Returns
     * false in dev simulation, where the install is logged instead of run and
     * the app keeps running.
     */
    public static boolean launchInstallerAndExit(Path installerFile) throws IOException {
        if (UpdateEnvironment.isDevSimulate()) {
            System.out.println("DEV SIMULATE: would install " + installerFile + " and restart");
            return false;
        }
        UpdateInstaller.forCurrentPlatform().installAndRestart(installerFile);
        Platform.exit();
        return true;
    }
}
