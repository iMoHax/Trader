package ru.trader.view.support.autocomplete;

import javafx.util.StringConverter;
import org.controlsfx.control.textfield.AutoCompletionBinding;
import ru.trader.model.ItemModel;
import ru.trader.model.MarketModel;

import java.util.Comparator;

public class ItemsProvider extends AbstractSuggestionProvider<String> {

    private final StringConverter<ItemModel> converter;
    private final Comparator<String> comparator;


    public ItemsProvider(MarketModel market) {
        super(market.getItemNames());
        converter = new ItemStringConverter(market);
        comparator = (i1, i2) -> i1.toLowerCase().compareTo(i2.toLowerCase());
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

    public StringConverter<ItemModel> getConverter() {
        return converter;
    }
}
