package digital.bangie.bstate;

import digital.bangie.bstate.enums.EvictionStrategy;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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

    Set<String> storeNames();

    int storeCount();

    boolean hasStore(String name);

    Optional<StoreInfo> storeInfo(String name);

    List<StoreInfo> storeInfos();

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

    <K, V> BStore<K, V> getOrRegisterStore(
            String name,
            Class<K> keyType,
            Class<V> valueType
    );

    <K, V> BStore<K, V> getOrRegisterStore(
            String name,
            Class<K> keyType,
            Class<V> valueType,
            StoreOptions options
    );

    int cleanupAll();

    void removeStore(String name);

    void clearStore(String name);

    void clearAll();

    Duration defaultTtl();

    int defaultMaxSize();

    EvictionStrategy defaultEvictionStrategy();
}
