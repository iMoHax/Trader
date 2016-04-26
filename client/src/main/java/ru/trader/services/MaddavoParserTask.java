package ru.trader.services;

import javafx.concurrent.Task;
import ru.trader.core.Market;
import ru.trader.maddavo.Parser;

import java.io.File;

public class MaddavoParserTask extends Task<Void> {

    private final File file;
    private final FILE_TYPE type;
    private final Market market;
    private final Parser parser;

    public MaddavoParserTask(File file, FILE_TYPE type, Market market) {
        this.file = file;
        this.type = type;
        this.market = market;
        this.parser = new Parser();
    }

    @Override
    protected void cancelled() {
        parser.cancel();
    }

    @Override
    protected Void call() throws Exception {

        switch (type){

            case SYSTEMS: parser.parseSystems(file, market);
                break;
            case STATIONS: parser.parseStations(file, market);
                break;
            case PRICES: parser.parsePrices(file, market);
                break;
        }
        return null;
    }

    public enum FILE_TYPE {
        SYSTEMS, STATIONS, PRICES
    }
}
