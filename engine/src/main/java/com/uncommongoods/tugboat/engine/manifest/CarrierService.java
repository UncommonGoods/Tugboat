// Copyright (c) 2025 Uncommon Goods LLC
// SPDX-License-Identifier: MPL-2.0

package com.uncommongoods.tugboat.engine.manifest;

import com.google.gson.annotations.Expose;

public record CarrierService(@Expose String carrier, @Expose String service) {
}
