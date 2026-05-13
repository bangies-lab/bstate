package digital.bangie.bstate;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BStateCleaner implements AutoCloseable {
    private final BState state;
    private final Duration interval;
    private final boolean daemonThread;
    private ScheduledExecutorService executorService;
    private boolean running;

    private BStateCleaner(Builder builder) {
        this.state = builder.state;
        this.interval = builder.interval;
        this.daemonThread = builder.daemonThread;
        this.running = false;
    }

    public static Builder builder() {
        return new Builder();
    }

    public synchronized void start() {
        if (running) {
            return;
        }

        executorService = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "bstate-cleaner");
            thread.setDaemon(daemonThread);
            return thread;
        });

        executorService.scheduleAtFixedRate(
                this::cleanupSafely,
                interval.toMillis(),
                interval.toMillis(),
                TimeUnit.MILLISECONDS
        );

        running = true;
    }

    public synchronized void stop() {
        if (!running) {
            return;
        }

        executorService.shutdownNow();
        executorService = null;
        running = false;
    }

    public synchronized boolean isRunning() {
        return running;
    }

    @Override
    public void close() {
        stop();
    }

    private void cleanupSafely() {
        try {
            state.cleanupAll();
        } catch (RuntimeException exception) {
            // Intentionally swallowed for now.
            // Cleaner must not die because one cleanup cycle failed.
        }
    }

    public static class Builder {
        private BState state;
        private Duration interval = Duration.ofMinutes(1);
        private boolean daemonThread = true;

        public Builder state(BState state) {
            if (state == null) {
                throw new IllegalArgumentException("State cannot be null");
            }

            this.state = state;
            return this;
        }

        public Builder interval(Duration interval) {
            if (interval == null) {
                throw new IllegalArgumentException("Interval cannot be null");
            }

            if (interval.isZero() || interval.isNegative()) {
                throw new IllegalArgumentException("Interval must be greater than zero");
            }

            this.interval = interval;
            return this;
        }

        public Builder daemonThread(boolean daemonThread) {
            this.daemonThread = daemonThread;
            return this;
        }

        public BStateCleaner build() {
            if (state == null) {
                throw new IllegalStateException("State is required");
            }

            return new BStateCleaner(this);
        }
    }
}
