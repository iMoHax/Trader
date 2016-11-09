package ru.trader.services;

import javafx.concurrent.Task;
import ru.trader.core.Market;
import ru.trader.store.imp.Importer;


public class ImportTask extends Task<Void> {

    private final Market market;
    private final Importer importer;

    public ImportTask(Importer importer, Market market) {
        this.importer = importer;
        this.market = market;
    }

    @Override
    protected void cancelled() {
        importer.cancel();
    }

    @Override
    protected Void call() throws Exception {
        importer.imp(market);
        return null;
    }

}
