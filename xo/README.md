# Tugboat XO 🫡

XO = Executive Officer

The server-side host: an HTTP service and an SQS worker over the same
[engine](../engine/README.md). Use it to drive shipments from your own systems —
an order management system, a WMS, a scheduled job — instead of from a person at
a pack bench.

This module is essentially a trivial reference implementation of a service built on the Engine.

Micronaut 4.9.2 on Jetty, Java 21.

**License:** Mozilla Public License 2.0 (`LICENSE` in this directory). If you
modify these files and distribute the result, those files must be published. See [LICENSING.md](../LICENSING.md).

## Why the queue matters

Every endpoint that advances a shipment takes `?async=true`. With it, XO drops a
message on SQS and returns immediately; a consumer in the same application picks
it up and runs the same operation.

That is useful because the engine caches after every step. Work done ahead of
time is simply *there* later:

```
02:00  POST /tugboat/SHIP-1001/rate?async=true    → queued, returns at once
02:00  ...consumer rates the shipment, engine caches RATED state
09:15  POST /tugboat/SHIP-1001/purchase           → starts from RATED, buys, prints
```

Rate and shop your whole morning's orders overnight and the packer's request does
one API call instead of four. Nothing is held in memory between the two — the
cached state is the handoff.

`POST /batch/purchase` is always asynchronous: hand it a list of shipment ids and
it enqueues one message each.

## Endpoints

Base path is `${tugboat.api.path}`, default `/tugboat`.

| Method | Path | `?async` | Notes |
|---|---|---|---|
| `GET` | `/{shipmentId}` | — | current cached state as JSON |
| `POST` | `/{shipmentId}/initialize` | yes | |
| `POST` | `/{shipmentId}/rate` | yes | |
| `POST` | `/{shipmentId}/re-rate` | yes | resets first if the shipment is at `SHOPPED` or earlier, then rates again |
| `POST` | `/{shipmentId}/shop` | yes | |
| `POST` | `/{shipmentId}/purchase` | yes | |
| `POST` | `/batch/purchase` | always | body: JSON array of shipment ids |
| `GET` | `/{shipmentId}/print` | — | returns labels |
| `GET` | `/{shipmentId}/re-print` | — | |
| `POST` | `/{shipmentId}/void` | yes | |
| `POST` | `/tracker/{trackingCode}/{carrier}` | — | creates a tracker |
| `GET` | `/return-label/{returnId}/{orderId}/{shipmentId}` | — | needs a pickup facility |
| `GET` | `/pickup-facility/{code}` | — | the facility and its manifest groups |
| `GET` | `/pickup-group/{code}/{groupName}` | — | one manifest group |

Because operations walk the state machine forward from wherever a shipment is,
you can call `purchase` on a fresh id and it will initialize, rate, shop, and buy
in one request.

## Authentication

There is none. XO ships with no authentication of its own — every endpoint is
open to anyone who can reach the port. Run it on a trusted network, or behind an
API gateway or reverse proxy that authenticates for you.

## Configuration

All of it is environment variables. There is no config file to edit — the
per-environment YAML files this service used to carry were removed, along with
its dependency on AWS Secrets Manager.

### Core

| Variable | Default | Purpose |
|---|---|---|
| `SERVER_PORT` | `8080` | HTTP port |
| `TUGBOAT_QUEUE` | — | SQS queue name for async work |
| `TUGBOAT_CACHE_PREFIX` | — | namespaces every cache key |

### Cache

Read by the cache adapter itself, not by Micronaut config:

| Variable | Default | Purpose |
|---|---|---|
| `CACHE_URL` | — | cache host. **Unset means no cache** — see below |
| `CACHE_PORT` | `6379` | cache port |
| `CACHE_TYPE` | first found | which cache adapter, if more than one is on the classpath |

### Carriers

| Variable | Used by |
|---|---|
| `TUGBOAT_EASYPOST_API_KEY` | [easypost adapter](../adapters/easypost/README.md) |

EasyPost is registered as the `default` client. Every other shipping client is
discovered at runtime via `ServiceLoader`, so the variables it needs depend on
which adapters are on the classpath. XO publishes its configuration under
type-namespaced names (`esw API Key` → `ESW_API_KEY`), and a client is only
registered when at least one of its keys is set — leave them blank to disable it.

Vendor adapters that are not tracked in this repository read the following when
present:

| Variable | Default | Used by |
|---|---|---|
| `TUGBOAT_PARSEL_API_KEY` | — | parsel adapter |
| `TUGBOAT_ESW_API_KEY` | — | esw adapter |
| `TUGBOAT_ESW_SECURITY_URL` | ESW production security host | esw adapter |
| `TUGBOAT_ESW_PACKAGE_URL` | ESW production package host | esw adapter |
| `TUGBOAT_ESW_BRAND_CODE` | — | esw adapter; deployment-specific |
| `TUGBOAT_ESW_GRANT_TYPE` | `client_credentials` | esw adapter |
| `TUGBOAT_ESW_CLIENT_ID` | — | esw adapter; deployment-specific |

### SQS

Credentials, region, and endpoint all resolve through the standard AWS chain, so
there is nothing Tugboat-specific to configure:

| Variable | Purpose |
|---|---|
| `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY` | or use a profile or IAM role |
| `AWS_REGION` | |
| `AWS_ENDPOINT_URL_SQS` | point SQS at LocalStack or a self-hosted equivalent |
| `MICRONAUT_JMS_SQS_ENABLED=false` | run with no queue at all — synchronous endpoints only |

### Anything your hooks need

Set it as an environment variable and read it by name:

```java
String url = TugboatSettings.get("MY_SERVICE_URL");
```

XO publishes nothing, so lookups fall through to the real environment. The same
hook code works in the [Bridge](../bridge/README.md), where the value is a row on
the Settings page. See [`ports/README.md`](../ports/README.md).

### Running without a cache

If `CACHE_URL` is unset, XO logs that it is running without a cache and starts
anyway. You lose state between requests — **every request begins at `INITIAL`**,
so async pre-work is pointless and the multi-step flow only works within a single
call. Pickup facilities and manifests are unavailable, and
`/pickup-facility/{code}` returns 503.

## Run it

### Local stack

The repo ships a `docker compose` setup with LocalStack for SQS and a Redis, so
nothing external is needed:

```bash
docker compose up --build
```

It brings up:

- **localstack** on 4566, with the `tugboat-queue` created on startup
- **redis** on 6379
- **tugboat-xo** on 8080, pointed at both

The XO container waits on a LocalStack health check that only passes once the
queue exists. This matters: the JMS listener resolves the queue URL at boot, and
without the gate XO would start, fail to register the listener, log a clean
startup, and sit there as a dead consumer.

Non-secret config lives in `docker-compose.yml`; API keys go in `.env.dev`, which
is gitignored.

### From Gradle

```bash
./gradlew :tugboat-xo:run
```

### As a jar

```bash
./gradlew :tugboat-xo:build
java -jar xo/build/libs/tugboat-xo-all-optimized.jar
```

The jar name has no version in it on purpose, so deployment scripts and
Dockerfiles do not need editing on every release. It is the Micronaut AOT
optimized build (service loading, class loading, and Netty precomputed at build
time). **Building it does not require Docker** — only the `engine` test suite and
the compose stack do.

## Try it

Against the local stack:

```bash
curl http://localhost:8080/tugboat/pickup-facility/BK

# Queue a rate, return immediately
curl -X POST 'http://localhost:8080/tugboat/SHIP-1001/rate?async=true'
# → task rate has been enqueued for shipment SHIP-1001

# Later: read what the worker cached
curl http://localhost:8080/tugboat/SHIP-1001

# Buy several at once (always async)
curl -X POST -H 'Content-Type: application/json' \
  -d '["SHIP-1001","SHIP-1002","SHIP-1003"]' \
  http://localhost:8080/tugboat/batch/purchase
```

A shipment id only becomes a real shipment once something fills in its addresses
and parcels — that is your initial hook's job. See
[`hooks/README.md`](../hooks/README.md).

## Building the image

```bash
docker build -t tugboat-xo .
```

The `Dockerfile` at the repo root builds the whole project and copies the
optimized jar into a `temurin:21-jre-alpine` image.

## Adding a carrier

Write an adapter and add one line to `xo/build.gradle.kts`; XO builds it from
environment variables with no code change here. See
[`adapters/README.md`](../adapters/README.md).

If you package your own fat jar, merge `META-INF/services` files — several
adapters register factories under the same filename, and losing that merge means
`ServiceLoader` finds only one of them. The Shadow configuration in this module
already does it.
