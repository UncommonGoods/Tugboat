# Redis Adapter

Implements [`ICacheClient`](../../ports/README.md) over Redis using [Jedis](https://github.com/redis/jedis). This adapter provides the engine with its resumable behavior: Tugboat state and the cargo lock are stored here.

**License:** MIT. **Gradle project:** `:adapters:tugboat-redis`.

## Components

The module contains two primary classes:

- **`JedisRedisAdapter`** — An `ICacheClient` over a `JedisPool`. Every method borrows a connection, executes one command, and returns the connection.
- **`JedisRedisClientFactory`** — The `CacheClientFactory` that host applications discover through `ServiceLoader`.

## Configuration

```java
public String type()             { return "redis"; }
public List<String> configKeys() { return List.of("Cache URL", "Cache Port"); }
```

| Environment | Configuration Method |
|---|---|
| **The Bridge** | Settings → Data: **Cache URL** and **Cache Port** appear as fixed rows, generated dynamically from `configKeys()` |
| **XO** | `CACHE_URL` and `CACHE_PORT` environment variables |
| **Java** | `new JedisRedisAdapter(host, port)` |

`Cache Port` defaults to `6379` if left blank. A blank `Cache URL` triggers an error from the factory; however, hosts treat "no cache configured at all" as a valid state and skip building a client entirely rather than failing to start. The `CACHE_TYPE=redis` property explicitly selects this adapter if multiple cache adapters exist on the classpath.

## Connection Ownership

The adapter provides two constructors, which dictate whether the underlying pool is closed when the adapter is released:

```java
// Owns its pool. Calling close() shuts the pool down. This is the constructor used by the factory.
ICacheClient client = new JedisRedisAdapter("localhost", 6379);

// Wraps an externally owned pool. Calling close() leaves it open, and the caller remains responsible for it.
JedisPool myPool = new JedisPool(new JedisPoolConfig(), "localhost", 6379);
ICacheClient client = new JedisRedisAdapter(myPool);
```

`JedisRedisAdapter` implements `AutoCloseable`. Hosts release cache clients using an `instanceof AutoCloseable` check, ensuring that The Bridge can rebuild its configuration on every save or PROD/DEV switch without leaking connection pools.

Custom cache adapters holding connections should adhere to the same two-constructor pattern (see [`adapters/README.md`](../README.md)).

## Running Redis Locally

`docker compose up` starts a `redis:7-alpine` container alongside the XO service. To start it independently:

```bash
docker run --rm -p 6379:6379 redis:7-alpine
```

The Bridge can then be pointed at `localhost` / `6379`, or XO configured via `CACHE_URL=localhost`. To inspect what the engine is writing:

```bash
redis-cli --scan --pattern 'TUGBOAT*'
redis-cli get 'TUGBOAT:TBCARGO:SHIP-1001'
```

The key layout is documented in [`engine/README.md`](../../engine/README.md).

## Running Without a Cache

Running without a cache client is supported, but the following capabilities are disabled:

- **Persistence between calls:** Every operation begins at `INITIAL`, meaning multi-step workflows must execute within a single process.
- **The cargo lock:** No mechanism exists to serialize multiple workers operating on the same shipment.
- **Pickup facilities and manifests:** These are inherently cache-backed, and their constructors will reject initialization without a cache.

Standard rating, buying, and printing operations remain fully functional.

## Build

```bash
./gradlew :adapters:tugboat-redis:build
```

Published as `com.uncommongoods.tugboat:tugboat-redis-adapter`. Depends on `:tugboat-ports` and `redis.clients:jedis:6.0.0` (exposed as an `api` dependency, allowing consumers to construct their own `JedisPool`).

The `engine` test suite exercises this adapter against a real Redis container (see [`engine/README.md`](../../engine/README.md)).


> Tugboat is not affiliated with Redis
