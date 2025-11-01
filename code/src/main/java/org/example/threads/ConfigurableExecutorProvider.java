package org.example.threads;

import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;
import java.util.concurrent.Executors;

/**
 * Provides configurable {@link ExecutorService} instances.
 * Supports common executor configurations like fixed or cached thread pools.
 */
public class ConfigurableExecutorProvider extends ExecutorProvider {

    /**
     * Creates a provider using a supplied {@link ExecutorService} factory.
     *
     * @param executorFactory a supplier that creates the executor service
     */
    public ConfigurableExecutorProvider(Supplier<ExecutorService> executorFactory) {
        super(executorFactory.get());
    }

    /**
     * Creates a provider with a fixed thread pool
     * using the number of available CPU cores.
     *
     * @return a new {@code ConfigurableExecutorProvider}
     */
    public static ConfigurableExecutorProvider fixedCpuPool() {
        int cores = Runtime.getRuntime().availableProcessors();
        return new ConfigurableExecutorProvider(
                () -> Executors.newFixedThreadPool(cores)
        );
    }

    /**
     * Creates a provider with a fixed thread pool of the given size.
     *
     * @param cores number of threads in the pool
     * @return a new {@code ConfigurableExecutorProvider}
     */
    public static ConfigurableExecutorProvider fixedCpuPool(int cores) {
        return new ConfigurableExecutorProvider(
                () -> Executors.newFixedThreadPool(cores)
        );
    }

    /**
     * Creates a provider with a cached thread pool.
     *
     * @return a new {@code ConfigurableExecutorProvider}
     */
    public static ConfigurableExecutorProvider cachedPool() {
        return new ConfigurableExecutorProvider(
                () -> Executors.newCachedThreadPool()
        );
    }
}
