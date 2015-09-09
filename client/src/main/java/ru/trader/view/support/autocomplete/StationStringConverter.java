package ru.trader.view.support.autocomplete;

import javafx.util.StringConverter;
import ru.trader.model.MarketModel;
import ru.trader.model.StationModel;
import ru.trader.model.SystemModel;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StationStringConverter extends StringConverter<StationModel> {
    private final MarketModel market;
    private static final Pattern STATION_REGEXP = Pattern.compile("([^:]+): (.+)");

    public StationStringConverter(MarketModel market) {
        this.market = market;
    }

    @Override
    public String toString(StationModel station) {
        return station.getSystem().getName()+": "+station.getName();
    }

    @Override
    public StationModel fromString(String name) {
        Matcher matcher = STATION_REGEXP.matcher(name);
        if (matcher.find()){
            SystemModel system = market.get(matcher.group(1));
            if (system != null){
                return system.get(matcher.group(2));
            }
        }
        return null;
    }
}
