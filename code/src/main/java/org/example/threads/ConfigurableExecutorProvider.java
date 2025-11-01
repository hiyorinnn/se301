package org.example.threads;

import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;
import java.util.concurrent.Executors;

/* Lightweight provider of ExecutorService instances.
   Offers common pool configurations (fixed by CPU, fixed by size, cached). */
public class ConfigurableExecutorProvider extends ExecutorProvider {

    /* Create a provider from a supplier that builds the executor. */
    public ConfigurableExecutorProvider(Supplier<ExecutorService> executorFactory) {
        super(executorFactory.get());
    }

    /* Fixed thread pool sized to available CPU cores. */
    public static ConfigurableExecutorProvider fixedCpuPool() {
        int cores = Runtime.getRuntime().availableProcessors();
        return new ConfigurableExecutorProvider(
                () -> Executors.newFixedThreadPool(cores)
        );
    }

    /* Fixed thread pool with the given number of threads. */
    public static ConfigurableExecutorProvider fixedCpuPool(int cores) {
        return new ConfigurableExecutorProvider(
                () -> Executors.newFixedThreadPool(cores)
        );
    }

    /* Cached thread pool for many short-lived tasks. */
    public static ConfigurableExecutorProvider cachedPool() {
        return new ConfigurableExecutorProvider(
                () -> Executors.newCachedThreadPool()
        );
    }
}
