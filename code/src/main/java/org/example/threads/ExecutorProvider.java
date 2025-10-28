package org.example.threads;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public abstract class ExecutorProvider implements AutoCloseable {
    
    protected final ExecutorService executor;

    protected ExecutorProvider(ExecutorService executor) {
        this.executor = executor;
    }

    public ExecutorService get() {
        return executor;
    }

    @Override
    public void close() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            executor.shutdownNow();
        }
    }
}