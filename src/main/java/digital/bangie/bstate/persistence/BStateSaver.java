package digital.bangie.bstate.persistence;

import digital.bangie.bstate.BState;

import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BStateSaver implements AutoCloseable {
    private final BState state;
    private final Path path;
    private final Duration interval;
    private final boolean daemonThread;
    private ScheduledExecutorService executorService;
    private boolean running;

    private BStateSaver(Builder builder) {
        this.state = builder.state;
        this.path = builder.path;
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
            Thread thread = new Thread(runnable, "bstate-saver");
            thread.setDaemon(daemonThread);
            return thread;
        });

        executorService.scheduleAtFixedRate(
                this::saveSafely,
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

    private void saveSafely() {
        try {
            BStatePersistence.save(state, path);
        } catch (RuntimeException exception) {
            // Intentionally swallowed for now.
            // Saver must not die because one save cycle failed.
        }
    }

    public static class Builder {
        private BState state;
        private Path path;
        private Duration interval = Duration.ofMinutes(5);
        private boolean daemonThread = true;

        public Builder state(BState state) {
            if (state == null) {
                throw new IllegalArgumentException("State cannot be null");
            }

            this.state = state;
            return this;
        }

        public Builder path(Path path) {
            if (path == null) {
                throw new IllegalArgumentException("Path cannot be null");
            }

            this.path = path;
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

        public BStateSaver build() {
            if (state == null) {
                throw new IllegalStateException("State is required");
            }

            if (path == null) {
                throw new IllegalStateException("Path is required");
            }

            return new BStateSaver(this);
        }
    }
}
