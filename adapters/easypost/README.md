# EasyPost Adapter

*Note: This project is not affiliated with EasyPost.*

This module provides an implementation of the [`ports`](../../ports/README.md) interfaces for EasyPost by wrapping the official [`easypost-api-client`](https://github.com/EasyPost/easypost-java).

## Configuration

```java
public String type()             { return "easypost"; }
public List<String> configKeys() { return List.of("API Key"); }
```

| Environment | Configuration Method |
|---|---|
| **The Bridge** | Settings → Data → Clients: Add an entry, set type to `easypost`, and provide the **API Key**. (The default seeded client uses this type.) |
| **XO** | Provide `API_KEY` in the environment, or `TUGBOAT_EASYPOST_API_KEY` for the client that XO builds internally. |
| **Java** | `new ShippingClientAdapter(apiKey)` |

The factory's `create(...)` method will throw an `IllegalArgumentException` if the API key is missing or blank.

## Direct Instantiation

Host applications typically construct this client dynamically via the factory, but it can be instantiated directly for testing or custom integrations:

```java
import com.uncommongoods.tugboat.engine.ports.shipping.service.IShippingClient;
import com.uncommongoods.tugboat.engine.ports.shipping.service.ShippingClientAdapter;
import com.uncommongoods.tugboat.engine.config.EngineConfig;

IShippingClient client = new ShippingClientAdapter(System.getenv("EASYPOST_API_KEY"));

EngineConfig engineConfig = EngineConfig.builder()
  .withShippingClient(client) // Registered as "default"
  .withCacheClient(cacheClient)
  .withCachePrefix("TUGBOAT")
  .build();
```

If a configured EasyPost client is already managed by the application, it can be wrapped directly using `EngineConfig.Builder.withEasyPostClient(EasyPostClient)`.

## Build

```bash
./gradlew :adapters:tugboat-easypost:build
```

The adapter is published as `com.uncommongoods.tugboat:tugboat-easypost-adapter`. It declares `api` dependencies on both `:tugboat-ports` and `com.easypost:easypost-api-client:8.7.0`, meaning consumers inherit the EasyPost types transitively.
