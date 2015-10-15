package ru.trader.view.support.autocomplete;

import javafx.util.StringConverter;
import org.controlsfx.control.textfield.AutoCompletionBinding;
import ru.trader.model.MarketModel;
import ru.trader.model.SystemModel;

import java.util.Comparator;

public class SystemsProvider extends AbstractSuggestionProvider<String> {

    private final StringConverter<SystemModel> converter;
    private final Comparator<String> comparator;


    public SystemsProvider(MarketModel market) {
        super(market.getSystemNames());
        converter = new SystemsStringConverter(market);
        comparator = (s1, s2) -> s1.toLowerCase().compareTo(s2.toLowerCase());
    }

    @Override
    protected Comparator<String> getComparator() {
        return comparator;
    }

    @Override
    protected boolean isMatch(String suggestion, AutoCompletionBinding.ISuggestionRequest request) {
        String s = suggestion.toLowerCase();
        return s.startsWith(request.getUserText().toLowerCase());
    }

    public StringConverter<SystemModel> getConverter() {
        return converter;
    }
}
