package org.example.io;

import org.example.error.AppException;
import org.example.model.User;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.stream.Collectors;

public class CsvResultWriter implements ResultWriter {

    private static final String HEADER = "user_name,hashed_password,plain_password\n";

    @Override
    public void write(String path, Collection<User> users) throws AppException {

        String csvContent = HEADER +
                users.stream()
                     .filter(User::isFound)
                     .map(u -> u.getUsername() + "," + u.getHashedPassword() + "," + u.getFoundPassword())
                     .collect(Collectors.joining("\n")) +
                "\n"; 

        try {
            Files.writeString(Path.of(path), csvContent, StandardCharsets.UTF_8);
            System.out.println("\nCracked password details have been written to " + path);
        } catch (IOException e) {
            throw new AppException("Failed to write CSV file: " + path, e);
        }
    }
}
