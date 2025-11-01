package org.example.app;

import org.example.loader.LoadService;
import org.example.loader.UserLoader;
import org.example.loader.DictionaryLoader;
import org.example.store.HashLookupBuilder;
import org.example.store.LookupTableBuilder;
import org.example.cracktask.Crack;
import org.example.cracktask.CrackTask;
import org.example.hash.Hasher;
import org.example.hash.Sha256Hasher;
import org.example.io.ResultWriter;
import org.example.io.CsvResultWriter;
import org.example.reporter.ConsoleSummaryReporter;
import org.example.reporter.SummaryReporter;

public class AppFactory {

    private final Hasher hasher;
    private final ResultWriter writer;
    private final Crack cracker;
    private final SummaryReporter summaryReporter;

    public AppFactory(Hasher hasher, ResultWriter writer, Crack cracker, SummaryReporter summaryReporter) {
        this.hasher = hasher;
        this.writer = writer;
        this.cracker = cracker;
        this.summaryReporter = summaryReporter;
    }

    public AppFactory() {
        this(new Sha256Hasher(), new CsvResultWriter(), new CrackTask(), new ConsoleSummaryReporter());
    }

    public DictionaryAttackRunner createRunner() {
        LoadService loadService = new LoadService(new UserLoader(), new DictionaryLoader());
        HashLookupBuilder hashBuilder = new LookupTableBuilder(hasher);

        return new DictionaryAttackRunner(loadService, hashBuilder, cracker, writer, summaryReporter);
    }
}
