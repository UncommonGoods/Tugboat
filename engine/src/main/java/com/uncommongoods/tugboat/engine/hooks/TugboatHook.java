// Copyright (c) 2025 Uncommon Goods LLC
// SPDX-License-Identifier: MPL-2.0

package com.uncommongoods.tugboat.engine.hooks;

import com.uncommongoods.tugboat.engine.Tugboat;
import com.uncommongoods.tugboat.engine.exception.TugboatException;

public interface TugboatHook {
    void execute(Tugboat tugboat) throws TugboatException;
}
