package digital.bangie.bstate;

import digital.bangie.bstate.enums.EvictionStrategy;

import java.time.Duration;

public class StoreOptions {
    private final Duration ttl;
    private final int maxSize;
    private final EvictionStrategy evictionStrategy;

    private StoreOptions(Builder builder) {
        this.ttl = builder.ttl;
        this.maxSize = builder.maxSize;
        this.evictionStrategy = builder.evictionStrategy;
    }

    public static StoreOptions defaults() {
        return builder().build();
    }

    public static Builder builder() {
        return new Builder();
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

    public static class Builder {
        private Duration ttl = Duration.ofMinutes(5);
        private int maxSize = 1000;
        private EvictionStrategy evictionStrategy = EvictionStrategy.OLDEST;

        public Builder ttl(Duration ttl) {
            if (ttl == null) {
                throw new IllegalArgumentException("TTL cannot be null");
            }

            if (ttl.isZero() || ttl.isNegative()) {
                throw new IllegalArgumentException("TTL must be greater than zero");
            }

            this.ttl = ttl;
            return this;
        }

        public Builder maxSize(int maxSize) {
            if (maxSize <= 0) {
                throw new IllegalArgumentException("Max size must be greater than zero");
            }

            this.maxSize = maxSize;
            return this;
        }

        public Builder evictionStrategy(EvictionStrategy evictionStrategy) {
            if (evictionStrategy == null) {
                throw new IllegalArgumentException("Eviction strategy cannot be null");
            }

            this.evictionStrategy = evictionStrategy;
            return this;
        }

        public StoreOptions build() {
            return new StoreOptions(this);
        }
    }
}
