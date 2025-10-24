package org.example.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import org.example.model.User;
import org.example.error.AppException;
import org.example.loader.*;

public class CsvResultWriter implements ResultWriter<User> {

    private static final int BUFFER_SIZE = 32 * 1024;
    private static final String HEADER = "user_name,hashed_password,plain_password\n";

    @Override
    public void write(String filePath, Loader<User> userLoader) {
        List<User> users = userLoader.load(filePath);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, StandardCharsets.UTF_8), BUFFER_SIZE)) {
            writer.write(HEADER);

            // Stream, filter, and map to CSV lines
            List<String> lines = users.stream()
                                      .filter(u -> u.isFound())
                                      .map(u -> u.getUsername() + "," + u.getHashedPassword() + "," + u.getFoundPassword())
                                      .collect(Collectors.toList());

            // Write all lines at once
            for (String line : lines) {
                writer.write(line);
                writer.write('\n');
            }
            System.out.println("\nCracked password details have been written to " + filePath);

        } catch (IOException e) {
            throw new AppException("Failed to write CSV file: " + filePath, e);
        }
    }
}
