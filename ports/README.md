# Tugboat Ports 🏗️

The `ports` module defines the core interfaces, types, and service-provider contracts for the Tugboat engine. By implementing 
these interfaces, external shipping APIs and caching mechanisms can be easily integrated into the engine.

The `engine` and all adapters depend on `ports`.

## Architecture

The engine operates on a standardized shipping vocabulary (shipments, rates, parcels, addresses, labels, trackers) 
originally modeled after the [EasyPost Java](https://github.com/EasyPost/easypost-java.git) client. Rather than coupling 
the engine to multiple vendor-specific domain models, vendors are integrated via adapters that translate their models into 
these standard `ports` interfaces. 

This design means that the `engine` remains agnostic to specific implementations (e.g., EasyPost, ESW, Redis). 
Introducing a new carrier aggregator or cache client requires only a new adapter module, with zero modifications to the core engine.

## Components

*Note: `ShipmentOptions` and `ShipmentOptionValues` are concrete classes defining the exhaustive option surface (e.g., label format,
hazmat, dry ice). The engine extends this via `TugboatOptions` to add internal properties and hook slots.*

### Shipping Services (`shipping/service/`)

`IShippingClient` serves as the primary entry point provided by an adapter, exposing resource-specific services:

```java
public interface IShippingClient {
    IAddressService        getAddressService();
    IBatchService          getBatchService();
    IParcelService         getParcelService();
    IRateService           getRateService();
    IShipmentService       getShipmentService();
    ICarrierAccountService getCarrierAccountService();
    IOrderService          getOrderService();
    IRefundService         getRefundService();
    IScanFormService       getScanFormService();
    ITrackerService        getTrackerService();

    int    getConnectionTimeoutMilliseconds();
    int    getReadTimeoutMilliseconds();
    String getApiKey();
    String getApiVersion();
    String getApiBase();
}
```

The engine uses a strict subset of these services: `getShipmentService().create/buy`, `getOrderService().create/buy`, 
`getAddressService().create`, and `getCarrierAccountService().all()`. Adapters for APIs lacking certain concepts (e.g., 
scan forms) may safely throw an `UnsupportedOperationException` for those endpoints.


### Cache (`cache/ICacheClient`)

The cache interface is based on Redis's [Jedis](https://github.com/redis/jedis) library.

While the interface is covers the whole Redis API, the core engine currently requires only `get`, `set`, `setEx`, `del`, `hgetAll`, and 
`hset`. Unused operations can throw `UnsupportedOperationException`.

### Configuration (`config/TugboatSettings`)

`TugboatSettings` has a unified, environment-variable-style configuration lookup mechanism for both hooks and adapters, 
ensuring portability across both desktop and server environments.

```java
String url   = TugboatSettings.get("MY_SERVICE_URL");
String token = TugboatSettings.get("MY_TOKEN", "default-value");
```

Property resolution order (`get(name)`):
1. Runtime values explicitly published via `TugboatSettings.publish(map)` (e.g., active UI settings from the Bridge).
2. JVM System Properties: `System.getProperty(name)` (e.g., `-DMY_SERVICE_URL=...`).
3. Exact Environment Variables: `System.getenv(name)`.
4. Normalized Environment Variables: `System.getenv(normalize(name))` (uppercased, non-alphanumeric characters replaced by `_`).

*Note: `TugboatSettings.publish(map)` completely overwrites the published state, ensuring only one environment's configuration 
is active at a time.*

### Factory Service Provider Interfaces (SPI)

Adapters are instantiated via factories discovered at runtime using `java.util.ServiceLoader`.

```java
public interface ShippingClientFactory {
    String       type();          // e.g., "easypost"
    List<String> configKeys();    // e.g., ["API Key"]
    IShippingClient create(Map<String, String> config);
}

public interface CacheClientFactory {
    String       type();          // e.g., "redis"
    List<String> configKeys();    // e.g., ["Cache URL", "Cache Port"]
    ICacheClient create(Map<String, String> config);
}
```

The `configKeys()` array dictates required configuration. The Bridge dynamically renders UI input fields based on these keys, 
while XO automatically resolves them as environment variables (via the normalization rules above).

Not every SPI needs a factory. A host uses at most one of each and never asks anyone to choose it, so `ServiceLoader`
instantiates these directly:

```java
public interface TokenValidator {
    boolean isValid(String credential);  // authorize an inbound request
}
```

**Discovery Behavior:**
- **Shipping Clients:** Can co-exist. Users select the active client by `type()`.
- **Cache Clients:** Singleton. Hosts initialize the first discovered factory unless the `CACHE_TYPE` property explicitly 
- specifies a provider.
- **Token Validators:** Singleton, optional. With none registered, a host serves every request unauthenticated — which is
  what lets a build with no auth service run at all, and which hosts are expected to announce loudly at startup.
  Validation fails closed: an implementation returns `false` for a credential it knows to be bad, and *throws* when it
  cannot tell, so an outage in whatever vouches for the credential refuses requests instead of waving them through.

To register a factory, provide its fully-qualified class name in a file at `src/main/resources/META-INF/services/<interface FQCN>`. 
See [`adapters/README.md`](../adapters/README.md) for examples.

### Exceptions

- `TugboatException`: The primary checked exception for engine operations.
- `EndOfPaginationException`: Raised upon reaching the end of a paginated collection.

## Implementing an Adapter

To implement a new integration (detailed guide in [`adapters/README.md`](../adapters/README.md)):

1. Create a new Gradle module depending on `:tugboat-ports`.
2. Implement `IShippingClient` (or `ICacheClient`) and translate vendor models into the required `I*` interfaces.
3. Implement the corresponding factory (`ShippingClientFactory` / `CacheClientFactory`) and declare required configuration keys.
4. Register the factory under `META-INF/services/`.
5. Add the module as a dependency in the target host's `build.gradle.kts`.

## Build and Test

```bash
./gradlew :tugboat-ports:build
./gradlew :tugboat-ports:test              # Executes TugboatSettingsTest
./gradlew :tugboat-ports:publishToMavenLocal
```

This module has no external runtime dependencies and requires no external services to build or test. 
Published as `com.uncommongoods.tugboat:tugboat-ports`.

## Licensing

The `ports` module is licensed under the MIT License (`LICENSE`). You may implement these interfaces in private, proprietary 
modules and link them into the Bridge or XO applications without open-sourcing your code. The MPL 2.0 license of the host 
applications applies only to modifications of their respective files, not to independently developed modules linked alongside them (§3.3).
