package digital.bangie.bstate;

public class StoreStats {
    private long hits;
    private long misses;
    private long puts;
    private long removals;
    private long expiredRemovals;
    private long evictions;

    public void recordHit() {
        hits++;
    }

    public void recordMiss() {
        misses++;
    }

    public void recordPut() {
        puts++;
    }

    public void recordRemoval() {
        removals++;
    }

    public void recordExpiredRemoval() {
        expiredRemovals++;
    }

    public void recordEviction() {
        evictions++;
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
}
