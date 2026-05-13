package digital.bangie.bstate;

import digital.bangie.bstate.entity.Product;
import digital.bangie.bstate.entity.User;
import digital.bangie.bstate.enums.EvictionStrategy;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class BStateTest {

    @Test
    void shouldUseStoreLikeRepositoryCache() {
        BState state = BState.inMemory();

        BStore<UUID, User> userStore = state.registerStore(
                "users",
                UUID.class,
                User.class
        );

        User bangie = User.builder()
                .name("Bangie")
                .age(36)
                .build();

        bangie = saveUser(bangie);

        userStore.put(bangie.getId(), bangie);

        assertTrue(userStore.contains(bangie.getId()));
        assertEquals(bangie, userStore.getOrThrow(bangie.getId()));
        assertEquals(Optional.of(bangie), userStore.get(bangie.getId()));
        assertEquals(1, userStore.size());
    }

    @Test
    void shouldReplaceExistingValueWhenPutIsCalledAgain() {
        BState state = BState.inMemory();

        BStore<UUID, User> userStore = state.registerStore(
                "users",
                UUID.class,
                User.class
        );

        UUID userId = UUID.randomUUID();

        User firstVersion = User.builder()
                .id(userId)
                .name("Bangie")
                .age(36)
                .build();

        User secondVersion = User.builder()
                .id(userId)
                .name("Bangie")
                .age(37)
                .build();

        userStore.put(userId, firstVersion);
        userStore.put(userId, secondVersion);

        User cachedUser = userStore.getOrThrow(userId);

        assertEquals("Bangie", cachedUser.getName());
        assertEquals(37, cachedUser.getAge());
        assertEquals(1, userStore.size());
        assertEquals(2, userStore.stats().getPuts());
    }

    @Test
    void shouldReturnOptionalValueWhenKeyExists() {
        BState state = BState.inMemory();

        BStore<UUID, User> userStore = state.registerStore(
                "users",
                UUID.class,
                User.class
        );

        User bangie = saveUser(User.builder()
                .name("Bangie")
                .age(36)
                .build());

        userStore.put(bangie.getId(), bangie);

        Optional<User> result = userStore.get(bangie.getId());

        assertTrue(result.isPresent());
        assertEquals("Bangie", result.get().getName());
        assertEquals(36, result.get().getAge());
        assertEquals(1, userStore.stats().getHits());
    }

    @Test
    void shouldReturnEmptyOptionalWhenKeyDoesNotExist() {
        BState state = BState.inMemory();

        BStore<UUID, User> userStore = state.registerStore(
                "users",
                UUID.class,
                User.class
        );

        Optional<User> result = userStore.get(UUID.randomUUID());

        assertTrue(result.isEmpty());
        assertEquals(1, userStore.stats().getMisses());
    }

    @Test
    void shouldReturnTrueWhenStoreContainsKey() {
        BState state = BState.inMemory();

        BStore<UUID, User> userStore = state.registerStore(
                "users",
                UUID.class,
                User.class
        );

        User bangie = saveUser(User.builder()
                .name("Bangie")
                .age(36)
                .build());

        userStore.put(bangie.getId(), bangie);

        assertTrue(userStore.contains(bangie.getId()));
    }

    @Test
    void shouldReturnFalseWhenStoreDoesNotContainKey() {
        BState state = BState.inMemory();

        BStore<UUID, User> userStore = state.registerStore(
                "users",
                UUID.class,
                User.class
        );

        assertFalse(userStore.contains(UUID.randomUUID()));
    }

    @Test
    void shouldGetValueOrThrowWhenValueExists() {
        BState state = BState.inMemory();

        BStore<UUID, User> userStore = state.registerStore(
                "users",
                UUID.class,
                User.class
        );

        User bangie = saveUser(User.builder()
                .name("Bangie")
                .age(36)
                .build());

        userStore.put(bangie.getId(), bangie);

        User result = userStore.getOrThrow(bangie.getId());

        assertEquals(bangie, result);
    }

    @Test
    void shouldThrowDefaultExceptionWhenValueDoesNotExist() {
        BState state = BState.inMemory();

        BStore<UUID, User> userStore = state.registerStore(
                "users",
                UUID.class,
                User.class
        );

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> userStore.getOrThrow(UUID.randomUUID())
        );

        assertEquals("Value not found in store users", exception.getMessage());
    }

    @Test
    void shouldThrowCustomMessageWhenValueDoesNotExist() {
        BState state = BState.inMemory();

        BStore<UUID, User> userStore = state.registerStore(
                "users",
                UUID.class,
                User.class
        );

        UUID userId = UUID.randomUUID();

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> userStore.getOrThrow(userId, "User not found: " + userId)
        );

        assertEquals("User not found: " + userId, exception.getMessage());
    }

    @Test
    void shouldUseDefaultExceptionMessageWhenCustomMessageIsNull() {
        BState state = BState.inMemory();

        BStore<UUID, User> userStore = state.registerStore(
                "users",
                UUID.class,
                User.class
        );

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> userStore.getOrThrow(UUID.randomUUID(), null)
        );

        assertEquals("Value not found in store users", exception.getMessage());
    }

    @Test
    void shouldUseDefaultExceptionMessageWhenCustomMessageIsBlank() {
        BState state = BState.inMemory();

        BStore<UUID, User> userStore = state.registerStore(
                "users",
                UUID.class,
                User.class
        );

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> userStore.getOrThrow(UUID.randomUUID(), "   ")
        );

        assertEquals("Value not found in store users", exception.getMessage());
    }

    @Test
    void shouldRememberValueWhenMissing() {
        BState state = BState.inMemory();

        BStore<UUID, User> userStore = state.registerStore(
                "users",
                UUID.class,
                User.class
        );

        UUID userId = UUID.randomUUID();

        User result = userStore.remember(
                userId,
                () -> User.builder()
                        .id(userId)
                        .name("Bangie")
                        .age(36)
                        .build()
        );

        assertEquals(userId, result.getId());
        assertEquals("Bangie", result.getName());
        assertEquals(36, result.getAge());
        assertEquals(Optional.of(result), userStore.get(userId));
    }

    @Test
    void shouldNotCallRememberSupplierWhenValueAlreadyExists() {
        BState state = BState.inMemory();

        BStore<UUID, User> userStore = state.registerStore(
                "users",
                UUID.class,
                User.class
        );

        AtomicInteger supplierCalls = new AtomicInteger(0);

        User bangie = saveUser(User.builder()
                .name("Bangie")
                .age(36)
                .build());

        userStore.put(bangie.getId(), bangie);

        User result = userStore.remember(
                bangie.getId(),
                () -> {
                    supplierCalls.incrementAndGet();
                    return User.builder()
                            .id(bangie.getId())
                            .name("Fallback")
                            .age(99)
                            .build();
                }
        );

        assertEquals(bangie, result);
        assertEquals(0, supplierCalls.get());
    }

    @Test
    void shouldRemoveValue() {
        BState state = BState.inMemory();

        BStore<UUID, User> userStore = state.registerStore(
                "users",
                UUID.class,
                User.class
        );

        User bangie = saveUser(User.builder()
                .name("Bangie")
                .age(36)
                .build());

        userStore.put(bangie.getId(), bangie);
        userStore.remove(bangie.getId());

        assertFalse(userStore.contains(bangie.getId()));
        assertTrue(userStore.get(bangie.getId()).isEmpty());
        assertEquals(1, userStore.stats().getRemovals());
    }

    @Test
    void shouldClearStore() {
        BState state = BState.inMemory();

        BStore<UUID, User> userStore = state.registerStore(
                "users",
                UUID.class,
                User.class
        );

        User bangie = saveUser(User.builder()
                .name("Bangie")
                .age(36)
                .build());

        User gizmo = saveUser(User.builder()
                .name("Gizmo")
                .age(1)
                .build());

        userStore.put(bangie.getId(), bangie);
        userStore.put(gizmo.getId(), gizmo);

        userStore.clear();

        assertEquals(0, userStore.size());
        assertFalse(userStore.contains(bangie.getId()));
        assertFalse(userStore.contains(gizmo.getId()));
    }

    @Test
    void shouldExpireValueAfterTtl() throws InterruptedException {
        BState state = BState.inMemory();

        BStore<UUID, User> userStore = state.registerStore(
                "users",
                UUID.class,
                User.class,
                StoreOptions.builder()
                        .ttl(Duration.ofMillis(50))
                        .maxSize(100)
                        .evictionStrategy(EvictionStrategy.OLDEST)
                        .build()
        );

        User bangie = saveUser(User.builder()
                .name("Bangie")
                .age(36)
                .build());

        userStore.put(bangie.getId(), bangie);

        Thread.sleep(80);

        assertFalse(userStore.contains(bangie.getId()));
        assertTrue(userStore.get(bangie.getId()).isEmpty());
        assertEquals(1, userStore.stats().getExpiredRemovals());
    }

    @Test
    void shouldEvictOldestValueWhenMaxSizeIsReached() {
        BState state = BState.inMemory();

        BStore<UUID, User> userStore = state.registerStore(
                "users",
                UUID.class,
                User.class,
                StoreOptions.builder()
                        .ttl(Duration.ofMinutes(5))
                        .maxSize(2)
                        .evictionStrategy(EvictionStrategy.OLDEST)
                        .build()
        );

        User first = saveUser(User.builder()
                .name("First")
                .age(1)
                .build());

        User second = saveUser(User.builder()
                .name("Second")
                .age(2)
                .build());

        User third = saveUser(User.builder()
                .name("Third")
                .age(3)
                .build());

        userStore.put(first.getId(), first);
        userStore.put(second.getId(), second);
        userStore.put(third.getId(), third);

        assertFalse(userStore.contains(first.getId()));
        assertEquals(second, userStore.getOrThrow(second.getId()));
        assertEquals(third, userStore.getOrThrow(third.getId()));
        assertEquals(2, userStore.size());
        assertEquals(1, userStore.stats().getEvictions());
    }

    @Test
    void shouldEvictLeastRecentlyUsedValueWhenMaxSizeIsReached() {
        BState state = BState.inMemory();

        BStore<UUID, User> userStore = state.registerStore(
                "users",
                UUID.class,
                User.class,
                StoreOptions.builder()
                        .ttl(Duration.ofMinutes(5))
                        .maxSize(2)
                        .evictionStrategy(EvictionStrategy.LRU)
                        .build()
        );

        User first = saveUser(User.builder()
                .name("First")
                .age(1)
                .build());

        User second = saveUser(User.builder()
                .name("Second")
                .age(2)
                .build());

        User third = saveUser(User.builder()
                .name("Third")
                .age(3)
                .build());

        userStore.put(first.getId(), first);
        userStore.put(second.getId(), second);

        userStore.get(first.getId());

        userStore.put(third.getId(), third);

        assertEquals(first, userStore.getOrThrow(first.getId()));
        assertFalse(userStore.contains(second.getId()));
        assertEquals(third, userStore.getOrThrow(third.getId()));
        assertEquals(2, userStore.size());
        assertEquals(1, userStore.stats().getEvictions());
    }

    @Test
    void shouldReturnSameStoreWhenRegisteringSameStoreAgain() {
        BState state = BState.inMemory();

        BStore<UUID, User> first = state.registerStore(
                "users",
                UUID.class,
                User.class
        );

        BStore<UUID, User> second = state.registerStore(
                "users",
                UUID.class,
                User.class
        );

        assertSame(first, second);
    }

    @Test
    void shouldRejectSameStoreNameWithDifferentTypes() {
        BState state = BState.inMemory();

        state.registerStore(
                "users",
                UUID.class,
                User.class
        );

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> state.registerStore("users", String.class, User.class)
        );

        assertTrue(exception.getMessage().contains("different key or value type"));
    }

    @Test
    void shouldGetRegisteredStoreByNameAndTypes() {
        BState state = BState.inMemory();

        BStore<UUID, User> users = state.registerStore(
                "users",
                UUID.class,
                User.class
        );

        Optional<BStore<UUID, User>> result = state.getStore(
                "users",
                UUID.class,
                User.class
        );

        assertTrue(result.isPresent());
        assertSame(users, result.get());
    }

    @Test
    void shouldReturnEmptyWhenGettingStoreWithWrongTypes() {
        BState state = BState.inMemory();

        state.registerStore(
                "users",
                UUID.class,
                User.class
        );

        Optional<BStore<String, User>> result = state.getStore(
                "users",
                String.class,
                User.class
        );

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldUseDefaultOptionsFromBuilder() {
        BState state = BState.builder()
                .defaultTtl(Duration.ofSeconds(10))
                .defaultMaxSize(50)
                .defaultEvictionStrategy(EvictionStrategy.LRU)
                .build();

        assertEquals(Duration.ofSeconds(10), state.defaultTtl());
        assertEquals(50, state.defaultMaxSize());
        assertEquals(EvictionStrategy.LRU, state.defaultEvictionStrategy());
    }

    @Test
    void shouldClearStoreFromStateManager() {
        BState state = BState.inMemory();

        BStore<UUID, User> userStore = state.registerStore(
                "users",
                UUID.class,
                User.class
        );

        User bangie = saveUser(User.builder()
                .name("Bangie")
                .age(36)
                .build());

        userStore.put(bangie.getId(), bangie);

        state.clearStore("users");

        assertEquals(0, userStore.size());
        assertFalse(userStore.contains(bangie.getId()));
    }

    @Test
    void shouldClearAllStoresFromStateManager() {
        BState state = BState.inMemory();

        BStore<UUID, User> userStore = state.registerStore(
                "users",
                UUID.class,
                User.class
        );

        BStore<UUID, Product> productStore = state.registerStore(
                "products",
                UUID.class,
                Product.class
        );

        User bangie = saveUser(User.builder()
                .name("Bangie")
                .age(36)
                .build());

        Product keyboard = Product.builder()
                .id(UUID.randomUUID())
                .name("Tipkalica")
                .build();

        userStore.put(bangie.getId(), bangie);
        productStore.put(keyboard.getId(), keyboard);

        state.clearAll();

        assertEquals(0, userStore.size());
        assertEquals(0, productStore.size());
    }

    @Test
    void shouldRemoveStoreFromStateManager() {
        BState state = BState.inMemory();

        state.registerStore(
                "users",
                UUID.class,
                User.class
        );

        state.removeStore("users");

        Optional<BStore<UUID, User>> result = state.getStore(
                "users",
                UUID.class,
                User.class
        );

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldIgnoreRemoveStoreWhenStoreDoesNotExist() {
        BState state = BState.inMemory();

        assertDoesNotThrow(() -> state.removeStore("users"));
    }

    @Test
    void shouldNotEvictValuesWhenEvictionStrategyIsNone() {
        BState state = BState.inMemory();

        BStore<UUID, User> userStore = state.registerStore(
                "users",
                UUID.class,
                User.class,
                StoreOptions.builder()
                        .ttl(Duration.ofMinutes(5))
                        .maxSize(2)
                        .evictionStrategy(EvictionStrategy.NONE)
                        .build()
        );

        User first = saveUser(User.builder()
                .name("First")
                .age(1)
                .build());

        User second = saveUser(User.builder()
                .name("Second")
                .age(2)
                .build());

        User third = saveUser(User.builder()
                .name("Third")
                .age(3)
                .build());

        userStore.put(first.getId(), first);
        userStore.put(second.getId(), second);
        userStore.put(third.getId(), third);

        assertEquals(first, userStore.getOrThrow(first.getId()));
        assertEquals(second, userStore.getOrThrow(second.getId()));
        assertEquals(third, userStore.getOrThrow(third.getId()));
        assertEquals(3, userStore.size());
        assertEquals(0, userStore.stats().getEvictions());
    }

    @Test
    void shouldNotRecordHitWhenContainsFindsValue() {
        BState state = BState.inMemory();

        BStore<UUID, User> userStore = state.registerStore(
                "users",
                UUID.class,
                User.class
        );

        User bangie = saveUser(User.builder()
                .name("Bangie")
                .age(36)
                .build());

        userStore.put(bangie.getId(), bangie);

        assertTrue(userStore.contains(bangie.getId()));
        assertEquals(0, userStore.stats().getHits());
    }

    @Test
    void shouldNotRecordMissWhenContainsDoesNotFindValue() {
        BState state = BState.inMemory();

        BStore<UUID, User> userStore = state.registerStore(
                "users",
                UUID.class,
                User.class
        );

        assertFalse(userStore.contains(UUID.randomUUID()));
        assertEquals(0, userStore.stats().getMisses());
    }

    @Test
    void shouldRegisterStoreWhenGetOrRegisterStoreDoesNotExist() {
        BState state = BState.inMemory();

        BStore<UUID, User> userStore = state.getOrRegisterStore(
                "users",
                UUID.class,
                User.class
        );

        User bangie = saveUser(User.builder()
                .name("Bangie")
                .age(36)
                .build());

        userStore.put(bangie.getId(), bangie);

        assertEquals(bangie, userStore.getOrThrow(bangie.getId()));
    }

    @Test
    void shouldReturnExistingStoreWhenGetOrRegisterStoreAlreadyExists() {
        BState state = BState.inMemory();

        BStore<UUID, User> first = state.registerStore(
                "users",
                UUID.class,
                User.class
        );

        BStore<UUID, User> second = state.getOrRegisterStore(
                "users",
                UUID.class,
                User.class
        );

        assertSame(first, second);
    }

    @Test
    void shouldRejectGetOrRegisterStoreWhenStoreExistsWithDifferentTypes() {
        BState state = BState.inMemory();

        state.registerStore(
                "users",
                UUID.class,
                User.class
        );

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> state.getOrRegisterStore("users", String.class, User.class)
        );

        assertTrue(exception.getMessage().contains("different key or value type"));
    }

    @Test
    void shouldRegisterStoreWithOptionsWhenUsingGetOrRegisterStore() {
        BState state = BState.inMemory();

        BStore<UUID, User> userStore = state.getOrRegisterStore(
                "users",
                UUID.class,
                User.class,
                StoreOptions.builder()
                        .ttl(Duration.ofMinutes(5))
                        .maxSize(2)
                        .evictionStrategy(EvictionStrategy.NONE)
                        .build()
        );

        User first = saveUser(User.builder()
                .name("First")
                .age(1)
                .build());

        User second = saveUser(User.builder()
                .name("Second")
                .age(2)
                .build());

        User third = saveUser(User.builder()
                .name("Third")
                .age(3)
                .build());

        userStore.put(first.getId(), first);
        userStore.put(second.getId(), second);
        userStore.put(third.getId(), third);

        assertEquals(3, userStore.size());
        assertEquals(0, userStore.stats().getEvictions());
    }

    @Test
    void shouldCleanupExpiredValuesFromStore() throws InterruptedException {
        BState state = BState.inMemory();

        BStore<UUID, User> userStore = state.registerStore(
                "users",
                UUID.class,
                User.class,
                StoreOptions.builder()
                        .ttl(Duration.ofMillis(50))
                        .maxSize(100)
                        .evictionStrategy(EvictionStrategy.OLDEST)
                        .build()
        );

        User bangie = saveUser(User.builder()
                .name("Bangie")
                .age(36)
                .build());

        userStore.put(bangie.getId(), bangie);

        Thread.sleep(80);

        int removed = userStore.cleanupExpired();

        assertEquals(1, removed);
        assertEquals(0, userStore.size());
        assertEquals(1, userStore.stats().getExpiredRemovals());
    }

    @Test
    void shouldNotCleanupValuesThatAreNotExpired() {
        BState state = BState.inMemory();

        BStore<UUID, User> userStore = state.registerStore(
                "users",
                UUID.class,
                User.class,
                StoreOptions.builder()
                        .ttl(Duration.ofMinutes(5))
                        .maxSize(100)
                        .evictionStrategy(EvictionStrategy.OLDEST)
                        .build()
        );

        User bangie = saveUser(User.builder()
                .name("Bangie")
                .age(36)
                .build());

        userStore.put(bangie.getId(), bangie);

        int removed = userStore.cleanupExpired();

        assertEquals(0, removed);
        assertEquals(1, userStore.size());
        assertTrue(userStore.contains(bangie.getId()));
    }

    @Test
    void shouldCleanupExpiredValuesFromAllStores() throws InterruptedException {
        BState state = BState.inMemory();

        StoreOptions options = StoreOptions.builder()
                .ttl(Duration.ofMillis(50))
                .maxSize(100)
                .evictionStrategy(EvictionStrategy.OLDEST)
                .build();

        BStore<UUID, User> userStore = state.registerStore(
                "users",
                UUID.class,
                User.class,
                options
        );

        BStore<UUID, Product> productStore = state.registerStore(
                "products",
                UUID.class,
                Product.class,
                options
        );

        User bangie = saveUser(User.builder()
                .name("Bangie")
                .age(36)
                .build());

        Product keyboard = Product.builder()
                .id(UUID.randomUUID())
                .name("Tipkalica")
                .build();

        userStore.put(bangie.getId(), bangie);
        productStore.put(keyboard.getId(), keyboard);

        Thread.sleep(80);

        int removed = state.cleanupAll();

        assertEquals(2, removed);
        assertEquals(0, userStore.size());
        assertEquals(0, productStore.size());
    }

    private User saveUser(User user) {
        if (user.getId() != null) {
            return user;
        }

        return User.builder()
                .id(UUID.randomUUID())
                .name(user.getName())
                .age(user.getAge())
                .build();
    }
}