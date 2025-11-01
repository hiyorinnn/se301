package org.example.loader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import org.example.error.AppException;

/* Loads dictionary words from a text file.
   Each non-empty line becomes a word; duplicates are ignored. */
public class DictionaryLoader implements Loader<String> {

    private final Set<String> words = new HashSet<>();

    /* Read words from the given file and return a set of unique words. */
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

    /* Add a word to the set; duplicates are automatically ignored. */
    private void processWord(String word) {
        words.add(word);
    }
}
