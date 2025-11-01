package org.example.app;

import org.example.loader.Loading;
import org.example.service.*;
import org.example.store.HashLookupBuilder;
import org.example.io.ResultWriter;
import org.example.cracktask.Crack;
import org.example.error.AppException;
import org.example.reporter.SummaryReporter;

import java.util.Map;

/* Orchestrates a full dictionary attack:
   - loads user and dictionary data
   - builds hash lookup table
   - performs cracking
   - writes results
   - prints summary */
public class DictionaryAttackRunner {

    private final LoadService dataLoader;
    private final HashLookupService hashLookupService;
    private final CrackService crackService;
    private final ResultService resultService;
    private final SummaryReporter summaryReporter;

    // wires services together from core components
    public DictionaryAttackRunner(Loading loadService,
                                  HashLookupBuilder storeHashPwd,
                                  Crack cracker,
                                  ResultWriter writer,
                                  SummaryReporter summaryReporter) {
        this.dataLoader = new LoadService(loadService);
        this.hashLookupService = new HashLookupService(storeHashPwd);
        this.crackService = new CrackService(cracker);
        this.resultService = new ResultService(writer);
        this.summaryReporter = summaryReporter;
    }

    // run the dictionary attack end-to-end
    public void run(String usersPath, String dictPath, String outputPath) throws AppException, InterruptedException {
        long start = System.currentTimeMillis();

        var data = dataLoader.load(usersPath, dictPath, 2);

        Map<String, String> hashToPlaintext = hashLookupService.buildWithProgress(data.dict());

        long passwordsFound = crackService.crackAll(data.users(), hashToPlaintext);

        resultService.write(outputPath, data.users());

        long elapsed = System.currentTimeMillis() - start;

        summaryReporter.printSummary(data.users().size(), hashToPlaintext.size(), passwordsFound, elapsed);
    }
}
