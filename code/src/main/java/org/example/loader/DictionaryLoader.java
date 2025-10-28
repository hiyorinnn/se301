package org.example.loader;

import java.util.List;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

import org.example.error.*;

public class DictionaryLoader implements Loader<String> {

    @Override
    public List<String> load(String filePath) throws AppException {
        try {
            return Files.lines(Path.of(filePath))
                        .map(String::trim)
                        .filter(line -> !line.isEmpty())
                        .collect(Collectors.toList());
        } catch (IOException e) {
            throw new AppException("Failed to load dictionary: " + filePath, e);
        }  
    }
}