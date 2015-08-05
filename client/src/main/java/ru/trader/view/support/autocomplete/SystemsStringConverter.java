package ru.trader.view.support.autocomplete;

import javafx.util.StringConverter;
import ru.trader.model.MarketModel;
import ru.trader.model.SystemModel;

public class SystemsStringConverter extends StringConverter<SystemModel> {
    private final MarketModel market;

    public SystemsStringConverter(MarketModel market) {
        this.market = market;
    }

    @Override
    public String toString(SystemModel system) {
        return system.getName();
    }

    @Override
    public SystemModel fromString(String name) {
        return market.get(name);
    }
}
