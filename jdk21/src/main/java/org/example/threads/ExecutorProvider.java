package org.example.threads;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Abstract base class for managing an ExecutorService.
 * Provides a consistent interface for obtaining and closing executor services safely.
 * Intended to be extended by concrete implementations that decide
 * the type and configuration of the executor.
 * Implements AutoCloseable so it can be used in try-with-resources blocks,
 * automatically shutting down the executor when done.
 */
public abstract class ExecutorProvider implements AutoCloseable {

    private static final int SHUTDOWN_TIMEOUT_SECONDS = 5;

    /**
     * The underlying ExecutorService managed by this provider.
     */
    protected final ExecutorService executor;

    /**
     * Constructs a new ExecutorProvider with the given executor.
     *
     * @param executor the ExecutorService to manage
     */
    protected ExecutorProvider(ExecutorService executor) {
        this.executor = executor;
    }

    /**
     * Returns the underlying ExecutorService.
     *
     * @return the managed executor
     */
    public ExecutorService get() {
        return executor;
    }

    /**
     * Shuts down the executor gracefully.
     * First attempts an orderly shutdown using shutdown().
     * If tasks do not terminate within 5 seconds, it forces shutdown using shutdownNow().
     * If the current thread is interrupted while waiting, the thread's interrupt status is restored
     * and shutdownNow() is called.
     */
    @Override
    public void close() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            executor.shutdownNow();
        }
    }
}
