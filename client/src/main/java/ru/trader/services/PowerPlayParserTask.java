package ru.trader.services;

import javafx.concurrent.Task;
import ru.trader.core.Market;
import ru.trader.powerplay.PPParser;

import java.io.File;

public class PowerPlayParserTask extends Task<Void> {

    private final File file;
    private final FILE_TYPE type;
    private final PPParser parser;

    public PowerPlayParserTask(File file, FILE_TYPE type, Market market) {
        this.file = file;
        this.type = type;
        this.parser = new PPParser(market);
    }

    @Override
    protected void cancelled() {
        parser.cancel();
    }

    @Override
    protected Void call() throws Exception {

        switch (type){

            case SYSTEMS: parser.parseSystems(file);
                break;
            case PREDICTION: parser.parsePrediction(file);
                break;
        }
        return null;
    }

    public enum FILE_TYPE {
        SYSTEMS, PREDICTION
    }
}
