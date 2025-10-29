package org.example.loader;

import org.example.model.User;
import org.example.error.AppException;
import org.example.threads.ExecutorProvider;

import java.util.List;
import java.util.concurrent.*;

public class LoadService {
    private final Loader<User> userLoader;
    private final Loader<String> dictLoader;

    public LoadService(Loader<User> userLoader, Loader<String> dictLoader) {
        this.userLoader = userLoader;
        this.dictLoader = dictLoader;
    }

    public LoadedData load(String usersPath, String dictPath, ExecutorProvider provider) throws AppException {
        try (provider) {
            ExecutorService exec = provider.get();
            Future<List<User>> usersFuture = exec.submit(() -> userLoader.load(usersPath));
            Future<List<String>> dictFuture  = exec.submit(() -> dictLoader.load(dictPath));

            return new LoadedData(usersFuture.get(), dictFuture.get());
        } catch (ExecutionException e) {
            throw new AppException("Failed during concurrent file loading", e.getCause());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AppException("Loading interrupted", e);
        }
    }

    public record LoadedData(List<User> users, List<String> dict) {}
}