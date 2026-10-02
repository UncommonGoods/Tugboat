// Copyright (c) 2025 Uncommon Goods LLC
// SPDX-License-Identifier: MPL-2.0

package com.uncommongoods.tugboat.bridge.update;

/** POJO form of latest.json published alongside each MSI on S3. */
public class UpdateManifest {
    public String version;
    public String releaseDate;
    public String installerUrl;
    public String sha256;
    public long size;
    public String releaseNotes;

    public boolean isValid() {
        return version != null && !version.isBlank()
            && installerUrl != null && !installerUrl.isBlank()
            && sha256 != null && !sha256.isBlank();
    }
}
