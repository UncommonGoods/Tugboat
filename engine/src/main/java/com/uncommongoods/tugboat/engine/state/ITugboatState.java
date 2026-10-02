// Copyright (c) 2025 Uncommon Goods LLC
// SPDX-License-Identifier: MPL-2.0

package com.uncommongoods.tugboat.engine.state;

import com.uncommongoods.tugboat.engine.ports.shipping.model.IPostageLabel;
import com.uncommongoods.tugboat.engine.exception.TugboatException;

import java.util.List;

public interface ITugboatState {

    ITugboatState initialize() throws TugboatException, IllegalStateException;

    ITugboatState rate() throws TugboatException, IllegalStateException;

    ITugboatState shop() throws TugboatException, IllegalStateException;

    ITugboatState purchase() throws TugboatException, IllegalStateException;

    List<IPostageLabel> print() throws TugboatException, IllegalStateException;

    List<IPostageLabel> reprint() throws TugboatException, IllegalStateException;

    ITugboatState voidLabel() throws TugboatException, IllegalStateException;
}
