package ru.trader.maddavo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.core.Market;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Parser {
    private final static Logger LOG = LoggerFactory.getLogger(Parser.class);

    private boolean canceled;

    public void parseSystems(File file, Market market) throws IOException {
        parseFile(file, new SystemHandler(market), 1);

    }

    public void parseStations(File file, Market market) throws IOException {
        parseFile(file, new StationHandler(market), 1);

    }

    public void parsePrices(File file, Market market) throws IOException {
        parseFile(file, new OffersHandler(market, true), 0);

    }

    private void parseFile(File file, ParseHandler handler, int skip) throws IOException {
        canceled = false;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            int row = 0;
            String line;
            while ((line = reader.readLine()) != null) {
                if (canceled) break;
                row++;
                if (row <= skip) continue;
                handler.parse(line);
            }
        }
    }

    public void cancel(){
        this.canceled = true;
    }
}
