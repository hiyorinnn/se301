package org.example.io;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.example.model.User;
import org.example.error.AppException;

public class CsvResultWriter implements ResultWriter {

    private static final int BUFFER_SIZE = 32 * 1024;
    private static final String HEADER = "user_name,hashed_password,plain_password\n";

    @Override
    public void write(String path, Collection<User> users) throws AppException {

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path, StandardCharsets.UTF_8), BUFFER_SIZE)) {
            writer.write(HEADER);

            List<String> lines = users.stream()
                                      .filter(User::isFound)
                                      .map(u -> u.getUsername() + "," + u.getHashedPassword() + "," + u.getFoundPassword())
                                      .collect(Collectors.toList());

            // Write all lines at once
            for (String line : lines) {
                writer.write(line);
                writer.write('\n');
            }

            System.out.println("\nCracked password details have been written to " + path);

        } catch (IOException e) {
            throw new AppException("Failed to write CSV file: " + path, e);
        }
    }
}
