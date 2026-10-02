# Tugboat Hooks ⚓

Reference implementations of the Tugboat engine's lifecycle hooks.

Hooks are the supported extension point for injecting your own logic into a
shipment's lifecycle — recording state to a database, mutating the shipment,
rewriting errors, etc. — **without modifying the engine**. The engine
calls your hooks; you never call the engine from a hook.

Because this module is MIT-licensed, you may customize these hooks and keep your
changes private, even when they are linked into the MPL applications (`bridge`,
`xo`) — MPL's copyleft is file-level and reaches only the files you modify. See
[LICENSING.md](../LICENSING.md) at the repo root.

## How hooks work

A Tugboat moves through a fixed state machine. As it moves, the engine invokes
the corresponding hook (if one is set), handing it the live `Tugboat` instance:

```
INITIAL ──▶ INITIALIZED ──▶ RATED ──▶ SHOPPED ──▶ PURCHASED ──▶ PRINTED
   │             │            │           │            │            │
initialHook   ratedHook  shoppedHook purchasedHook  (print)    printedHook
                                                                   │
                                                              voidedHook (on void)
```

**The initial hook runs *before* its transition; every other hook runs *after*
its own.** That is deliberate: the initial hook is where you populate the
shipment — look up the order, set the destination address and parcel — so that
`initialize()` has something to validate. By the time `ratedHook` runs, rating
has already happened and the rates are on the Tugboat. The exact ordering is
tabulated in [`engine/README.md`](../engine/README.md#hooks-and-when-they-fire).

Any exception thrown during an operation is routed to the **error hook**, which
receives both the `Tugboat` and the `Exception` and may rewrite or re-throw it.

There are two interfaces, both declared in `engine`:

```java
public interface TugboatHook {
    void execute(Tugboat tugboat) throws TugboatException;
}

public interface TugboatErrorHook {
    void execute(Tugboat tugboat, Exception ex) throws TugboatException;
}
```

Inside `execute(...)` the `Tugboat` is fully readable and mutable — you can call
`getCargoId()`, `getMetadata()`, `getOptions()`, `getSelectedRate()`,
`getPostageLabels()`, `setReference(...)`, `setParcels(...)`, and so on.

## What ships in this module

The reference implementations in `src/main/java/.../hooks/impl/`. Four of them
are deliberately empty — they mark the seam and do nothing until you fill them
in. The other three are real, and are worth reading before you replace them:

| Hook | What it does today |
|---|---|
| `InitialHookImpl` | Sets a shipment reference (`OB{cargoId}-{random}`) and turns off `selectLowestRate`, so rate shopping honors the delivery date instead of just taking the cheapest. **This is where most people put their order lookup.** |
| `PrintedHookImpl` | When the Tugboat's metadata marks it a letter, rewrites ZPL field origins in the label so the print lands correctly on letter stock. |
| `ErrorHookImpl` | Turns engine errors into something a packer can act on: rewrites "cannot go backwards from printed" into "Already shipped. Must be voided to re-ship.", prefixes `HAZMAT` or `ESW International Error:` when relevant, and appends a ZPL barcode plus the shipment id and carrier/service so the error can be printed and scanned. |
| `RatedHookImpl`, `ShoppedHookImpl`, `PurchasedHookImpl`, `VoidedHookImpl` | Empty. Your slots. |

The originals talked to a specific company's database and dispatch service. That
code is gone; what remains is the shape plus the parts that are generally useful.

## Customizing a hook

Each hook is an ordinary class implementing one of the two interfaces. To
customize behavior, edit the relevant `*HookImpl` in this module or write your
own class. Then wire it onto `TugboatOptions` and pass those options to the
builder — the engine picks it up automatically. Hooks are optional; any hook you
don't set is simply skipped.

```java
package com.example.hooks;

import com.uncommongoods.tugboat.engine.Tugboat;
import com.uncommongoods.tugboat.engine.exception.TugboatException;
import com.uncommongoods.tugboat.engine.hooks.TugboatHook;

// 1. Implement the hook. This one runs right after the label is purchased
//    and records the carrier + tracking selection somewhere of your choosing.
public class RecordPurchaseHook implements TugboatHook {

    @Override
    public void execute(Tugboat tugboat) throws TugboatException {
        var rate = tugboat.getSelectedRate();
        if (rate != null) {
            System.out.printf("Purchased cargo %s via %s %s%n",
                    tugboat.getCargoId(), rate.getCarrier(), rate.getService());
            // ...persist to a database, emit an event, call a service, etc.
        }
    }
}
```

```java
// 2. Wire it onto the options and build the Tugboat. Mix and match the
//    reference hooks with your own.
TugboatOptions options = new TugboatOptions();
options.setLabelFormat("ZPL");
options.setInitialHook(new com.uncommongoods.tugboat.engine.hooks.impl.InitialHookImpl());
options.setPurchasedHook(new RecordPurchaseHook());   // your custom hook

Tugboat tugboat = Tugboat.builder(engineConfig, cargoId)
        .options(options)
        .build();

tugboat.purchase();   // engine fires RecordPurchaseHook after the purchase succeeds
```

To customize error handling instead, implement `TugboatErrorHook` and set it
with `options.setErrorHook(...)`; it will be called whenever any operation throws.

## Wiring all hooks at once: `TugboatHooks`

This module ships `TugboatHooks`, an implementation of the engine's
`TugboatHookProvider` SPI that builds every reference hook and wires them onto
an options instance in one call:

```java
TugboatHookProvider hooks = new TugboatHooks();

TugboatOptions options = new TugboatOptions();
options.setLabelFormat("ZPL");
hooks.applyTo(options);

// ...build and drive Tugboats with these options...

hooks.close();   // releases any resources the provider owns (none by default)
```

Both host applications (`bridge`, `xo`) construct their hooks exactly this way —
through the `TugboatHookProvider` interface, with no arguments. Customizing what
the hooks do (including what they connect to) is entirely contained in this
module.

## Configuration: settings read like environment variables

Hooks do not receive configuration through constructors. Instead they read named
values at runtime via `TugboatSettings` (from the `ports` module):

```java
import com.uncommongoods.tugboat.engine.ports.config.TugboatSettings;

String dbUrl = TugboatSettings.get("MY_DB_URL");
String token = TugboatSettings.get("MY_SERVICE_TOKEN", "default-value");
```

`TugboatSettings.get(name)` resolves, in order:

1. values **published by the host** — the Bridge publishes every row of its
   Settings → Data section (the freeform name/value rows), so a row named
   `MY_DB_URL` is immediately visible to hooks;
2. JVM system properties (`-DMY_DB_URL=...`);
3. environment variables, first by the exact name, then by the normalized name
   (uppercased, non-alphanumerics → `_`, so a row named "Cache URL" and an env
   var `CACHE_URL` converge).

The same hook code therefore works in both hosts with zero host changes: in the
Bridge, users type rows into the settings page; in XO (a server), users set
plain environment variables.

### Owning a data connection

Because configuration arrives by name, hooks that need a database own the whole
connection concern themselves — the host apps never see a JDBC URL, driver, or
credentials. Add your driver/pool dependencies to *this* module's
`build.gradle.kts`, acquire the resource in your `TugboatHookProvider`, and
release it in `close()`:

```java
public final class MyHooks implements TugboatHookProvider {

    private final HikariDataSource dataSource;

    public MyHooks() {
        HikariConfig hc = new HikariConfig();
        hc.setJdbcUrl(TugboatSettings.get("MY_DB_URL"));
        hc.setUsername(TugboatSettings.get("MY_DB_USER"));
        hc.setPassword(TugboatSettings.get("MY_DB_PASSWORD"));
        this.dataSource = new HikariDataSource(hc);
        // construct hooks that share this.dataSource ...
    }

    @Override
    public void applyTo(TugboatOptions options) { /* set your hooks */ }

    @Override
    public void close() {
        dataSource.close();
    }
}
```

Which names you read is up to you — document them for your deployment, and set
them either as Bridge settings rows or as environment variables.

## Build

```bash
./gradlew :tugboat-hooks:build
```

No Docker and no external services — the reference hooks have no dependencies
beyond the engine and the shipping adapters. If you add a database driver or a
connection pool, it goes in *this* module's `build.gradle.kts`; the host
applications never need to know about it.

There is no separate deployment step. `bridge` and `xo` both depend on this
module directly, so rebuilding either picks up your changes:

```bash
./gradlew :tugboat-bridge:run          # exercise hooks in the desktop app
./gradlew :playground:run              # or from a plain Java main (see below)
```

To verify a hook is actually being called, the quickest check is `playground`, a
scratch harness for driving the engine from a plain `main`. It is **not tracked in
version control**, so the `:playground` task exists only in working copies that
have the directory on disk; see `playground/README.md` there.
