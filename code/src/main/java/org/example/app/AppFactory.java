package org.example.app;

import org.example.loader.*;
import org.example.hash.*;
import org.example.io.*;

public class AppFactory {
    public static DictionaryAttackRunner createRunner() {
        
        LoadService loadService = new LoadService(new UserLoader(), new DictionaryLoader());
        Hasher hasher = new Sha256Hasher();
        ResultWriter writer = new CsvResultWriter();

        return new DictionaryAttackRunner(loadService, hasher, writer);
    }
}