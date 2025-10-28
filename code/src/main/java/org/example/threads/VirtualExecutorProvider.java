package org.example.threads;

import java.util.concurrent.Executors;

public class VirtualExecutorProvider extends ExecutorProvider {
    
    public VirtualExecutorProvider() {
        super(Executors.newVirtualThreadPerTaskExecutor());
    }
}