package org.example.loader;

import java.util.Set;
import org.example.error.AppException;

/**
 * Generic loader interface for loading data of type T from a given path.
 *
 * @param <T> the type of objects this loader produces
 */

public interface Loader<T> {
    /**
     * Loads a set of objects of type T from the specified path.
     *
     * @param path the path to the data source (e.g., a file)
     * @return a set of objects loaded from the given path
     * @throws AppException if the loading fails due to I/O errors or invalid data
     */
    
    // List<T> load(String path) throws AppException;
    Set<T> load(String path) throws AppException; 
}