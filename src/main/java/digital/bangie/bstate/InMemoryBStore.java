package digital.bangie.bstate;

import digital.bangie.bstate.enums.EvictionStrategy;
import digital.bangie.bstate.persistence.BStoreEntrySnapshot;
import digital.bangie.bstate.persistence.BStoreSnapshot;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

class InMemoryBStore<K, V> implements BStore<K, V> {
    private final String name;
    private final Class<K> keyType;
    private final Class<V> valueType;
    private final StoreOptions options;
    private final ConcurrentHashMap<K, StoreEntry<V>> entries;
    private final StoreStats stats;
    private final AtomicLong orderCounter;

    InMemoryBStore(String name, Class<K> keyType, Class<V> valueType, StoreOptions options) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Store name cannot be blank");
        }

        if (keyType == null) {
            throw new IllegalArgumentException("Key type cannot be null");
        }

        if (valueType == null) {
            throw new IllegalArgumentException("Value type cannot be null");
        }

        if (options == null) {
            throw new IllegalArgumentException("Store options cannot be null");
        }

        this.name = name;
        this.keyType = keyType;
        this.valueType = valueType;
        this.options = options;
        this.entries = new ConcurrentHashMap<>();
        this.stats = new StoreStats();
        this.orderCounter = new AtomicLong(0);
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Class<K> keyType() {
        return keyType;
    }

    @Override
    public Class<V> valueType() {
        return valueType;
    }

    @Override
    public StoreOptions options() {
        return options;
    }

    @Override
    public void put(K key, V value) {
        put(key, value, options.getTtl());
    }

    @Override
    public void put(K key, V value, Duration ttl) {
        validateKey(key);
        validateValue(value);
        validateTtl(ttl);

        evictIfNeeded();

        Instant now = Instant.now();
        Instant expiresAt = now.plus(ttl);

        long order = orderCounter.incrementAndGet();
        entries.put(key, new StoreEntry<>(value, now, expiresAt, order));
        stats.recordPut();
    }

    @Override
    public Optional<V> get(K key) {
        validateKey(key);

        StoreEntry<V> entry = entries.get(key);

        if (entry == null) {
            stats.recordMiss();
            return Optional.empty();
        }

        if (entry.isExpired()) {
            entries.remove(key);
            stats.recordExpiredRemoval();
            stats.recordMiss();
            return Optional.empty();
        }

        entry.markAccessed(orderCounter.incrementAndGet());
        stats.recordHit();

        return Optional.of(entry.getValue());
    }

    @Override
    public V getOrThrow(K key) {
        return get(key).orElseThrow(() -> new IllegalStateException(defaultNotFoundMessage()));
    }

    @Override
    public V getOrThrow(K key, String message) {
        String returnMessage = message;
        if (returnMessage == null || returnMessage.isBlank()) {
            returnMessage = defaultNotFoundMessage();
        }
        String finalMessage = returnMessage;
        return get(key).orElseThrow(() -> new IllegalStateException(finalMessage));
    }

    @Override
    public boolean contains(K key) {
        validateKey(key);

        StoreEntry<V> entry = entries.get(key);

        if (entry == null) {
            return false;
        }

        if (entry.isExpired()) {
            entries.remove(key);
            stats.recordExpiredRemoval();
            return false;
        }

        return true;
    }

    @Override
    public V remember(K key, Supplier<V> supplier) {
        return remember(key, supplier, options.getTtl());
    }

    @Override
    public V remember(K key, Supplier<V> supplier, Duration ttl) {
        validateKey(key);
        validateSupplier(supplier);
        validateTtl(ttl);

        Optional<V> cachedValue = get(key);

        if (cachedValue.isPresent()) {
            return cachedValue.get();
        }

        V value = supplier.get();
        put(key, value, ttl);

        return value;
    }

    @Override
    public int cleanupExpired() {
        int removed = 0;

        for (Map.Entry<K, StoreEntry<V>> entry : entries.entrySet()) {
            if (entry.getValue().isExpired()) {
                boolean didRemove = entries.remove(entry.getKey(), entry.getValue());

                if (didRemove) {
                    removed++;
                    stats.recordExpiredRemoval();
                }
            }
        }

        return removed;
    }

    @Override
    public void remove(K key) {
        validateKey(key);

        StoreEntry<V> removed = entries.remove(key);

        if (removed != null) {
            stats.recordRemoval();
        }
    }

    @Override
    public void clear() {
        entries.clear();
    }

    @Override
    public int size() {
        return entries.size();
    }

    @Override
    public StoreStats stats() {
        return stats;
    }

    @Override
    public StoreStatsSnapshot statsSnapshot() {
        return stats.snapshot(size());
    }

    @Override
    public BStoreSnapshot snapshot() {
        List<BStoreEntrySnapshot> snapshots = new ArrayList<>();

        for (Map.Entry<K, StoreEntry<V>> entry : entries.entrySet()) {
            StoreEntry<V> storeEntry = entry.getValue();

            if (!storeEntry.isExpired()) {
                snapshots.add(new BStoreEntrySnapshot(
                        entry.getKey(),
                        storeEntry.getValue(),
                        storeEntry.getCreatedAt(),
                        storeEntry.getExpiresAt()
                ));
            }
        }

        return new BStoreSnapshot(
                name,
                keyType.getName(),
                valueType.getName(),
                options.getTtl(),
                options.getMaxSize(),
                options.getEvictionStrategy(),
                snapshots
        );
    }

    private void validateKey(K key) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }

        if (!keyType.isInstance(key)) {
            throw new IllegalArgumentException("Invalid key type for store " + name);
        }
    }

    private void validateValue(V value) {
        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null");
        }

        if (!valueType.isInstance(value)) {
            throw new IllegalArgumentException("Invalid value type for store " + name);
        }
    }

    private void validateTtl(Duration ttl) {
        if (ttl == null) {
            throw new IllegalArgumentException("TTL cannot be null");
        }

        if (ttl.isZero() || ttl.isNegative()) {
            throw new IllegalArgumentException("TTL must be greater than zero");
        }
    }

    private void validateSupplier(Supplier<V> supplier) {
        if (supplier == null) {
            throw new IllegalArgumentException("Supplier cannot be null");
        }
    }

    private void evictIfNeeded() {
        if (options.getEvictionStrategy() == EvictionStrategy.NONE) {
            return;
        }

        if (entries.size() < options.getMaxSize()) {
            return;
        }

        if (options.getEvictionStrategy() == EvictionStrategy.OLDEST) {
            evictOldest();
            return;
        }

        if (options.getEvictionStrategy() == EvictionStrategy.LRU) {
            evictLeastRecentlyUsed();
            return;
        }
    }

    private void evictOldest() {
        Optional<K> keyForRemoval = entries.entrySet()
                .stream()
                .min(Comparator.comparingLong(entry -> entry.getValue().getCreatedOrder()))
                .map(Map.Entry::getKey);

        if (keyForRemoval.isPresent()) {
            entries.remove(keyForRemoval.get());
            stats.recordEviction();
        }
    }

    private void evictLeastRecentlyUsed() {
        Optional<K> keyForRemoval = entries.entrySet()
                .stream()
                .min(Comparator.comparingLong(entry -> entry.getValue().getAccessOrder()))
                .map(Map.Entry::getKey);

        if (keyForRemoval.isPresent()) {
            entries.remove(keyForRemoval.get());
            stats.recordEviction();
        }
    }

    private String defaultNotFoundMessage() {
        return "Value not found in store " + name;
    }
}
