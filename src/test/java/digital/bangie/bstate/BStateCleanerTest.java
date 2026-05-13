package digital.bangie.bstate;

import digital.bangie.bstate.entity.User;
import digital.bangie.bstate.enums.EvictionStrategy;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class BStateCleanerTest {

    @Test
    void shouldStartCleaner() {
        BState state = BState.inMemory();

        BStateCleaner cleaner = BStateCleaner.builder()
                .state(state)
                .interval(Duration.ofMillis(50))
                .build();

        cleaner.start();

        assertTrue(cleaner.isRunning());

        cleaner.stop();
    }

    @Test
    void shouldStopCleaner() {
        BState state = BState.inMemory();

        BStateCleaner cleaner = BStateCleaner.builder()
                .state(state)
                .interval(Duration.ofMillis(50))
                .build();

        cleaner.start();
        cleaner.stop();

        assertFalse(cleaner.isRunning());
    }

    @Test
    void shouldIgnoreStartWhenAlreadyRunning() {
        BState state = BState.inMemory();

        BStateCleaner cleaner = BStateCleaner.builder()
                .state(state)
                .interval(Duration.ofMillis(50))
                .build();

        cleaner.start();
        cleaner.start();

        assertTrue(cleaner.isRunning());

        cleaner.stop();
    }

    @Test
    void shouldIgnoreStopWhenNotRunning() {
        BState state = BState.inMemory();

        BStateCleaner cleaner = BStateCleaner.builder()
                .state(state)
                .interval(Duration.ofMillis(50))
                .build();

        assertDoesNotThrow(cleaner::stop);
        assertFalse(cleaner.isRunning());
    }

    @Test
    void shouldCleanupExpiredValuesInBackground() throws InterruptedException {
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

        User bangie = User.builder()
                .id(UUID.randomUUID())
                .name("Bangie")
                .age(36)
                .build();

        userStore.put(bangie.getId(), bangie);

        BStateCleaner cleaner = BStateCleaner.builder()
                .state(state)
                .interval(Duration.ofMillis(50))
                .build();

        cleaner.start();

        Thread.sleep(160);

        cleaner.stop();

        assertEquals(0, userStore.size());
        assertFalse(userStore.contains(bangie.getId()));
        assertEquals(1, userStore.stats().getExpiredRemovals());
    }

    @Test
    void shouldCloseCleaner() {
        BState state = BState.inMemory();

        BStateCleaner cleaner = BStateCleaner.builder()
                .state(state)
                .interval(Duration.ofMillis(50))
                .build();

        cleaner.start();
        cleaner.close();

        assertFalse(cleaner.isRunning());
    }

    @Test
    void shouldRejectNullState() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> BStateCleaner.builder().state(null)
        );

        assertEquals("State cannot be null", exception.getMessage());
    }

    @Test
    void shouldRejectNullInterval() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> BStateCleaner.builder().interval(null)
        );

        assertEquals("Interval cannot be null", exception.getMessage());
    }

    @Test
    void shouldRejectZeroInterval() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> BStateCleaner.builder().interval(Duration.ZERO)
        );

        assertEquals("Interval must be greater than zero", exception.getMessage());
    }

    @Test
    void shouldRejectNegativeInterval() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> BStateCleaner.builder().interval(Duration.ofMillis(-1))
        );

        assertEquals("Interval must be greater than zero", exception.getMessage());
    }

    @Test
    void shouldRejectBuildWithoutState() {
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> BStateCleaner.builder().build()
        );

        assertEquals("State is required", exception.getMessage());
    }
}
