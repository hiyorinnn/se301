package org.example.loader;

import org.example.model.User;
import org.example.error.AppException;
import org.example.threads.ExecutorProvider;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;

public class Loading {
    private final Loader<User> userLoader;
    private final Loader<String> dictLoader;

    public Loading(Loader<User> userLoader, Loader<String> dictLoader) {
        this.userLoader = userLoader;
        this.dictLoader = dictLoader;
    }

    /**
     * Load users and dictionary concurrently using the provided ExecutorProvider.
     *
     * @param usersPath path to user file
     * @param dictPath path to dictionary file
     * @param provider executor provider (managed outside)
     * @return LoadedData containing users and dictionary
     * @throws AppException if loading fails
     */
    public LoadedData load(String usersPath, String dictPath, ExecutorProvider provider) throws AppException {
        ExecutorService exec = provider.get();

        try {
            CompletableFuture<Set<User>> usersFuture = supplyAsyncWithAppException(() -> userLoader.load(usersPath), exec);
            CompletableFuture<Set<String>> dictFuture = supplyAsyncWithAppException(() -> dictLoader.load(dictPath), exec);

            // Join both futures and return result
            Set<User> users = usersFuture.join();
            Set<String> dict = dictFuture.join();

            return new LoadedData(users, dict);
        } catch (CompletionException e) {
            throw new AppException("Failed during concurrent file loading", e.getCause());
        }
    }

    /**
     * Utility method to wrap AppException into CompletionException for CompletableFuture
     */
    private <T> CompletableFuture<T> supplyAsyncWithAppException(LoaderTask<T> task, ExecutorService exec) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return task.load();
            } catch (AppException e) {
                throw new CompletionException(e);
            }
        }, exec);
    }

    @FunctionalInterface
    private interface LoaderTask<T> {
        T load() throws AppException;
    }

    /**
     * Simple data record for loaded users and dictionary
     */
    public record LoadedData(Set<User> users, Set<String> dict) {}
}
