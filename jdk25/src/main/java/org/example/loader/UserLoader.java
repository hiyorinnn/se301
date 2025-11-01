package org.example.loader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
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
        Set<User> users = new LinkedHashSet<>();

        try (Stream<String> lines = Files.lines(Path.of(filename))) {
            lines.map(line -> line.split(","))    
                 .filter(parts -> parts.length >= 2)
                 .forEachOrdered(parts -> users.add(new User(
                         parts[0].trim(), 
                         parts[1].trim()
                )));
        } catch (IOException e) {
            throw new AppException("Failed to load users from file: " + filename, e);
        }

        return users;
    }
}

