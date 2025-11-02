package org.example.threads;

import java.util.concurrent.Executors;

/**
 * SingleThreadProvider is a concrete ExecutorProvider that provides
 * a single-threaded executor. Ideal for tasks that should run
 * sequentially in a dedicated thread.
 */
public class SingleThreadProvider extends ExecutorProvider {

    /**
     * Constructs a SingleThreadProvider with a single-thread executor.
     */
    public SingleThreadProvider() {
        super(Executors.newSingleThreadExecutor());
    }

    /**
     * Convenience method to submit a task to the single thread.
     *
     * @param task the Runnable task to execute
     */
    public void submitTask(Runnable task) {
        executor.submit(task);
    }
}

