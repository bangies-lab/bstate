package digital.bangie.bstate;

import digital.bangie.bstate.entity.Product;
import digital.bangie.bstate.entity.User;
import digital.bangie.bstate.enums.EvictionStrategy;
import digital.bangie.bstate.persistence.BStatePersistence;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.Duration;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class BStatePersistenceTest {
    @TempDir
    Path tempDir;

    @Test
    void shouldSaveAndLoadState() {
        Path path = tempDir.resolve("bstate.snapshot");

        BState state = BState.inMemory();

        BStore<UUID, User> userStore = state.getOrRegisterStore(
                "users",
                UUID.class,
                User.class
        );

        User bangie = User.builder()
                .id(UUID.randomUUID())
                .name("Bangie")
                .age(36)
                .build();

        userStore.put(bangie.getId(), bangie);

        BStatePersistence.save(state, path);

        BState restored = BStatePersistence.load(path);

        BStore<UUID, User> restoredUsers = restored.getStore(
                "users",
                UUID.class,
                User.class
        ).orElseThrow();

        User restoredUser = restoredUsers.getOrThrow(bangie.getId());

        assertEquals(bangie.getId(), restoredUser.getId());
        assertEquals("Bangie", restoredUser.getName());
        assertEquals(36, restoredUser.getAge());
    }

    @Test
    void shouldSaveAndLoadMultipleStores() {
        Path path = tempDir.resolve("bstate.snapshot");

        BState state = BState.inMemory();

        BStore<UUID, User> userStore = state.getOrRegisterStore(
                "users",
                UUID.class,
                User.class
        );

        BStore<UUID, Product> productStore = state.getOrRegisterStore(
                "products",
                UUID.class,
                Product.class
        );

        User bangie = User.builder()
                .id(UUID.randomUUID())
                .name("Bangie")
                .age(36)
                .build();

        Product keyboard = Product.builder()
                .id(UUID.randomUUID())
                .name("Tipkalica")
                .build();

        userStore.put(bangie.getId(), bangie);
        productStore.put(keyboard.getId(), keyboard);

        BStatePersistence.save(state, path);

        BState restored = BStatePersistence.load(path);

        assertTrue(restored.hasStore("users"));
        assertTrue(restored.hasStore("products"));
        assertEquals(2, restored.storeCount());
    }

    @Test
    void shouldRestoreStoreOptions() {
        Path path = tempDir.resolve("bstate.snapshot");

        BState state = BState.inMemory();

        state.getOrRegisterStore(
                "users",
                UUID.class,
                User.class,
                StoreOptions.builder()
                        .ttl(Duration.ofMinutes(10))
                        .maxSize(5000)
                        .evictionStrategy(EvictionStrategy.LRU)
                        .build()
        );

        BStatePersistence.save(state, path);

        BState restored = BStatePersistence.load(path);

        assertTrue(restored.storeInfo("users").isPresent());
        assertEquals(Duration.ofMinutes(10), restored.storeInfo("users").get().getTtl());
        assertEquals(5000, restored.storeInfo("users").get().getMaxSize());
        assertEquals(EvictionStrategy.LRU, restored.storeInfo("users").get().getEvictionStrategy());
    }

    @Test
    void shouldNotRestoreExpiredValues() throws InterruptedException {
        Path path = tempDir.resolve("bstate.snapshot");

        BState state = BState.inMemory();

        BStore<UUID, User> userStore = state.getOrRegisterStore(
                "users",
                UUID.class,
                User.class,
                StoreOptions.builder()
                        .ttl(Duration.ofMillis(50))
                        .maxSize(100)
                        .evictionStrategy(EvictionStrategy.OLDEST)
                        .build()
        );

        User bangie = User.builder()
                .id(UUID.randomUUID())
                .name("Bangie")
                .age(36)
                .build();

        userStore.put(bangie.getId(), bangie);

        BStatePersistence.save(state, path);

        Thread.sleep(80);

        BState restored = BStatePersistence.load(path);

        BStore<UUID, User> restoredUsers = restored.getStore(
                "users",
                UUID.class,
                User.class
        ).orElseThrow();

        assertTrue(restoredUsers.get(bangie.getId()).isEmpty());
    }
}
