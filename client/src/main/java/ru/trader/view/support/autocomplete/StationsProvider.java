package ru.trader.view.support.autocomplete;

import javafx.util.StringConverter;
import org.controlsfx.control.textfield.AutoCompletionBinding;
import ru.trader.model.MarketModel;
import ru.trader.model.StationModel;

import java.util.Comparator;

public class StationsProvider extends AbstractSuggestionProvider<String> {

    private final StringConverter<StationModel> converter;
    private final Comparator<String> comparator;


    public StationsProvider(MarketModel market) {
        super(market.getStationNames());
        converter = new StationStringConverter(market);
        comparator = (s1, s2) -> s1.toLowerCase().compareTo(s2.toLowerCase());
    }

    @Override
    protected Comparator<String> getComparator() {
        return comparator;
    }

    @Override
    protected boolean isMatch(String suggestion, AutoCompletionBinding.ISuggestionRequest request) {
        String s = suggestion.toLowerCase();
        return s.contains(request.getUserText().toLowerCase());
    }

    public StringConverter<StationModel> getConverter() {
        return converter;
    }
}
