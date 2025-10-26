package org.example.app;

import org.example.loader.*;
import org.example.model.*;
import org.example.service.*;
import org.example.io.*;

public class AppFactory {

    public static DictionaryAttackRunner createRunner() {
        Loader<User> userLoader = new UserLoader();
        Loader<String> dictLoader = new DictionaryLoader();
        Hasher hasher = new Sha256Hasher();
        ResultWriter writer = new CsvResultWriter();

        return new DictionaryAttackRunner(userLoader, dictLoader, hasher, writer);
    }
}
