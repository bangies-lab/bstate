package digital.bangie.bstate;

import digital.bangie.bstate.enums.EvictionStrategy;

import java.time.Duration;

public class StoreInfo {
    private final String name;
    private final Class<?> keyType;
    private final Class<?> valueType;
    private final int size;
    private final Duration ttl;
    private final int maxSize;
    private final EvictionStrategy evictionStrategy;

    public StoreInfo(
            String name,
            Class<?> keyType,
            Class<?> valueType,
            int size,
            Duration ttl,
            int maxSize,
            EvictionStrategy evictionStrategy
    ) {
        this.name = name;
        this.keyType = keyType;
        this.valueType = valueType;
        this.size = size;
        this.ttl = ttl;
        this.maxSize = maxSize;
        this.evictionStrategy = evictionStrategy;
    }

    public String getName() {
        return name;
    }

    public Class<?> getKeyType() {
        return keyType;
    }

    public Class<?> getValueType() {
        return valueType;
    }

    public int getSize() {
        return size;
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
}
