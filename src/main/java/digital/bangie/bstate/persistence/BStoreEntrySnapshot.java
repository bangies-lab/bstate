package digital.bangie.bstate.persistence;

import java.io.Serializable;
import java.time.Instant;

public class BStoreEntrySnapshot implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Object key;
    private final Object value;
    private final Instant createdAt;
    private final Instant expiresAt;

    public BStoreEntrySnapshot(Object key, Object value, Instant createdAt, Instant expiresAt) {
        this.key = key;
        this.value = value;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    public Object getKey() {
        return key;
    }

    public Object getValue() {
        return value;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }
}
