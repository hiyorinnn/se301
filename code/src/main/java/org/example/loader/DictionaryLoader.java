// package org.example.loader;

// import java.util.List;
// import java.io.IOException;
// import java.nio.file.Files;
// import java.nio.file.Path;
// import java.util.stream.Collectors;

// import org.example.error.*;

// public class DictionaryLoader implements Loader<String> {

//     @Override
//     public List<String> load(String filePath) throws AppException {
//         try {
//             return Files.lines(Path.of(filePath))
//                         .map(String::trim)
//                         .filter(line -> !line.isEmpty())
//                         .collect(Collectors.toList());
//         } catch (IOException e) {
//             throw new AppException("Failed to load dictionary: " + filePath, e);
//         }  
//     }
// }

// Better for memory

package org.example.loader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import org.example.error.AppException;

/**
 * Loader implementation for dictionaries.
 *
 * Reads a text file line by line and loads each non-empty line as a word
 * into a set. Duplicate words are automatically ignored.
 */
public class DictionaryLoader implements Loader<String> {

    private final Set<String> words = new HashSet<>();

    /**
     * Loads words from a text file into a set.
     *
     * Each non-empty line in the file is treated as a single word.
     * Duplicate words are ignored.
     *
     * @param filePath the path to the dictionary file
     * @return a set of unique words loaded from the file
     * @throws AppException if the file cannot be read or an I/O error occurs
     */
    @Override
    public Set<String> load(String filePath) throws AppException {
        try (Stream<String> lines = Files.lines(Path.of(filePath))) {
            lines.map(String::trim)
                 .filter(line -> !line.isEmpty())
                 .forEach(this::processWord);
        } catch (IOException e) {
            throw new AppException("Failed to load dictionary: " + filePath, e);
        }

        return words;
    }

    /**
     * Adds a word to the set of loaded words.
     * 
     * Duplicates are automatically ignored.
     *
     * @param word the word to add
     */
    private void processWord(String word) {
        words.add(word);
    }
}

// package org.example.loader;

// import org.example.error.AppException;

// import java.io.IOException;
// import java.nio.file.Files;
// import java.nio.file.Path;
// import java.util.List;
// import java.util.Set;
// import java.util.concurrent.ConcurrentHashMap;
// import java.util.stream.Collectors;

// public class DictionaryLoader implements Loader<String> {

//     private static final int BATCH_SIZE = 1000; // adjust as needed

//     @Override
//     public Set<String> load(String filePath) throws AppException {
//         Set<String> words = ConcurrentHashMap.newKeySet();

//         try {
//             List<String> lines = Files.readAllLines(Path.of(filePath));

//             // Partition lines into batches
//             for (int i = 0; i < lines.size(); i += BATCH_SIZE) {
//                 int end = Math.min(i + BATCH_SIZE, lines.size());
//                 List<String> batch = lines.subList(i, end);

//                 // Process each batch in parallel
//                 Set<String> batchWords = batch.parallelStream()
//                         .map(String::trim)
//                         .filter(s -> !s.isEmpty())
//                         .collect(Collectors.toSet());

//                 words.addAll(batchWords); // thread-safe
//             }

//         } catch (IOException e) {
//             throw new AppException("Failed to load dictionary: " + filePath, e);
//         }

//         return words;
//     }
// }

