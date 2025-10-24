package org.example.io;

import org.example.error.*;
import org.example.loader.*;

public interface ResultWriter<T> {
    void write(String path, Loader<T> loadType) throws AppException;
}
