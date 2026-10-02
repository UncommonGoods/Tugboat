# Tugboat Engine 𖣘

The Engine is the core of Tugboat. It wraps a shipping API (like EasyPost) into a predictable state machine, executes custom business logic at each step, and automatically caches progress. It also provides out-of-the-box support for pickup facilities, manifest batches, and delivery date shopping.

The Engine depends on [`ports`](../ports/README.md), meaning any compliant carrier or cache API can be plugged in.

## Contents

- [The State Machine](#the-state-machine)
- [Hooks and Execution Order](#hooks-and-execution-order)
- [Caching and Locking](#caching-and-locking)
- [Configuration](#configuration)
- [A Minimal Shipment](#a-minimal-shipment)
- [Pickup Facilities](#pickup-facilities)
- [Rating Against Pickup Dates](#rating-against-pickup-dates)
- [Reset and Void Operations](#reset-and-void-operations)
- [Build and Test](#build-and-test)
- [Licensing](#licensing)

## The State Machine

Tugboat models the shipping workflow as a strict state machine. This makes the lifecycle predictable, easier to debug, and provides exact execution points for custom logic or pickup groups. Most importantly, the state machine is what enables Tugboat to enforce hard delivery deadlines during rate shopping.

```
INITIAL ──▶ INITIALIZED ──▶ RATED ──▶ SHOPPED ──▶ PURCHASED ──▶ PRINTED
                                                       │            │
                                                       └──▶ VOIDED ◀┘
```

| State | Transition Method | Description |
|---|---|---|
| `INITIAL` | `Tugboat.builder(...).build()` | Creates a new Tugboat object with basic shipment info. |
| `INITIALIZED` | `initialize()` | Validates addresses and parcels against pickup groups. This is the ideal stage for Hooks to fetch data from an external database. |
| `RATED` | `rate()` | Queries the carrier API for the shipment and retrieves available rates. |
| `SHOPPED` | `shop()` | Selects a rate based on delivery date rules and binds the shipment to a manifest batch (if a facility is configured). |
| `PURCHASED` | `purchase()` | Buys the postage and attaches labels and tracking codes to the Tugboat object. |
| `PRINTED` | `print()` | Returns the labels. Calling `reprint()` returns them again. |
| `VOIDED` | `voidLabel()` | Refunds the label. This can be called from either `PURCHASED` or `PRINTED`. |

States are implemented as separate classes (e.g., `InitializedTugboatState` implements `ITugboatState`). Attempting an illegal transition throws an `IllegalStateException` indicating the missed steps:

```java
tugboat.purchase();
// IllegalStateException: Cannot purchase from initialized state.
//                        Must rate and shop first.
```

The state machine automatically executes intermediate steps. For example, invoking `tugboat.purchase()` on a new cargo ID will automatically execute `initialize()`, `rate()`, `shop()`, and `purchase()` sequentially.

## Hooks and Execution Order

Business logic is injected using Hooks attached to `TugboatOptions` (see the full guide in [`hooks/README.md`](../hooks/README.md)).

**Execution Order:** The initial hook runs *before* the first transition, providing an opportunity to look up the order and populate the shipment data. Every subsequent hook runs *after* its respective transition.

| Transition / Operation | Execution Sequence |
|---|---|
| `INITIAL → INITIALIZED` | `initialHook.execute(tugboat)` **then** `initialize()` |
| `INITIALIZED → RATED` | `rate()` **then** `ratedHook.execute(tugboat)` |
| `RATED → SHOPPED` | `shop()` **then** `shoppedHook.execute(tugboat)` |
| `SHOPPED → PURCHASED` | `purchase()` **then** `purchasedHook.execute(tugboat)` |
| `print()` | Labels fetched **then** `printedHook.execute(tugboat)` |
| `voidLabel()` | Void executed **then** `voidedHook.execute(tugboat)` |

If an exception is thrown during any step, it is routed to `errorHook.execute(tugboat, ex)`, where it can be intercepted, logged, or rewritten. All hooks are optional.

## Caching and Locking

After every state transition, the engine saves the shipment's full state to the cache and releases the cargo lock. This means if a process terminates or work is intentionally split across time, another process can seamlessly resume the workflow.

- **State Key:** `{cachePrefix}:TBCARGO:{cargoId}` (JSON payload, defaults to a two-week expiration).
- **Cargo Lock:** `{cachePrefix}:TBCARGOLOCK:{cargoId}` (30-second lease). Concurrent requests attempting to acquire the same cargo ID will result in a `TugboatException`. 
  *Note: `BypassTugboatLock.BYPASS_TUGBOAT_LOCK` can be passed to bypass the check if exclusive access is guaranteed.*

```java
Tugboat tugboat = Tugboat.builder(engineConfig, "SHIP-1001").build();
tugboat.retrieve(); // Loads cached state if it exists; otherwise a no-op
```

**Cacheless Operation:** Building an `EngineConfig` without a cache client degrades gracefully. Rating, purchasing, and printing function normally within a single process, but state persistence, cross-process locking, pickup facilities, and manifests are disabled.

## Configuration

```java
EngineConfig engineConfig = EngineConfig.builder()
        .withShippingClient(easyPostClient)              // The default client
        .withShippingClient("esw", intlClient)           // Additional clients by key
        .withCacheClient(cacheClient)                    // Optional
        .withCachePrefix("TUGBOAT")                      // Namespace for cache keys
        .withCacheExpirySeconds(1_209_600)               // Optional (~2 weeks)
        .build();
```

Shipping clients are keyed. The engine uses the `"default"` key unless `shippingClientKey` is explicitly set on the Tugboat instance. This allows a single engine to drive multiple aggregators concurrently. Host applications typically instantiate these clients dynamically via factories; see [`adapters/README.md`](../adapters/README.md).

## A Minimal Shipment

A minimal rating, purchasing, and printing workflow (bypassing facilities and manifests):

```java
import com.uncommongoods.tugboat.engine.Tugboat;
import com.uncommongoods.tugboat.engine.TugboatOptions;
import com.uncommongoods.tugboat.engine.config.EngineConfig;
import com.uncommongoods.tugboat.engine.model.TugboatAddress;
import com.uncommongoods.tugboat.engine.model.TugboatParcel;
import com.uncommongoods.tugboat.engine.ports.shipping.model.IPostageLabel;

import java.time.LocalDate;
import java.util.List;

TugboatOptions options = new TugboatOptions();
options.setLabelFormat("ZPL");
options.setPostageLabelInline(true);
options.setSelectLowestRate(true); // Disables delivery-date constraints

Tugboat tugboat = Tugboat.builder(engineConfig, "SHIP-1001")
        .originAddress(new TugboatAddress(
                "Warehouse", "123 Dock St", "", "Brooklyn", "NY", "11222", "US",
                "5555550100", "ops@example.com", false))
        .destinationAddress(new TugboatAddress(
                "Jane Roe", "456 Main St", "Apt 2", "Columbus", "OH", "43215", "US",
                "5555550111", "jane@example.com", true))
        .parcel(new TugboatParcel(32.0f, 12.0f, 9.0f, 4.0f)) // Weight (oz), L, W, H (in)
        .expectedDeliveryDate(LocalDate.now().plusDays(4))
        .options(options)
        .build();

tugboat.purchase(); // Executes initialize, rate, shop, and purchase sequentially

List<IPostageLabel> labels = tugboat.print();
labels.forEach(pl -> System.out.println(pl.getLabelFile()));

System.out.println(tugboat.getSelectedRate().getCarrier() + " " + 
                   tugboat.getSelectedRate().getService() + " $" + 
                   tugboat.getSelectedRate().getRate());
        
System.out.println(tugboat.getTrackingCodes());
```

*Note: All four dimensions of `TugboatParcel` must be strictly greater than zero to pass initialization. `TugboatAddress` and `TugboatParcel` can be initialized from a `Map<String, Object>` for JSON deserialization.*

## Pickup Facilities

A `PickupFacility` represents a physical shipping location mapped to a short code (e.g., "BK" for Brooklyn). It manages `PickupGroup` instances for each carrier pickup. Each group tracks:

- **Carrier Account ID**: The billing account for the label.
- **Accepted Services**: A set of `CarrierService` records the pickup supports.
- **Pickup Date**: The actual departure date of the carrier's truck.
- **Manifest Batch**: An aggregate of parcels assigned to the pickup.

```java
PickupFacility facility = new PickupFacility(engineConfig, "BK", warehouseAddress);
facility.create();

String upsGroupId = facility.createPickupGroup("UPS-AFTERNOON", "ca_ups_account_id", LocalDate.now());
facility.addServiceToGroup("UPS-AFTERNOON", new CarrierService("UPS", "Ground"));
facility.addServiceToGroup("UPS-AFTERNOON", new CarrierService("UPS", "NextDayAir"));

String uspsGroupId = facility.createPickupGroup("USPS-MORNING", "ca_usps_account_id", LocalDate.now().plusDays(1));
facility.addServiceToGroup("USPS-MORNING", new CarrierService("USPS", "Priority"));
```

Assigning a facility during Tugboat instantiation enforces pickup logic:

```java
Tugboat tugboat = Tugboat.builder(engineConfig, "SHIP-1002")
        .pickupFacility(facility)
        .expectedDeliveryDate(LocalDate.now().plusDays(3))
        .destinationAddress(destination)
        .parcel(parcel)
        .options(options)
        .build();
```

Closing a group upon truck departure finalizes the manifest batch and automatically initializes a new batch for the subsequent pickup:

```java
facility.closePickupGroup("UPS-AFTERNOON", LocalDate.now().plusDays(1));
```

*Note: Facilities mandate a cache client. Pickup groups will reject dates in the past both during closure and when assigning parcels to a batch.*

## Rating Against Pickup Dates

When a `PickupFacility` is active, the engine intelligently shops for rates based on the actual pickup date. 

Assuming a facility with a UPS pickup today (Monday) and a USPS pickup tomorrow (Tuesday), rating a parcel with a delivery deadline of Thursday (`expectedDeliveryDate = Monday + 3`) triggers the following logic:

**1. Batched Rate Requests**
The engine partitions requests by `(shipping client, pickup date)`, defining a `date_advance` (the offset in days between today and the pickup date, floored at zero).
- **Request A (UPS):** `date_advance = 0` (ships today).
- **Request B (USPS):** `date_advance = 1` (ships tomorrow).
The carrier evaluates Request B as a Tuesday shipment, ensuring accurate transit estimations. Results are merged into a unified rate list.

**2. Rate Shopping and Filtering**
During `shop()`, the engine applies the following filters:
- Drops rates for services not explicitly supported by an active pickup group.
- Drops rates priced at or below $0.01.
- Filters out rates that fail to arrive on or before the `expectedDeliveryDate`, deriving the arrival time from the carrier's estimate relative to the assigned ship date. 
- Selects the cheapest remaining rate.

**Overrides:**
| Configuration | Behavior |
|---|---|
| `options.setSelectedCarrierService(new CarrierService("UPS", "Ground"))` | Forces selection of the cheapest rate for the exact service; bypasses delivery date logic. |
| `options.setSelectLowestRate(true)` | Falls back to the absolute cheapest rate if no options meet the delivery deadline. |
| *(None)* | Throws `TugboatException: No deliverable service found` if deadlines cannot be met. |

**3. Purchase and Batch Assignment**
`purchase()` acquires the postage, and the parcel joins the manifest batch of the winning pickup group. If the group's pickup date has elapsed in the interim, the purchase is voided and the state machine forces the pickup date to advance to the next available slot before repeating the transition.

*Note: For multi-parcel shipments, the engine utilizes the carrier's Orders API instead of Shipments (supported by adapters like EasyPost).*

## Reset and Void Operations

The state machine is strictly unidirectional. 
- `reset()` returns a Tugboat to the `INITIAL` state for reprocessing.
- `voidLabel()` refunds a label from the `PURCHASED` or `PRINTED` states. Re-shipping requires voiding the existing label first.

Overloads allow preserving specific shipment components during transitions, preventing redundant data derivation:

```java
tugboat.initialize(List.of(ShipmentComponent.PARCELS));
```
Valid `ShipmentComponent` values include `ORIGIN_ADDRESS`, `DESTINATION_ADDRESS`, `RETURN_ADDRESS`, and `PARCELS`. Unlisted components are wiped and re-derived (typically by the initial hook).

## Build and Test

```bash
./gradlew :tugboat-engine:build          # Compile and package
./gradlew :tugboat-engine:test           # Requires a running Docker daemon
./gradlew :tugboat-engine:publishToMavenLocal
```

The test suite (`TugboatTest`, `PickupFacilityTest`) provisions a `redis:7-alpine` container via Testcontainers. A running Docker daemon is mandatory for test execution. To bypass tests during compilation:

```bash
./gradlew build -x :tugboat-engine:test
```

Published as `com.uncommongoods.tugboat:tugboat-engine`.

## Licensing

The Engine is licensed under the Mozilla Public License 2.0 (`LICENSE`). Modifications to these files distributed as a product must be published. However, utilizing the unmodified Engine as a library within a commercial product is permitted, and proprietary hooks or adapters remain private. Internal execution of a modified copy does not trigger publication requirements. For details, see [`LICENSING.md`](../LICENSING.md).
