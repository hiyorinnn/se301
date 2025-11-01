package org.example.loader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import org.example.error.AppException;
import org.example.model.User;

/* Loads User objects from a CSV file.
   Expects each line to have at least: username, hashed password.
   Lines with fewer than two fields are ignored. */
public class UserLoader implements Loader<User> {

    /* Read users from the given CSV file and return as a set. */
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

