// package org.example.loader;

// import org.example.model.User;
// import org.example.error.AppException;
// import org.example.threads.ExecutorProvider;

// import java.util.List;
// import java.util.concurrent.*;

// public class LoadService {
//     private final Loader<User> userLoader;
//     private final Loader<String> dictLoader;

//     public LoadService(Loader<User> userLoader, Loader<String> dictLoader) {
//         this.userLoader = userLoader;
//         this.dictLoader = dictLoader;
//     }

//     public LoadedData load(String usersPath, String dictPath, ExecutorProvider provider) throws AppException {
//         try (provider) {
//             ExecutorService exec = provider.get();
//             Future<List<User>> usersFuture = exec.submit(() -> userLoader.load(usersPath));
//             Future<List<String>> dictFuture  = exec.submit(() -> dictLoader.load(dictPath));

//             return new LoadedData(usersFuture.get(), dictFuture.get());
//         } catch (ExecutionException e) {
//             throw new AppException("Failed during concurrent file loading", e.getCause());
//         } catch (InterruptedException e) {
//             Thread.currentThread().interrupt();
//             throw new AppException("Loading interrupted", e);
//         }
//     }

//     public record LoadedData(List<User> users, List<String> dict) {}
// }

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

// package org.example.loader;

// import org.example.model.User;
// import org.example.error.AppException;

// import java.util.Set;
// import java.util.concurrent.StructuredTaskScope;

// /**
//  * Service responsible for orchestrating concurrent loading of data sources.
//  * Uses Structured Concurrency (JDK 25) for cleaner, safer parallel execution.
//  */
// public class LoadService {

//     private final Loader<User> userLoader;
//     private final Loader<String> dictLoader;

//     public LoadService(Loader<User> userLoader, Loader<String> dictLoader) {
//         this.userLoader = userLoader;
//         this.dictLoader = dictLoader;
//     }

//     /**
//      * Load users and dictionary concurrently using structured concurrency.
//      *
//      * @param usersPath path to user file
//      * @param dictPath  path to dictionary file
//      * @return LoadedData containing users and dictionary
//      * @throws AppException if loading fails or is interrupted
//      */
//     public LoadedData load(String usersPath, String dictPath) throws AppException {
//         try (var scope = new StructuredTaskScope.ShutdownOnFailure<Void>()) {

//             var usersFuture = scope.fork(() -> userLoader.load(usersPath));
//             var dictFuture = scope.fork(() -> dictLoader.load(dictPath));

//             scope.join(); // wait for both to complete
//             return new LoadedData(usersFuture.resultNow(), dictFuture.resultNow());

//         } catch (StructuredTaskScope.FailedException e) {
//             throw new AppException("Failed during concurrent file loading", e.getCause());
//         } catch (InterruptedException e) {
//             Thread.currentThread().interrupt();
//             throw new AppException("Loading was interrupted", e);
//         }
//     }

//     /**
//      * Immutable data record for loaded users and dictionary.
//      */
//     public record LoadedData(Set<User> users, Set<String> dict) {}
// }
