// package org.example.loader;

// import java.util.List;
// import java.io.IOException;
// import java.nio.file.Files;
// import java.nio.file.Path;
// import java.util.stream.Stream;

// import org.example.error.*;
// import org.example.model.User;


// public class UserLoader implements Loader<User> {

//     @Override
//     public List<User> load(String filename) throws AppException {
//         try (Stream<String> lines = Files.lines(Path.of(filename))) {
//             return lines
//                     .map(line -> line.split(","))
//                     .filter(parts -> parts.length >= 2)
//                     .map(parts -> new User(parts[0].trim(), parts[1].trim()))
//                     .toList(); // Java 16+ collector
//         } catch (IOException e) {
//             throw new AppException("Failed to load users from file: " + filename, e);
//         }
//     }
// }

package org.example.loader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.example.error.AppException;
import org.example.model.User;

public class UserLoader implements Loader<User> {

    @Override
    public List<User> load(String filename) throws AppException {
        List<User> users = new ArrayList<>();

        try (Stream<String> lines = Files.lines(Path.of(filename))) {
            lines.map(line -> line.split(","))                     // split CSV line
                 .filter(parts -> parts.length >= 2)              // skip invalid lines
                 .forEach(parts -> users.add(new User(
                         parts[0].trim(), 
                         parts[1].trim()
                 )));                                              // add to list line by line
        } catch (IOException e) {
            throw new AppException("Failed to load users from file: " + filename, e);
        }

        return users;
    }
}

// For set

// package org.example.loader;

// import java.io.IOException;
// import java.nio.file.Files;
// import java.nio.file.Path;
// import java.util.HashSet;
// import java.util.Set;
// import java.util.stream.Stream;

// import org.example.error.AppException;
// import org.example.model.User;

// public class UserLoader implements Loader<User> {

//     @Override
//     public Set<User> load(String filename) throws AppException {
//         Set<User> users = new HashSet<>();

//         try (Stream<String> lines = Files.lines(Path.of(filename))) {
//             lines.map(line -> line.split(","))                     // split CSV line
//                  .filter(parts -> parts.length >= 2)              // skip invalid lines
//                  .forEach(parts -> users.add(new User(
//                          parts[0].trim(), 
//                          parts[1].trim()
//                  )));                                              // add to set line by line
//         } catch (IOException e) {
//             throw new AppException("Failed to load users from file: " + filename, e);
//         }

//         return users;
//     }
// }
