package ru.trader.maddavo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.core.Market;

import java.io.*;

public class Parser {
    private final static Logger LOG = LoggerFactory.getLogger(Parser.class);

    public static void parseSystems(File file, Market market) throws IOException {
        parseFile(file, new SystemHandler(market), 1);

    }

    public static void parseStations(File file, Market market) throws IOException {
        parseFile(file, new StationHandler(market), 1);

    }

    public static void parsePrices(File file, Market market) throws IOException {
        parseFile(file, new OffersHandler(market, true), 0);

    }

    private static void parseFile(File file, ParseHandler handler, int skip) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            int row = 0;
            String line;
            while ((line = reader.readLine()) != null) {
                row++;
                if (row <= skip) continue;
                handler.parse(line);
            }
        }
    }
}
