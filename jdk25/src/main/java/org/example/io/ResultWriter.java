package org.example.io;

import org.example.error.AppException;
import org.example.model.User;
import java.util.Collection;

/**
 * Writes a collection of users to an output destination.
 * Implementations decide the format and storage mechanism.
 */
public interface ResultWriter {

    /**
     * Writes the given users to the specified path.
     * Only handles the output; does not modify the users themselves.
     */
    void write(String path, Collection<User> users) throws AppException;
}
