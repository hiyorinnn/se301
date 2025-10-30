package org.example.io;

import org.example.error.AppException;
import org.example.model.User;
import java.util.Collection;

public interface ResultWriter {
    void write(String path, Collection<User> users) throws AppException;
}
