package ru.trader.maddavo;

import au.com.bytecode.opencsv.CSVParser;
import ru.trader.core.Market;

import java.io.IOException;

public abstract class CSVParseHandler implements ParseHandler {
    private final CSVParser parser;
    protected final Market market;

    protected CSVParseHandler(Market market) {
        this.market = market;
        parser = new CSVParser(',', '\'');
    }

    @Override
    public void parse(String str) throws IOException {
        String[] values = parser.parseLine(str);
        for (int i = 0; i < values.length; i++) {
            String value = values[i];
            if ("?".equals(value) || "-".equals(value)){
                values[i] = "";
            }
        }
        doWork(values);
    }

    protected abstract void doWork(String[] values);
}
