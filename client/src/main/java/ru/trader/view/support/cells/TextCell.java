package ru.trader.view.support.cells;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import javafx.util.StringConverter;

public class TextCell<T,V> implements Callback<TableColumn<T, V>, TableCell<T, V>> {
    private StringConverter<V> converter;

    public TextCell() {
    }

    public StringConverter<V> getConverter() {
        return converter;
    }

    public void setConverter(StringConverter<V> converter) {
        this.converter = converter;
    }

    @Override
    public TableCell<T, V> call(TableColumn<T, V> param) {
        return new TableCell<T, V>(){
            @Override
            protected void updateItem(V item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                    setGraphic(null);
                } else {
                    if (converter != null){
                        setText(converter.toString(item));
                    } else {
                        setText(item.toString());
                    }
                    setGraphic(null);
                }
            }
        };
    }

}
