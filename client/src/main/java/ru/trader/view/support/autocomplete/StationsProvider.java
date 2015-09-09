package ru.trader.view.support.autocomplete;

import javafx.util.StringConverter;
import org.controlsfx.control.textfield.AutoCompletionBinding;
import ru.trader.model.MarketModel;
import ru.trader.model.StationModel;

import java.util.Comparator;

public class StationsProvider extends CachedSuggestionProvider<StationModel> {

    private final StringConverter<StationModel> converter;
    private final Comparator<StationModel> comparator;


    public StationsProvider(MarketModel market) {
        super(market.stationsProperty());
        converter = new StationStringConverter(market);
        comparator = (s1, s2) -> converter.toString(s1).toLowerCase().compareTo(converter.toString(s2).toLowerCase());
    }

    @Override
    protected Comparator<StationModel> getComparator() {
        return comparator;
    }

    @Override
    protected boolean isMatch(StationModel suggestion, AutoCompletionBinding.ISuggestionRequest request) {
        String s = converter.toString(suggestion).toLowerCase();
        return s.contains(request.getUserText().toLowerCase());
    }

    public StringConverter<StationModel> getConverter() {
        return converter;
    }
}
