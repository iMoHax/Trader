package ru.trader.view.support.autocomplete;

import javafx.util.StringConverter;
import org.controlsfx.control.textfield.AutoCompletionBinding;
import ru.trader.model.MarketModel;
import ru.trader.model.SystemModel;

import java.util.Comparator;

public class SystemsProvider extends CachedSuggestionProvider<SystemModel> {

    private final StringConverter<SystemModel> converter;
    private final Comparator<SystemModel> comparator;


    public SystemsProvider(MarketModel market) {
        super(market.systemsProperty());
        converter = new SystemsStringConverter(market);
        comparator = (s1, s2) -> converter.toString(s1).toLowerCase().compareTo(converter.toString(s2).toLowerCase());
    }

    @Override
    protected Comparator<SystemModel> getComparator() {
        return comparator;
    }

    @Override
    protected boolean isMatch(SystemModel suggestion, AutoCompletionBinding.ISuggestionRequest request) {
        String s = converter.toString(suggestion).toLowerCase();
        return s.contains(request.getUserText().toLowerCase());
    }

    public StringConverter<SystemModel> getConverter() {
        return converter;
    }
}
