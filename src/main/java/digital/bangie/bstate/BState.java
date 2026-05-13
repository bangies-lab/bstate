package digital.bangie.bstate;

import digital.bangie.bstate.enums.EvictionStrategy;

import java.time.Duration;
import java.util.Optional;

public interface BState {
    static BStateBuilder builder() {
        return new BStateBuilder();
    }

    static BState create() {
        return builder().build();
    }

    static BState inMemory() {
        return builder().build();
    }

    <K, V> BStore<K, V> registerStore(
            String name,
            Class<K> keyType,
            Class<V> valueType
    );

    <K, V> BStore<K, V> registerStore(
            String name,
            Class<K> keyType,
            Class<V> valueType,
            StoreOptions options
    );

    <K, V> Optional<BStore<K, V>> getStore(
            String name,
            Class<K> keyType,
            Class<V> valueType
    );

    void removeStore(String name);

    void clearStore(String name);

    void clearAll();

    Duration defaultTtl();

    int defaultMaxSize();

    EvictionStrategy defaultEvictionStrategy();
}
