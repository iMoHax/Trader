package ru.trader.view.support.cells;

import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

import java.util.function.Function;

public class CustomListCell<T> implements Callback<ListView<T>, ListCell<T>> {

    private final Function<T, String> toString;

    public CustomListCell(Function<T, String> toString) {
        this.toString = toString;
    }

    @Override
    public ListCell<T> call(ListView<T> param){
        return new ListCell<T>(){
            @Override
            public void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null && !empty){
                    setText(toString.apply(item));
                    setGraphic(null);
                } else {
                    setText(null);
                    setGraphic(null);
                }
            }
        };
    }
}
