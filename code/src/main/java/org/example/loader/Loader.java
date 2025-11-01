package org.example.loader;

import java.util.Set;
import org.example.error.AppException;

/* Generic interface for loading data of type T from a given path. */
public interface Loader<T> {
    /* Load a set of objects of type T from the specified path.
       Throws AppException on I/O errors or invalid data. */
    Set<T> load(String path) throws AppException;
}
