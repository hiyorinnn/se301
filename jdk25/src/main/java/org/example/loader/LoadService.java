package org.example.loader;

import org.example.model.User;
import org.example.error.AppException;
import org.example.threads.ExecutorProvider;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;

public class LoadService {
    private final Loader<User> userLoader;
    private final Loader<String> dictLoader;

    public LoadService(Loader<User> userLoader, Loader<String> dictLoader) {
        this.userLoader = userLoader;
        this.dictLoader = dictLoader;
    }

    /**
     * Load users and dictionary concurrently using the provided ExecutorProvider.
     *
     * @param usersPath path to user file
     * @param dictPath path to dictionary file
     * @param provider executor provider (can be CPU pool, cached pool, or virtual threads)
     * @return LoadedData containing users and dictionary
     * @throws AppException if loading fails or is interrupted
     */
    public LoadedData load(String usersPath, String dictPath, ExecutorProvider provider) throws AppException {
        ExecutorService exec = provider.get();

        try {
            CompletableFuture<Set<User>> usersFuture = CompletableFuture.supplyAsync(() -> {
                try { return userLoader.load(usersPath); } 
                catch (AppException e) { throw new CompletionException(e); }
            }, exec);

            CompletableFuture<Set<String>> dictFuture = CompletableFuture.supplyAsync(() -> {
                try { return dictLoader.load(dictPath); } 
                catch (AppException e) { throw new CompletionException(e); }
            }, exec);

            CompletableFuture.allOf(usersFuture, dictFuture).join();

            return new LoadedData(usersFuture.join(), dictFuture.join());
        } catch (CompletionException e) {
            throw new AppException("Failed during concurrent file loading", e.getCause());
        }
    }

    /**
     * Simple data record for loaded users and dictionary
     */
    public record LoadedData(Set<User> users, Set<String> dict) {}
}
