package digital.bangie.bstate;

import digital.bangie.bstate.enums.EvictionStrategy;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

class DefaultBState implements BState {
    private final StoreOptions defaultOptions;
    private final ConcurrentHashMap<String, RegisteredStore<?, ?>> stores;

    DefaultBState(StoreOptions defaultOptions) {
        if (defaultOptions == null) {
            throw new IllegalArgumentException("Default options cannot be null");
        }

        this.defaultOptions = defaultOptions;
        this.stores = new ConcurrentHashMap<>();
    }

    @Override
    public <K, V> BStore<K, V> registerStore(
            String name,
            Class<K> keyType,
            Class<V> valueType
    ) {
        return registerStore(name, keyType, valueType, defaultOptions);
    }

    @Override
    public <K, V> BStore<K, V> registerStore(
            String name,
            Class<K> keyType,
            Class<V> valueType,
            StoreOptions options
    ) {
        validateStoreName(name);

        if (keyType == null) {
            throw new IllegalArgumentException("Key type cannot be null");
        }

        if (valueType == null) {
            throw new IllegalArgumentException("Value type cannot be null");
        }

        if (options == null) {
            throw new IllegalArgumentException("Store options cannot be null");
        }

        RegisteredStore<?, ?> existingStore = stores.get(name);

        if (existingStore != null) {
            if (!existingStore.matches(keyType, valueType)) {
                throw new IllegalStateException("Store already exists with different key or value type: " + name);
            }

            return castStore(existingStore.store());
        }

        BStore<K, V> store = new InMemoryBStore<>(name, keyType, valueType, options);
        RegisteredStore<K, V> registeredStore = new RegisteredStore<>(name, keyType, valueType, store);
        RegisteredStore<?, ?> previousStore = stores.putIfAbsent(name, registeredStore);

        if (previousStore != null) {
            if (!previousStore.matches(keyType, valueType)) {
                throw new IllegalStateException("Store already exists with different key or value type: " + name);
            }

            return castStore(previousStore.store());
        }

        return store;
    }

    @Override
    public <K, V> Optional<BStore<K, V>> getStore(
            String name,
            Class<K> keyType,
            Class<V> valueType
    ) {
        validateStoreName(name);

        if (keyType == null) {
            throw new IllegalArgumentException("Key type cannot be null");
        }

        if (valueType == null) {
            throw new IllegalArgumentException("Value type cannot be null");
        }

        RegisteredStore<?, ?> registeredStore = stores.get(name);

        if (registeredStore == null) {
            return Optional.empty();
        }

        if (!registeredStore.matches(keyType, valueType)) {
            return Optional.empty();
        }

        return Optional.of(castStore(registeredStore.store()));
    }

    @Override
    public <K, V> BStore<K, V> getOrRegisterStore(
            String name,
            Class<K> keyType,
            Class<V> valueType
    ) {
        return registerStore(name, keyType, valueType);
    }

    @Override
    public <K, V> BStore<K, V> getOrRegisterStore(
            String name,
            Class<K> keyType,
            Class<V> valueType,
            StoreOptions options
    ) {
        return registerStore(name, keyType, valueType, options);
    }

    @Override
    public int cleanupAll() {
        int removed = 0;

        for (RegisteredStore<?, ?> registeredStore : stores.values()) {
            removed += registeredStore.store().cleanupExpired();
        }

        return removed;
    }

    @Override
    public void removeStore(String name) {
        validateStoreName(name);
        stores.remove(name);
    }

    @Override
    public void clearStore(String name) {
        validateStoreName(name);

        RegisteredStore<?, ?> registeredStore = stores.get(name);

        if (registeredStore == null) {
            return;
        }

        registeredStore.store().clear();
    }

    @Override
    public void clearAll() {
        for (RegisteredStore<?, ?> registeredStore : stores.values()) {
            registeredStore.store().clear();
        }
    }

    @Override
    public Duration defaultTtl() {
        return defaultOptions.getTtl();
    }

    @Override
    public int defaultMaxSize() {
        return defaultOptions.getMaxSize();
    }

    @Override
    public EvictionStrategy defaultEvictionStrategy() {
        return defaultOptions.getEvictionStrategy();
    }

    private void validateStoreName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Store name cannot be blank");
        }
    }

    @SuppressWarnings("unchecked")
    private <K, V> BStore<K, V> castStore(BStore<?, ?> store) {
        return (BStore<K, V>) store;
    }

    private record RegisteredStore<K, V>(
            String name,
            Class<K> keyType,
            Class<V> valueType,
            BStore<K, V> store
    ) {
        boolean matches(Class<?> expectedKeyType, Class<?> expectedValueType) {
            if (!keyType.equals(expectedKeyType)) {
                return false;
            }

            return valueType.equals(expectedValueType);
        }
    }
}
