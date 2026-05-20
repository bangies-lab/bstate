package digital.bangie.bstate.persistence;

import digital.bangie.bstate.BState;
import digital.bangie.bstate.BStore;
import digital.bangie.bstate.StoreOptions;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;

public final class BStatePersistence {
    private BStatePersistence() {
    }

    public static void save(BState state, Path path) {
        if (state == null) {
            throw new IllegalArgumentException("State cannot be null");
        }

        if (path == null) {
            throw new IllegalArgumentException("Path cannot be null");
        }

        Path parent = path.getParent();

        try {
            if (parent != null) {
                Files.createDirectories(parent);
            }

            try (OutputStream outputStream = Files.newOutputStream(path);
                 ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream)) {
                objectOutputStream.writeObject(state.snapshot());
            }
        } catch (IOException exception) {
            throw new BStatePersistenceException("Failed to save bState snapshot", exception);
        }
    }

    public static BState load(Path path) {
        BState state = BState.inMemory();
        restore(state, path);
        return state;
    }

    public static void restore(BState state, Path path) {
        if (state == null) {
            throw new IllegalArgumentException("State cannot be null");
        }

        BStateSnapshot snapshot = readSnapshot(path);

        for (BStoreSnapshot storeSnapshot : snapshot.getStores()) {
            restoreStore(state, storeSnapshot);
        }
    }

    private static BStateSnapshot readSnapshot(Path path) {
        if (path == null) {
            throw new IllegalArgumentException("Path cannot be null");
        }

        if (!Files.exists(path)) {
            throw new BStatePersistenceException("Snapshot file does not exist: " + path);
        }

        try (InputStream inputStream = Files.newInputStream(path);
             ObjectInputStream objectInputStream = new ObjectInputStream(inputStream)) {
            Object value = objectInputStream.readObject();

            if (!(value instanceof BStateSnapshot snapshot)) {
                throw new BStatePersistenceException("Invalid bState snapshot file");
            }

            return snapshot;
        } catch (IOException | ClassNotFoundException exception) {
            throw new BStatePersistenceException("Failed to load bState snapshot", exception);
        }
    }

    private static <K, V> void restoreStore(BState state, BStoreSnapshot storeSnapshot) {
        Class<K> keyType = loadClass(storeSnapshot.getKeyTypeName());
        Class<V> valueType = loadClass(storeSnapshot.getValueTypeName());

        StoreOptions options = StoreOptions.builder()
                .ttl(storeSnapshot.getTtl())
                .maxSize(storeSnapshot.getMaxSize())
                .evictionStrategy(storeSnapshot.getEvictionStrategy())
                .build();

        BStore<K, V> store = state.getOrRegisterStore(
                storeSnapshot.getName(),
                keyType,
                valueType,
                options
        );

        for (BStoreEntrySnapshot entrySnapshot : storeSnapshot.getEntries()) {
            if (!entrySnapshot.isExpired()) {
                K key = keyType.cast(entrySnapshot.getKey());
                V value = valueType.cast(entrySnapshot.getValue());

                Duration remainingTtl = Duration.between(
                        Instant.now(),
                        entrySnapshot.getExpiresAt()
                );

                if (!remainingTtl.isZero() && !remainingTtl.isNegative()) {
                    store.put(key, value, remainingTtl);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> Class<T> loadClass(String className) {
        try {
            return (Class<T>) Class.forName(className);
        } catch (ClassNotFoundException exception) {
            throw new BStatePersistenceException("Failed to load class: " + className, exception);
        }
    }
}
