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

// package org.example.loader;

// import org.example.model.User;
// import org.example.error.AppException;
// import org.example.threads.ExecutorProvider;

// import java.util.List;
// import java.util.concurrent.CompletableFuture;
// import java.util.concurrent.ExecutorService;

// public class LoadService {
//     private final Loader<User> userLoader;
//     private final Loader<String> dictLoader;

//     public LoadService(Loader<User> userLoader, Loader<String> dictLoader) {
//         this.userLoader = userLoader;
//         this.dictLoader = dictLoader;
//     }

//     /**
//      * Load users and dictionary concurrently using the provided ExecutorProvider.
//      *
//      * @param usersPath path to user file
//      * @param dictPath path to dictionary file
//      * @param provider executor provider (can be CPU pool, cached pool, or virtual threads)
//      * @return LoadedData containing users and dictionary
//      * @throws AppException if loading fails or is interrupted
//      */
//     public LoadedData load(String usersPath, String dictPath, ExecutorProvider provider) throws AppException {
//         ExecutorService exec = provider.get();

//         try {
//             // Run both loaders asynchronously
//             CompletableFuture<List<User>> usersFuture = CompletableFuture.supplyAsync(
//                     () -> loadSafe(userLoader, usersPath), exec
//             );
//             CompletableFuture<List<String>> dictFuture = CompletableFuture.supplyAsync(
//                     () -> loadSafe(dictLoader, dictPath), exec
//             );

//             // Wait for both to complete and combine results
//             List<User> users = usersFuture.join();
//             List<String> dict = dictFuture.join();

//             return new LoadedData(users, dict);
//         } catch (Exception e) {
//             throw new AppException("Failed during concurrent file loading", e);
//         }
//     }

//     /**
//      * Helper method to call loader and wrap exceptions into AppException
//      */
//     private <T> List<T> loadSafe(Loader<T> loader, String path) {
//         try {
//             return loader.load(path);
//         } catch (AppException e) {
//             throw new RuntimeException(e); // wrap checked exception for CompletableFuture
//         }
//     }

//     /**
//      * Simple data record for loaded users and dictionary
//      */
//     public record LoadedData(List<User> users, List<String> dict) {}
// }
