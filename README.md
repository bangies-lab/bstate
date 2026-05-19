# bState

bState is a lightweight Java library for managing typed state stores.

It is designed for situations where you want a simple in-memory state/cache layer without being tied to Spring Boot,
annotations, Redis, or any framework-specific magic.

The core idea is simple:

```java
BState state = BState.inMemory();

BStore<UUID, User> userStore = state.registerStore(
        "users",
        UUID.class,
        User.class
);
```

Each store is separated by name, key type, value type, TTL, max size, and eviction strategy.

---

## Why bState?

Sometimes you do not want one giant cache bucket with string keys like:

```java
cache.put("users:"+userId, user);
cache.

put("products:"+productId, product);
```

Instead, bState lets you create typed stores:

```java
BStore<UUID, User> users = state.registerStore(
        "users",
        UUID.class,
        User.class
);

BStore<UUID, Product> products = state.registerStore(
        "products",
        UUID.class,
        Product.class
);
```

This gives you:

- typed keys
- typed values
- separated stores
- per-store TTL
- per-store max size
- per-store eviction strategy
- basic store statistics
- clean API without framework dependency

---

## Background cleaner

Expired values are removed when they are accessed through `get`, `contains`, or manual cleanup.

You can also run automatic cleanup in the background:

```java
BStateCleaner cleaner = BStateCleaner.builder()
        .state(state)
        .interval(Duration.ofMinutes(1))
        .build();

cleaner.start();
```

## Store metadata

bState can describe registered stores:

```java
Set<String> names = state.storeNames();

int count = state.storeCount();

boolean hasUsers = state.hasStore("users");

Optional<StoreInfo> info = state.storeInfo("users");

List<StoreInfo> infos = state.storeInfos();
```

## Stats snapshots

Each store exposes live stats:

```java
StoreStats stats = userStore.stats();
```

For a stable point-in-time view, use statsSnapshot():
```java
StoreStatsSnapshot snapshot = userStore.statsSnapshot();

long hits = snapshot.getHits();
long misses = snapshot.getMisses();
long reads = snapshot.getReads();

double hitRate = snapshot.getHitRate();
double missRate = snapshot.getMissRate();

int size = snapshot.getSize();
Instant capturedAt = snapshot.getCapturedAt();
```
StoreStatsSnapshot is immutable. Later store operations do not change an already created snapshot.