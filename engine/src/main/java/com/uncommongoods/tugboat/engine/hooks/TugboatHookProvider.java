// Copyright (c) 2025 Uncommon Goods LLC
// SPDX-License-Identifier: MPL-2.0

package com.uncommongoods.tugboat.engine.hooks;

import com.uncommongoods.tugboat.engine.TugboatOptions;

/**
 * Supplies a fully wired set of lifecycle hooks, backed by whatever resources
 * (database connection, services, etc.) the provider owns.
 *
 * <p>The engine declares this contract; implementations live in the hooks
 * module(s). Hosts depend on this interface rather than a concrete provider,
 * so they need no compile-time knowledge of how the hooks are built or what
 * they connect to.
 *
 * <p>Extends {@link AutoCloseable} and narrows {@link #close()} to throw no
 * checked exception, so callers can release the provider's resources without
 * handling {@code Exception}.
 */
public interface TugboatHookProvider extends AutoCloseable {

    /** Wire every hook this provider supplies onto the given options. */
    void applyTo(TugboatOptions options);

    /** Release any resources held by this provider (e.g. a connection pool). */
    @Override
    void close();
}
