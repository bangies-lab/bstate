package digital.bangie.bstate;

import digital.bangie.bstate.enums.EvictionStrategy;

import java.time.Duration;

public class BStateBuilder {
    private Duration defaultTtl = Duration.ofMinutes(5);
    private int defaultMaxSize = 1000;
    private EvictionStrategy defaultEvictionStrategy = EvictionStrategy.OLDEST;

    public BStateBuilder defaultTtl(Duration defaultTtl) {
        if (defaultTtl == null) {
            throw new IllegalArgumentException("Default TTL cannot be null");
        }

        if (defaultTtl.isZero() || defaultTtl.isNegative()) {
            throw new IllegalArgumentException("Default TTL must be greater than zero");
        }

        this.defaultTtl = defaultTtl;
        return this;
    }

    public BStateBuilder defaultMaxSize(int defaultMaxSize) {
        if (defaultMaxSize <= 0) {
            throw new IllegalArgumentException("Default max size must be greater than zero");
        }

        this.defaultMaxSize = defaultMaxSize;
        return this;
    }

    public BStateBuilder defaultEvictionStrategy(EvictionStrategy defaultEvictionStrategy) {
        if (defaultEvictionStrategy == null) {
            throw new IllegalArgumentException("Default eviction strategy cannot be null");
        }

        this.defaultEvictionStrategy = defaultEvictionStrategy;
        return this;
    }

    public BState build() {
        StoreOptions defaultOptions = StoreOptions.builder()
                .ttl(defaultTtl)
                .maxSize(defaultMaxSize)
                .evictionStrategy(defaultEvictionStrategy)
                .build();

        return new DefaultBState(defaultOptions);
    }
}
