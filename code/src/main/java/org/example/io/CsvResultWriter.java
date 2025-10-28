package org.example.io;

import org.example.error.AppException;
import org.example.model.User;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
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

//    @Override
//    public void write(String path, Collection<User> users) throws AppException {
//        Path outputPath = Path.of(path);
//
    //        try (BufferedWriter writer = Files.newBufferedWriter(
//                outputPath,
//                StandardCharsets.UTF_8,
//                StandardOpenOption.CREATE,
//                StandardOpenOption.TRUNCATE_EXISTING)) {
//
//            // Write CSV header
//            writer.write(HEADER);
//
//            // Stream and write users line by line (low memory usage)
//            for (User user : users) {
//                if (user.isFound()) {
//                    writer
//                            .append(user.getUsername()).append(',')
//                            .append(user.getHashedPassword()).append(',')
//                            .append(user.getFoundPassword()).append('\n');
//                }
//            }
//
//            System.out.println("\nCracked password details have been written to " + path);
//
//        } catch (IOException e) {
//            throw new AppException("Failed to write CSV file: " + path, e);
//        }
//    }
}
