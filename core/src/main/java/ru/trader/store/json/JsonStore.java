package ru.trader.store.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import ru.trader.core.Market;
import ru.trader.core.MarketFilter;

import java.io.File;
import java.io.IOException;

public class JsonStore {
    private final ObjectMapper objectMapper;
    private static final String FILTERS_FILE_NAME = "filters.cfg";

    public JsonStore() {
        objectMapper = new ObjectMapper();
    }

    public MarketFilter getFilter(Market market) throws IOException {
        FiltersStore store = new FiltersStore(objectMapper);
        return store.read(market, new File(FILTERS_FILE_NAME));
    }

    public void saveFilter(MarketFilter filter) throws IOException {
        FiltersStore store = new FiltersStore(objectMapper);
        store.write(filter, new File(FILTERS_FILE_NAME));
    }
}
