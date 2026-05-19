package digital.bangie.bstate;

import java.time.Instant;

public class StoreStatsSnapshot {
    private final long hits;
    private final long misses;
    private final long puts;
    private final long removals;
    private final long expiredRemovals;
    private final long evictions;
    private final int size;
    private final Instant capturedAt;

    public StoreStatsSnapshot(
            long hits,
            long misses,
            long puts,
            long removals,
            long expiredRemovals,
            long evictions,
            int size,
            Instant capturedAt
    ) {
        this.hits = hits;
        this.misses = misses;
        this.puts = puts;
        this.removals = removals;
        this.expiredRemovals = expiredRemovals;
        this.evictions = evictions;
        this.size = size;
        this.capturedAt = capturedAt;
    }

    public long getHits() {
        return hits;
    }

    public long getMisses() {
        return misses;
    }

    public long getPuts() {
        return puts;
    }

    public long getRemovals() {
        return removals;
    }

    public long getExpiredRemovals() {
        return expiredRemovals;
    }

    public long getEvictions() {
        return evictions;
    }

    public int getSize() {
        return size;
    }

    public Instant getCapturedAt() {
        return capturedAt;
    }

    public long getReads() {
        return hits + misses;
    }

    public double getHitRate() {
        long reads = getReads();

        if (reads == 0) {
            return 0.0;
        }

        return (double) hits / reads;
    }

    public double getMissRate() {
        long reads = getReads();

        if (reads == 0) {
            return 0.0;
        }

        return (double) misses / reads;
    }
}
