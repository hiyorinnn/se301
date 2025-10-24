package org.example.loader;

import java.util.List;
import java.util.stream.Collectors;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.example.error.*;

public class DictionaryLoader implements Loader<String> {

    @Override
    public List<String> load(String filePath) throws AppException {
        try {
            return Files.lines(Path.of(filePath))       
                        .parallel() // concurrency
                        .map(String::trim) 
                        .filter(line -> !line.isEmpty()) 
                        .collect(Collectors.toList()); // thread-safe
        } catch (IOException e) {
            throw new AppException("Failed to load dictionary: " + filePath, e);
        }  
    }
}
