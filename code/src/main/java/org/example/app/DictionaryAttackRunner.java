package org.example.app;

import org.example.CrackTask.CrackTask;
import org.example.PasswordHashStore.LookupTableBuilder;
import org.example.loader.*;
import org.example.model.*;
import org.example.service.*;
import org.example.io.*;
import org.example.error.AppException;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class DictionaryAttackRunner {

    private final Loader<User> userLoader;
    private final Loader<String> dictLoader;
    private final Hasher hasher;
    private final ResultWriter resultWriter;


    public DictionaryAttackRunner(Loader<User> userLoader,
                                  Loader<String> dictLoader,
                                  Hasher hasher,
                                  ResultWriter resultWriter) {
        this.userLoader = userLoader;
        this.dictLoader = dictLoader;
        this.hasher = hasher;
        this.resultWriter = resultWriter;
    }

    public void run(String usersPath, String dictPath, String outputPath) throws AppException {
        long start = System.currentTimeMillis();

        // 1. Load data (single thread)
        List<User> users = userLoader.load(usersPath);
        List<String> dict = dictLoader.load(dictPath);

        // 2. instantiate CrackTask, todo: make it generic?
        LookupTableBuilder mapper = new LookupTableBuilder(dict, hasher);
        long totalUsers = users.size();
        System.out.println("Starting attack with " + totalUsers + " total tasks...");
        Map<String, String> hashToPlaintext = mapper.buildHashLookupTable();

        // 3. Lookup
        AtomicLong passwordsFound = new AtomicLong(0);
        CrackTask cracker = new CrackTask(users, hashToPlaintext, passwordsFound);
        cracker.crack();


        // 4. Print final summary todo: seperate this
        System.out.println();
        System.out.println("Total passwords found: " + passwordsFound);
        System.out.println("Total hashes computed: " + hashToPlaintext.size());
        System.out.println("Total time spent (ms): " + (System.currentTimeMillis() - start));


        // 5. Write results todo
        resultWriter.write(outputPath, users);
    }

}
