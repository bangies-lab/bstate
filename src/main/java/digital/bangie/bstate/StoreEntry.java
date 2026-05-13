package digital.bangie.bstate;

import java.time.Instant;

class StoreEntry<V> {
    private final V value;
    private final Instant createdAt;
    private final Instant expiresAt;
    private final long createdOrder;
    private long accessOrder;

    StoreEntry(V value, Instant createdAt, Instant expiresAt, long createdOrder) {
        this.value = value;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.createdOrder = createdOrder;
        this.accessOrder = createdOrder;
    }

    V getValue() {
        return value;
    }

    Instant getCreatedAt() {
        return createdAt;
    }

    long getCreatedOrder() {
        return createdOrder;
    }

    long getAccessOrder() {
        return accessOrder;
    }

    void markAccessed(long accessOrder) {
        this.accessOrder = accessOrder;
    }

    boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }
}