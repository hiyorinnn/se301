package org.example.loader;

import java.util.List;
import org.example.error.*;

public interface Loader<T> {
    List<T> load(String path) throws AppException; 
}