package org.example.threads;

import java.util.concurrent.Executors;

/**
 * Provides an executor where each task runs on a new virtual thread.
 * Virtual threads are lightweight and allow high concurrency
 * with minimal memory use.
 */
public class VirtualExecutorProvider extends ExecutorProvider {

    /**
     * Creates a provider that uses a virtual thread per task executor.
     */
    public VirtualExecutorProvider() {
        super(Executors.newVirtualThreadPerTaskExecutor());
    }
}
