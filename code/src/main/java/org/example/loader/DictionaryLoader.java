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

// Better for memory

// package org.example.loader;

// import java.io.IOException;
// import java.nio.file.Files;
// import java.nio.file.Path;
// import java.util.HashSet;
// import java.util.Set;
// import java.util.stream.Stream;

// import org.example.error.AppException;

// public class DictionaryLoader implements Loader<String> {

//     private final Set<String> words = new HashSet<>();

//     @Override
//     public Set<String> load(String filePath) throws AppException {
//         try (Stream<String> lines = Files.lines(Path.of(filePath))) {
//             lines.map(String::trim)
//                  .filter(line -> !line.isEmpty())
//                  .forEach(this::processWord);
//         } catch (IOException e) {
//             throw new AppException("Failed to load dictionary: " + filePath, e);
//         }

//         return words;
//     }

//     private void processWord(String word) {
//         words.add(word);  // automatically avoids duplicates
//     }
// }
