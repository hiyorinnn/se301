package org.example.loader;

import org.example.model.User;
import org.example.error.AppException;
import org.example.threads.ExecutorProvider;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;

/* Loads users and dictionary concurrently using a provided executor. */
public class Loading {
    private final Loader<User> userLoader;
    private final Loader<String> dictLoader;

    public Loading(Loader<User> userLoader, Loader<String> dictLoader) {
        this.userLoader = userLoader;
        this.dictLoader = dictLoader;
    }

    /* Load users and dictionary in parallel and return a data record. */
    public LoadedData load(String usersPath, String dictPath, ExecutorProvider provider) throws AppException {
        ExecutorService exec = provider.get();

        try {
            CompletableFuture<Set<User>> usersFuture = supplyAsyncWithAppException(() -> userLoader.load(usersPath), exec);
            CompletableFuture<Set<String>> dictFuture = supplyAsyncWithAppException(() -> dictLoader.load(dictPath), exec);

            Set<User> users = usersFuture.join();
            Set<String> dict = dictFuture.join();

            return new LoadedData(users, dict);
        } catch (CompletionException e) {
            throw new AppException("Failed during concurrent file loading", e.getCause());
        }
    }

    /* Wrap a Loader task that may throw AppException into a CompletableFuture. */
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

    /* Simple holder for loaded users and dictionary sets. */
    public record LoadedData(Set<User> users, Set<String> dict) {}
}

