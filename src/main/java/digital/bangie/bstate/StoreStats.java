package digital.bangie.bstate;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

public class StoreStats {
    private final AtomicLong hits = new AtomicLong(0);
    private final AtomicLong misses = new AtomicLong(0);
    private final AtomicLong puts = new AtomicLong(0);
    private final AtomicLong removals = new AtomicLong(0);
    private final AtomicLong expiredRemovals = new AtomicLong(0);
    private final AtomicLong evictions = new AtomicLong(0);

    void recordHit() {
        hits.incrementAndGet();
    }

    void recordMiss() {
        misses.incrementAndGet();
    }

    void recordPut() {
        puts.incrementAndGet();
    }

    void recordRemoval() {
        removals.incrementAndGet();
    }

    void recordExpiredRemoval() {
        expiredRemovals.incrementAndGet();
    }

    void recordEviction() {
        evictions.incrementAndGet();
    }

    public long getHits() {
        return hits.get();
    }

    public long getMisses() {
        return misses.get();
    }

    public long getPuts() {
        return puts.get();
    }

    public long getRemovals() {
        return removals.get();
    }

    public long getExpiredRemovals() {
        return expiredRemovals.get();
    }

    public long getEvictions() {
        return evictions.get();
    }

    public StoreStatsSnapshot snapshot(int size) {
        return new StoreStatsSnapshot(
                getHits(),
                getMisses(),
                getPuts(),
                getRemovals(),
                getExpiredRemovals(),
                getEvictions(),
                size,
                Instant.now()
        );
    }
}
