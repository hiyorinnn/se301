package org.example.app;

import org.example.CrackTask.Crack;
import org.example.CrackTask.CrackTask;
import org.example.StoreHashPassword.LookupTableBuilder;
import org.example.StoreHashPassword.StoreHashPassword;
import org.example.loader.*;
import org.example.hash.*;
import org.example.io.*;

public class AppFactory {
    public static DictionaryAttackRunner createRunner() {
        
        LoadService loadService = new LoadService(new UserLoader(), new DictionaryLoader());
        Hasher hasher = new Sha256Hasher();
        StoreHashPassword storeHashPwd = new LookupTableBuilder(hasher);
        ResultWriter writer = new CsvResultWriter();
        Crack cracker = new CrackTask();

        return new DictionaryAttackRunner(loadService, writer, storeHashPwd, cracker);
    }
}