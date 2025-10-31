package org.example.app;

import org.example.CrackTask.Crack;
import org.example.CrackTask.CrackTask;
import org.example.store.LookupTableBuilder;
import org.example.store.StoreHashPassword;
import org.example.loader.LoadService;
import org.example.loader.UserLoader;
import org.example.loader.DictionaryLoader;
import org.example.hash.Hasher;
import org.example.hash.Sha256Hasher;
import org.example.io.ResultWriter;
import org.example.io.CsvResultWriter;

/**
 * Creates and wires together the default application components.
 * Keeps construction in one place for easy replacement in tests.
 */
public class AppFactory {

    /**
     * Builds the default DictionaryAttackRunner with:
     * - file loaders,
     * - SHA-256 hasher,
     * - lookup-table store,
     * - CSV result writer,
     * - and the cracking task.
     */
    public static DictionaryAttackRunner createRunner() {
        LoadService loadService = new LoadService(new UserLoader(), new DictionaryLoader());
        Hasher hasher = new Sha256Hasher();
        StoreHashPassword storeHashPwd = new LookupTableBuilder(hasher);
        ResultWriter writer = new CsvResultWriter();
        Crack cracker = new CrackTask();

        return new DictionaryAttackRunner(loadService, writer, storeHashPwd, cracker);
    }
}
