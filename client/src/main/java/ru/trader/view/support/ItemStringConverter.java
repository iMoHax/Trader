package ru.trader.view.support;


import javafx.util.StringConverter;
import ru.trader.controllers.MainController;
import ru.trader.model.ItemModel;

import java.util.Optional;

public class ItemStringConverter extends StringConverter<ItemModel> {

    @Override
    public String toString(ItemModel item) {
        return item.getName();
    }

    @Override
    public ItemModel fromString(String name) {
        Optional<ItemModel> item = MainController.getWorld().itemsProperty().stream().filter(i -> i.getName().equals(name)).findAny();
        return item.orElse(null);
    }
}
