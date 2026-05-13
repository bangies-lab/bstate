# bState

bState is a lightweight Java library for managing typed state stores.

It is designed for situations where you want a simple in-memory state/cache layer without being tied to Spring Boot, annotations, Redis, or any framework-specific magic.

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
cache.put("users:" + userId, user);
cache.put("products:" + productId, product);
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