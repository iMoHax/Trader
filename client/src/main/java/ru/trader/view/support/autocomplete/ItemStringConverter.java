package ru.trader.view.support.autocomplete;


import javafx.util.StringConverter;
import ru.trader.model.ItemModel;
import ru.trader.model.MarketModel;

import java.util.Optional;

public class ItemStringConverter extends StringConverter<ItemModel> {
    private final MarketModel market;

    public ItemStringConverter(MarketModel market) {
        this.market = market;
    }

    @Override
    public String toString(ItemModel item) {
        return item.getName();
    }

    @Override
    public ItemModel fromString(String name) {
        Optional<ItemModel> item = market.itemsProperty().stream().filter(i -> i.getName().equals(name)).findAny();
        return item.orElse(null);
    }
}
