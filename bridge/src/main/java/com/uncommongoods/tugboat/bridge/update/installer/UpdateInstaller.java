// Copyright (c) 2025 Uncommon Goods LLC
// SPDX-License-Identifier: MPL-2.0

package com.uncommongoods.tugboat.bridge.update.installer;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Hands a downloaded installer off to the OS. Spawns a
 * detached helper that waits for this process to exit, installs the update,
 * then relaunches the app.  The he caller has to exit right after this returns.
 */
public interface UpdateInstaller {

    void installAndRestart(Path installerFile) throws IOException;

    static UpdateInstaller forCurrentPlatform() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            return new WindowsMsiInstaller();
        }
        throw new UnsupportedOperationException("Auto-update install is not supported on " + os);
    }
}
