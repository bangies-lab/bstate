package digital.bangie.bstate;

import java.time.Duration;
import java.util.Optional;
import java.util.function.Supplier;

public interface BStore<K, V> {
    String name();

    void put(K key, V value);

    void put(K key, V value, Duration ttl);

    Optional<V> get(K key);

    V getOrThrow(K key);

    V getOrThrow(K key, String message);

    boolean contains(K key);

    V remember(K key, Supplier<V> supplier);

    V remember(K key, Supplier<V> supplier, Duration ttl);

    void remove(K key);

    void clear();

    int size();

    StoreStats stats();
}
