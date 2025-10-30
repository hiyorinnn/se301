// // package org.example.loader;

// // import java.util.List;
// // import java.io.IOException;
// // import java.nio.file.Files;
// // import java.nio.file.Path;
// // import java.util.stream.Stream;

// // import org.example.error.*;
// // import org.example.model.User;


// // public class UserLoader implements Loader<User> {

// //     @Override
// //     public List<User> load(String filename) throws AppException {
// //         try (Stream<String> lines = Files.lines(Path.of(filename))) {
// //             return lines
// //                     .map(line -> line.split(","))
// //                     .filter(parts -> parts.length >= 2)
// //                     .map(parts -> new User(parts[0].trim(), parts[1].trim()))
// //                     .toList(); // Java 16+ collector
// //         } catch (IOException e) {
// //             throw new AppException("Failed to load users from file: " + filename, e);
// //         }
// //     }
// // }

// package org.example.loader;

// import java.io.IOException;
// import java.nio.file.Files;
// import java.nio.file.Path;
// import java.util.ArrayList;
// import java.util.List;
// import java.util.stream.Stream;

// import org.example.error.AppException;
// import org.example.model.User;

// public class UserLoader implements Loader<User> {

//     @Override
//     public List<User> load(String filename) throws AppException {
//         List<User> users = new ArrayList<>();

//         try (Stream<String> lines = Files.lines(Path.of(filename))) {
//             lines.map(line -> line.split(","))                     // split CSV line
//                  .filter(parts -> parts.length >= 2)              // skip invalid lines
//                  .forEach(parts -> users.add(new User(
//                          parts[0].trim(), 
//                          parts[1].trim()
//                  )));                                              // add to list line by line
//         } catch (IOException e) {
//             throw new AppException("Failed to load users from file: " + filename, e);
//         }

//         return users;
//     }
// }

// For set

package org.example.loader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import org.example.error.AppException;
import org.example.model.User;

/**
 * Loader implementation for User objects.
 * 
 * Reads a CSV file line by line and converts each valid line into a User object.
 * Each line must contain at least two comma-separated values: username and hashed password.
 * Lines with fewer than two fields are skipped.
 */
public class UserLoader implements Loader<User> {

    /**
     * Loads User objects from a CSV file.
     * 
     * Each line in the CSV file is expected to have at least two values:
     * - First value: username
     * - Second value: hashed password
     * Lines with fewer than two values are ignored.
     *
     * @param filename the path to the CSV file containing user data
     * @return a set of User objects loaded from the file
     * @throws AppException if the file cannot be read or an I/O error occurs
     */
    @Override
    public Set<User> load(String filename) throws AppException {
        Set<User> users = new HashSet<>();

        try (Stream<String> lines = Files.lines(Path.of(filename))) {
            lines.map(line -> line.split(","))    
                 .filter(parts -> parts.length >= 2)
                 .forEach(parts -> users.add(new User(
                         parts[0].trim(), 
                         parts[1].trim()
                 )));
        } catch (IOException e) {
            throw new AppException("Failed to load users from file: " + filename, e);
        }

        return users;
    }
}

// package org.example.loader;

// import org.example.model.User;
// import org.example.error.AppException;

// import java.io.IOException;
// import java.nio.file.Files;
// import java.nio.file.Path;
// import java.util.List;
// import java.util.Set;
// import java.util.concurrent.ConcurrentHashMap;
// import java.util.stream.Collectors;

// public class UserLoader implements Loader<User> {

//     private static final int BATCH_SIZE = 1000; // adjust depending on User size

//     @Override
//     public Set<User> load(String filePath) throws AppException {
//         Set<User> users = ConcurrentHashMap.newKeySet();

//         try {
//             List<String> lines = Files.readAllLines(Path.of(filePath));

//             // Partition lines into batches
//             for (int i = 0; i < lines.size(); i += BATCH_SIZE) {
//                 int end = Math.min(i + BATCH_SIZE, lines.size());
//                 List<String> batch = lines.subList(i, end);

//                 // Process each batch in parallel
//                 Set<User> batchUsers = batch.parallelStream()
//                         .map(String::trim)
//                         .filter(s -> !s.isEmpty())
//                         .map(this::parseUser)
//                         .collect(Collectors.toSet());

//                 users.addAll(batchUsers); // thread-safe
//             }

//         } catch (IOException e) {
//             throw new AppException("Failed to load users: " + filePath, e);
//         }

//         return users;
//     }

//     private User parseUser(String line) {
//         // Example CSV: name,hashedpassword
//         String[] parts = line.split(",");
//         return new User(parts[0].trim(), parts[1].trim());
//     }
// }
