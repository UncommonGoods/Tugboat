// Copyright (c) 2025 Uncommon Goods LLC
// SPDX-License-Identifier: MPL-2.0

package com.uncommongoods.tugboat.bridge;

/**
 * Exception thrown when shipping is cancelled by user action or validation failure
 */
public class ShippingCancelledException extends RuntimeException {
    public ShippingCancelledException(String message) {
        super(message);
    }
}
