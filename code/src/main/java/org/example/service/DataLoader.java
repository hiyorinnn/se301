package org.example.service;

import org.example.loader.LoadService;
import org.example.error.AppException;

public class DataLoader {
    private final LoadService loadService;

    public DataLoader(LoadService loadService) {
        this.loadService = loadService;
    }

    public LoadService.LoadedData load(String usersPath, String dictPath, int numLoadTasks) throws AppException {
        try (var loadProvider = org.example.threads.ConfigurableExecutorProvider.fixedCpuPool(numLoadTasks)) {
            return loadService.load(usersPath, dictPath, loadProvider);
        }
    }
}
