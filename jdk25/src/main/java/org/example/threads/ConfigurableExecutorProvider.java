package org.example.threads;

import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

public class ConfigurableExecutorProvider extends ExecutorProvider {

    /**
     * @param executorFactory: a supplier that creates the ExecutorService
     */
    public ConfigurableExecutorProvider(Supplier<ExecutorService> executorFactory) {
        super(executorFactory.get());
    }

    /**
     * Convenience method: fixed thread pool using available CPUs
     */
    public static ConfigurableExecutorProvider fixedCpuPool() {
        int cores = Runtime.getRuntime().availableProcessors();
        return new ConfigurableExecutorProvider(() -> java.util.concurrent.Executors.newFixedThreadPool(cores));
    }

    /**
     * Convenience method: fixed thread pool with the given number of threads
     */
    public static ConfigurableExecutorProvider fixedCpuPool(int cores) {
        return new ConfigurableExecutorProvider(() -> java.util.concurrent.Executors.newFixedThreadPool(cores));
    }

    /**
     * Convenience method: cached thread pool
     */
    public static ConfigurableExecutorProvider cachedPool() {
        return new ConfigurableExecutorProvider(() -> java.util.concurrent.Executors.newCachedThreadPool());
    }
}
