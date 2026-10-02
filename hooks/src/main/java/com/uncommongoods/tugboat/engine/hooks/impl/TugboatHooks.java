package com.uncommongoods.tugboat.engine.hooks.impl;

import com.uncommongoods.tugboat.engine.TugboatOptions;
import com.uncommongoods.tugboat.engine.hooks.TugboatHookProvider;

/**
 * Default {@link TugboatHookProvider}: builds the reference hook
 * implementations and wires them onto a {@link TugboatOptions} in one call.
 *
 * <p>Hooks needing configuration (connection URLs, credentials, service
 * endpoints) read it by name via
 * {@code com.uncommongoods.tugboat.engine.ports.config.TugboatSettings} —
 * satisfied by the Bridge settings page or by plain environment variables —
 * rather than through constructor arguments. A provider that owns resources
 * (e.g. a connection pool shared by its hooks) should acquire them in the
 * constructor and release them in {@link #close()}.
 */
public final class TugboatHooks implements TugboatHookProvider {

    private final InitialHookImpl initialHook = new InitialHookImpl();
    private final RatedHookImpl ratedHook = new RatedHookImpl();
    private final ShoppedHookImpl shoppedHook = new ShoppedHookImpl();
    private final PurchasedHookImpl purchasedHook = new PurchasedHookImpl();
    private final PrintedHookImpl printedHook = new PrintedHookImpl();
    private final VoidedHookImpl voidedHook = new VoidedHookImpl();
    private final ErrorHookImpl errorHook = new ErrorHookImpl();

    @Override
    public void applyTo(TugboatOptions options) {
        options.setInitialHook(initialHook);
        options.setRatedHook(ratedHook);
        options.setShoppedHook(shoppedHook);
        options.setPurchasedHook(purchasedHook);
        options.setPrintedHook(printedHook);
        options.setVoidedHook(voidedHook);
        options.setErrorHook(errorHook);
    }

    @Override
    public void close() {
        // The reference hooks hold no resources.
    }
}
