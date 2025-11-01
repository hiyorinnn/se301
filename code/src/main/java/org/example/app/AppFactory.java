package org.example.app;

import org.example.store.HashLookupBuilder;
import org.example.store.LookupTableBuilder;
import org.example.loader.LoadService;
import org.example.loader.UserLoader;
import org.example.loader.DictionaryLoader;
import org.example.cracktask.Crack;
import org.example.cracktask.CrackTask;
import org.example.hash.Hasher;
import org.example.hash.Sha256Hasher;
import org.example.io.ResultWriter;
import org.example.io.CsvResultWriter;

/**
 * AppFactory composes and returns a ready-to-use DictionaryAttackRunner.
 * 
 * Supports dependency injection of Hasher, ResultWriter, and Crack, 
 * or uses defaults (SHA-256 hasher, CSV writer, CrackTask).
 *
 * Example:
 * AppFactory factory = new AppFactory();
 * DictionaryAttackRunner runner = factory.createRunner();
 * runner.run();
 */

public class AppFactory {

    private final Hasher hasher;
    private final ResultWriter writer;
    private final Crack cracker;

    /**
     * Allows injection of configurable components, fully adhering to DIP and OCP.
     */
    public AppFactory(Hasher hasher, ResultWriter writer, Crack cracker) {
        this.hasher = hasher;
        this.writer = writer;
        this.cracker = cracker;
    }

    /**
     * Default constructor with standard components.
     */
    public AppFactory() {
        this(new Sha256Hasher(), new CsvResultWriter(), new CrackTask());
    }

    /**
     * Creates the fully-wired DictionaryAttackRunner.
     */
    public DictionaryAttackRunner createRunner() {
        LoadService loadService = new LoadService(new UserLoader(), new DictionaryLoader());
        HashLookupBuilder storeHashPwd = new LookupTableBuilder(hasher);

        return new DictionaryAttackRunner(loadService, writer, storeHashPwd, cracker);
    }
}
