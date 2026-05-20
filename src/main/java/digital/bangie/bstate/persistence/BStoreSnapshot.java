package digital.bangie.bstate.persistence;

import digital.bangie.bstate.enums.EvictionStrategy;

import java.io.Serializable;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BStoreSnapshot implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String name;
    private final String keyTypeName;
    private final String valueTypeName;
    private final Duration ttl;
    private final int maxSize;
    private final EvictionStrategy evictionStrategy;
    private final List<BStoreEntrySnapshot> entries;

    public BStoreSnapshot(
            String name,
            String keyTypeName,
            String valueTypeName,
            Duration ttl,
            int maxSize,
            EvictionStrategy evictionStrategy,
            List<BStoreEntrySnapshot> entries
    ) {
        this.name = name;
        this.keyTypeName = keyTypeName;
        this.valueTypeName = valueTypeName;
        this.ttl = ttl;
        this.maxSize = maxSize;
        this.evictionStrategy = evictionStrategy;
        this.entries = new ArrayList<>(entries);
    }

    public String getName() {
        return name;
    }

    public String getKeyTypeName() {
        return keyTypeName;
    }

    public String getValueTypeName() {
        return valueTypeName;
    }

    public Duration getTtl() {
        return ttl;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public EvictionStrategy getEvictionStrategy() {
        return evictionStrategy;
    }

    public List<BStoreEntrySnapshot> getEntries() {
        return Collections.unmodifiableList(entries);
    }
}
