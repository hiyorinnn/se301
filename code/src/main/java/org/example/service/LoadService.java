package org.example.service;

import org.example.loader.Loading;
import org.example.error.AppException;
import org.example.threads.ConfigurableExecutorProvider;

public class LoadService {
    private final Loading loadService;

    public LoadService(Loading loadService) {
        this.loadService = loadService;
    }

    public Loading.LoadedData load(String usersPath, String dictPath, int numLoadTasks) throws AppException {
        try (var loadProvider = ConfigurableExecutorProvider.fixedCpuPool(numLoadTasks)) {
            return loadService.load(usersPath, dictPath, loadProvider);
        }
    }
}
