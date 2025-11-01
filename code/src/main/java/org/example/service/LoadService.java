package org.example.service;

import org.example.loader.Loading;
import org.example.error.AppException;
import org.example.threads.ConfigurableExecutorProvider;

/* Thin wrapper that runs the Loading implementation using
   a configurable fixed-size executor and returns loaded data. */
public class LoadService {
    private final Loading loadService;

    public LoadService(Loading loadService) {
        this.loadService = loadService;
    }

    /* Load users and dictionary concurrently using a small thread pool.
       Returns loaded data and ensures the executor is properly closed. */
    public Loading.LoadedData load(String usersPath, String dictPath, int numLoadTasks) throws AppException {
        try (var loadProvider = ConfigurableExecutorProvider.fixedCpuPool(numLoadTasks)) {
            return loadService.load(usersPath, dictPath, loadProvider);
        }
    }
}
