// to add method for each "task"
//import title change to specific

package org.example.app;

import org.example.loader.*;
import org.example.model.*;
import org.example.threads.ExecutorProvider;
import org.example.io.*;
import org.example.CrackTask.CrackTask;
import org.example.PasswordHashStore.LookupTableBuilder;
import org.example.error.AppException;
import org.example.hash.*;
import org.example.threads.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class DictionaryAttackRunner {

    private final LoadService loadService;
    private final Hasher hasher;
    private final ResultWriter resultWriter;

    public DictionaryAttackRunner(LoadService loadService,
                                  Hasher hasher,
                                  ResultWriter resultWriter) {
        this.loadService = loadService;
        this.hasher = hasher;
        this.resultWriter = resultWriter;
    }

    public void run(String usersPath, String dictPath, String outputPath) throws AppException {
        long start = System.currentTimeMillis();

        // 1. Load data ((Virtual)
        // ExecutorProvider ioProvider = new VirtualExecutorProvider();
        // LoadService.LoadedData data = loadService.load(usersPath, dictPath, ioProvider);

        // 1. Load data (Platform Thread)
        LoadService.LoadedData data;
        try (ExecutorProvider ioProvider = ConfigurableExecutorProvider.fixedCpuPool()) {
            data = loadService.load(usersPath, dictPath, ioProvider);
        }

        // Now safe to use
        Set<User> users = data.users();
        Set<String> dict = data.dict();

        // 2. instantiate CrackTask
        LookupTableBuilder hashPwd = new LookupTableBuilder(dict, hasher);
        Map<String, String> hashToPlaintext = hashPwd.buildHashLookupTable();

        // Print the start of attack 
        long totalUsers = users.size();
        System.out.println("Starting attack with " + totalUsers + " total tasks...");

        // 3. Lookup
        AtomicLong passwordsFound = new AtomicLong(0);
        CrackTask cracker = new CrackTask(users, hashToPlaintext, passwordsFound);
        cracker.crack();

        // 4. Print final summary 
        System.out.println();
        System.out.println("Total passwords found: " + passwordsFound);
        System.out.println("Total hashes computed: " + hashToPlaintext.size());
        System.out.println("Total time spent (ms): " + (System.currentTimeMillis() - start));

        // 5. Write results
        resultWriter.write(outputPath, data.users());
    }

}
