package org.example.app;

import org.example.loader.*;
import org.example.model.*;
import org.example.service.*;
import org.example.io.*;

public class AppFactory {
    public static DictionaryAttackRunner createRunner() {
        
        LoadService loadService = new LoadService(new UserLoader(), new DictionaryLoader());
        HashService hashService = new HashService();
        AttackService attackService = new AttackService();
        ResultWriter writer = new CsvResultWriter();

        return new DictionaryAttackRunner(loadService, hashService, attackService, writer);
    }
}
