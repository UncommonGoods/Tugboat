// Copyright (c) 2025 Uncommon Goods LLC
// SPDX-License-Identifier: MPL-2.0

package com.uncommongoods.tugboat.bridge.update;

import java.nio.file.Path;

/**
 * Static context for the auto-update system. Reads configuration baked into
 * the installed app by jpackage (--java-options) and the jpackage runtime
 * properties (jpackage.app-version, jpackage.app-path), with overrides for
 * development.
 * Dev simulation: run with -Dtugboat.update.devSimulate=true and
 * -Dtugboat.update.currentVersion=0.0.1 to exercise check/download/verify
 * from the IDE; the installer launch is logged instead of executed.
 */
public final class UpdateEnvironment {

    private UpdateEnvironment() {}

    /** True when running from a jpackage-installed app (not IDE/gradle). */
    public static boolean isPackaged() {
        return System.getProperty("jpackage.app-path") != null;
    }

    public static String launcherPath() {
        return System.getProperty("jpackage.app-path");
    }

    public static String currentVersion() {
        String override = System.getProperty("tugboat.update.currentVersion");
        if (override != null) {
            return override;
        }
        String jpackageVersion = System.getProperty("jpackage.app-version");
        if (jpackageVersion != null) {
            return jpackageVersion;
        }
        Package pkg = UpdateEnvironment.class.getPackage();
        return pkg != null ? pkg.getImplementationVersion() : null;
    }

    public static boolean isDevSimulate() {
        return Boolean.getBoolean("tugboat.update.devSimulate");
    }

    public static boolean updatesEnabled() {
        if (Boolean.getBoolean("tugboat.update.disabled")) {
            return false;
        }
        return (isPackaged() || isDevSimulate()) && currentVersion() != null;
    }

    public static String channel() {
        return System.getProperty("tugboat.update.channel", "bridge");
    }

    public static String baseUrl() {
        return System.getProperty("tugboat.update.baseUrl",
            "https://tugboat-updates.s3.us-east-1.amazonaws.com/updates");
    }

    public static String latestManifestUrl() {
        return baseUrl() + "/windows/" + channel() + "/latest.json";
    }

    public static double checkIntervalHours() {
        try {
            return Double.parseDouble(System.getProperty("tugboat.update.intervalHours", "0.08"));
        } catch (NumberFormatException e) {
            return 0.08;
        }
    }

    public static String appDisplayName() {
        return System.getProperty("tugboat.app.name", "Tugboat Bridge");
    }

    public static Path updatesDir() {
        return Path.of(System.getProperty("user.home"), ".tugboat", "updates");
    }
}
