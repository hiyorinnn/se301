package org.example.threads;

import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

public class ConfigurableExecutorProvider extends ExecutorProvider {
    
    public ConfigurableExecutorProvider(Supplier<ExecutorService> executorFactory) {
        super(executorFactory.get());
    }
}