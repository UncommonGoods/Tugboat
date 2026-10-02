# Adapters ⚙️

Adapters implement the interfaces defined in [`ports`](../ports/README.md). They integrate external services (such as shipping APIs or caches) into the engine. Each adapter provides a factory, allowing host applications to instantiate them via configuration with no compile-time dependencies.

| Module | Gradle project | Implements | `type()` | `configKeys()` |
|---|---|---|---|---|
| [`easypost`](easypost/README.md) | `:adapters:tugboat-easypost` | `IShippingClient` | `easypost` | `API Key` |
| [`redis`](redis/README.md) | `:adapters:tugboat-redis` | `ICacheClient` | `redis` | `Cache URL`, `Cache Port` |

## Discovery

Hosts use `ServiceLoader` to discover adapters at runtime. Adapters register their factories in a `META-INF/services` file. There are two such registrations: `ShippingClientFactory` for the client, and `ModelTypeProvider` for the models it returns (see step 5 below).

Example path:
`adapters/easypost/src/main/resources/META-INF/services/com.uncommongoods.tugboat.engine.ports.shipping.service.ShippingClientFactory`

Containing the fully qualified class name:
`com.uncommongoods.tugboat.engine.ports.shipping.service.EasyPostClientFactory`

The Bridge UI dynamically builds its settings from these discovered factories, and XO resolves the required configuration keys from environment variables.

- **Shipping Clients:** Multiple can be active simultaneously, keyed by name.
- **Cache Clients:** Only one is active at a time. If multiple are present, the `CACHE_TYPE` environment variable dictates which to use.

## Creating a Custom Adapter

A new adapter (e.g., Shippo) is created through the following steps:

**1. Create a module depending on `ports`:**
```kotlin
// adapters/shippo/build.gradle.kts
plugins { `java-library` }
dependencies {
    api(project(":tugboat-ports"))
    implementation("com.shippo:shippo-java-client:x.y.z")
}
```

**2. Implement the interface:**
An `IShippingClient` is created that maps the vendor's models to the `I*` interfaces (e.g., `IShipment`, `IAddress`). Unsupported operations can throw `UnsupportedOperationException`.

**3. Implement the factory:**
```java
package com.uncommongoods.tugboat.engine.ports.shipping.service;

import java.util.List;
import java.util.Map;

public class ShippoClientFactory implements ShippingClientFactory {
  public static final String API_KEY = "API Key";

  @Override
  public String type() { return "shippo"; }

  @Override
  public List<String> configKeys() { return List.of(API_KEY); }

  @Override
  public IShippingClient create(Map<String, String> config) {
    String apiKey = config.get(API_KEY);
    if (apiKey == null || apiKey.isBlank()) {
      throw new IllegalArgumentException("shippo client requires '" + API_KEY + "'");
    }
    return new ShippoShippingClient(apiKey);
  }
}
```
Throwing an `IllegalArgumentException` for invalid configuration allows hosts to log the error and continue loading other clients. Configuration keys define Bridge UI fields and map directly to environment variables (e.g., `"API Key"` → `API_KEY`).

**4. Register the factory:**
The fully qualified factory class name is added to:
`adapters/shippo/src/main/resources/META-INF/services/com.uncommongoods.tugboat.engine.ports.shipping.service.ShippingClientFactory`

**5. Register the models for persistence:**
Tugboat persists a cargo to its cache between state transitions, so the models an
adapter returns have to survive a JSON round trip. Which concrete class rebuilds a
given object is decided by a second SPI, `ModelTypeProvider`:

```java
package com.uncommongoods.tugboat.engine.ports.shipping.model;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public class ShippoModelTypeProvider implements ModelTypeProvider {

  @Override
  public String providerType() { return "shippo"; }

  @Override
  public Set<EntityType> supportedTypes() {
    return EnumSet.of(EntityType.ADDRESS, EntityType.SHIPMENT, EntityType.RATE);
  }

  @Override
  public Class<? extends JsonSerializable> modelClass(EntityType type) {
    switch (type) {
      case ADDRESS:  return ShippoAddress.class;
      case SHIPMENT: return ShippoShipment.class;
      case RATE:     return ShippoRate.class;
      default:       return null;
    }
  }

  @Override
  public JsonSerializable fromMap(EntityType type, Map<String, Object> map, ModelResolver resolver) {
    switch (type) {
      case ADDRESS:  return new ShippoAddress(map);
      case SHIPMENT: return new ShippoShipment(map, resolver);
      case RATE:     return new ShippoRate(map);
      default:       throw new IllegalArgumentException("shippo does not handle " + type);
    }
  }
}
```

Registered the same way as the client factory, in
`META-INF/services/com.uncommongoods.tugboat.engine.ports.shipping.model.ModelTypeProvider`.

Three things are worth knowing:

- **`providerType()` must equal `getProviderType()` on every model it lists.** The
  engine writes that value into each serialized object as `_type` and looks it back
  up here. A disagreement is rejected the first time a model is written, rather
  than surfacing later as a model that will not load.
- **Only register a type the adapter can actually produce.** An unregistered
  `_type` is an error with a message naming what *is* registered -- it is never
  guessed at.
- **Rebuild nested models with `resolver`, not `new`.** `resolver.resolve(...)` and
  `resolveList(...)` reconstruct a child as whichever provider produced it. A child
  carrying no tag of its own is built as the parent's provider, which is what a
  single client's response means. `MapValues` has coercion helpers for the decoded
  values -- JSON arriving without a target type means every number is a `Double`
  and every date a `String`.

**6. Add to the classpath:**
Nothing to do. `settings.gradle.kts` scans `adapters/` for directories holding a build script and includes each as `:adapters:tugboat-<dir>`; the Bridge and XO depend on whatever it finds. Creating `adapters/shippo/` in step 1 was the whole of it — no `include()` line and no host dependency to add.

## Drop-in Jars

An adapter that is built elsewhere does not need to be a module in this repository at all. Any jar placed in the repository's `libs/` directory joins the Bridge and XO runtime classpath, where `ServiceLoader` discovers its factory exactly as it would a module's:

```
libs/shippo-adapter.jar
```

Then build the host as usual (`./gradlew :tugboat-xo:build`). The jars themselves are gitignored.

**The jar must carry its own dependencies.** A `libs/` jar is a plain file dependency, so Gradle resolves nothing transitively — the vendor SDK the adapter calls has to travel with it. Either shade the adapter and its SDK into one jar, or drop the adapter's dependency jars into `libs/` alongside it.

**Do not bundle `tugboat-ports` or gson.** The host already provides both. Re-bundling them puts duplicate classes into the fat jar, and the copy that wins is whichever Shadow happens to merge first. Mark them `compileOnly` in the adapter's own build, or relocate them when shading.

## Cache Adapters

Cache adapters implement `CacheClientFactory`.

**Rule:** The client must manage any connection resources it creates. If it manages a connection pool, it must implement `AutoCloseable`. Hosts call `close()` during reconfiguration or shutdown.

```java
public class MyCacheClient implements ICacheClient, AutoCloseable {
    private final ConnectionPool pool;
    private final boolean ownsPool;

    public MyCacheClient(String host, int port) {
        this.pool = new ConnectionPool(host, port);
        this.ownsPool = true; // Factory-built, must clean up
    }

    public MyCacheClient(ConnectionPool pool) {
        this.pool = pool;
        this.ownsPool = false; // Caller-supplied, do not close
    }

    @Override
    public void close() {
        if (ownsPool) pool.close();
    }
}
```

## Auth Adapters

Auth adapters implement `TokenValidator`, and have no factory: a host uses at most one and does not ask anyone to choose
it, so `ServiceLoader` instantiates the validator directly.

**Rule:** Fail closed. Return `false` only for a credential you know to be bad — that is a `401`, and the caller is at
fault. When you cannot tell, because whatever vouches for the credential is unreachable or answered nonsense, *throw* —
that is a `500`, and the deployment is at fault. Never return `true` to paper over an outage; a wedged auth service must
not become an open door.

A host that discovers no validator serves every request. That is deliberate — it is what lets a build with no auth
service in front of it run — and hosts announce it loudly at startup rather than quietly.

## Packaging

When building a fat jar (e.g., using the Shadow plugin), `META-INF/services` files must be merged (e.g., using `mergeServiceFiles()`). Otherwise, only one adapter's registration will survive the packaging process. The `xo` build handles this automatically.

## Build

```bash
./gradlew :adapters:tugboat-easypost:build
./gradlew :adapters:tugboat-redis:build
```
Adapters are published as `com.uncommongoods.tugboat:tugboat-<name>-adapter`. Testing is typically handled through the `engine` test suite and the host applications.

## Licensing

All adapters are MIT-licensed. Private adapters can be created and linked into The Bridge and XO applications without violating their MPL 2.0 licenses (§3.3). Because the build discovers adapters on its own — from `adapters/` or from `libs/` — adding one requires no change to any tracked file, so nothing about a private adapter has to be published.
