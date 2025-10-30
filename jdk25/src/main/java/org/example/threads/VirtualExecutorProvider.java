package org.example.threads;

import java.util.concurrent.Executors;

/**
 *
 * This provider creates an executor where each task runs on a new virtual thread.
 * Virtual threads are lightweight and allow high concurrency with minimal memory overhead.
 *
 * Example usage — submitting individual tasks:
 * <pre>
 * try (VirtualExecutorProvider provider = new VirtualExecutorProvider()) {
 *     provider.get().submit(() -> {
 *         // task code here
 *         System.out.println("Running on a virtual thread!");
 *     });
 *     // You can submit multiple tasks here
 * }
 * </pre>
 *
 * Example usage — using a service that consumes the executor:
 * <pre>
 * LoadService.LoadedData data;
 * try (VirtualExecutorProvider provider = new VirtualExecutorProvider()) {
 *     data = loadService.load(usersPath, dictPath, provider);
 * }
 * // Now safe to use data.users() and data.dict() after the block
 * </pre>
 */
public class VirtualExecutorProvider extends ExecutorProvider {

    /**
     * Constructs a VirtualExecutorProvider using a virtual thread per task executor.
     */
    public VirtualExecutorProvider() {
        super(Executors.newVirtualThreadPerTaskExecutor());
    }
}
