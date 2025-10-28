package org.example.app;

import org.example.error.AppException;
import org.example.io.ResultWriter;
import org.example.service.*;
import org.example.threads.ExecutorProvider;
import org.example.threads.VirtualExecutorProvider;

import java.util.Map;

public class DictionaryAttackRunner {

    private final LoadService loadService;
    private final HashService hashService;
    private final AttackService attackService;
    private final ResultWriter resultWriter;

    public DictionaryAttackRunner(LoadService loadService,
                                  HashService hashService,
                                  AttackService attackService,
                                  ResultWriter resultWriter) {
        this.loadService = loadService;
        this.hashService = hashService;
        this.attackService = attackService;
        this.resultWriter = resultWriter;
    }

    public void run(String usersPath, String dictPath, String outputPath) throws AppException {
        long start = System.currentTimeMillis();

        // Step 1: Load data (I/O-bound)
        ExecutorProvider ioProvider = new VirtualExecutorProvider();
        LoadService.LoadedData data = loadService.load(usersPath, dictPath, ioProvider);

        long total = data.dict().size() + data.users().size();
        ProgressReporter reporter = new ProgressReporter(total);

        // Step 2: Hash passwords
        Map<String, String> hashMap = hashService.buildHashDictionary(data.dict(), reporter);

        // Step 3: Crack users
        long found = attackService.crackPasswords(data.users(), hashMap, reporter);

        long duration = System.currentTimeMillis() - start;

        System.out.println("");
        System.out.println("Total passwords found: " + passwordsFound);
        System.out.println("Total hashes computed: " + hashesComputed);
        System.out.println("Total time spent (milliseconds): " + (System.currentTimeMillis() - start));

        // Step 4: Output results
        resultWriter.write(outputPath, data.users());
    }
}
