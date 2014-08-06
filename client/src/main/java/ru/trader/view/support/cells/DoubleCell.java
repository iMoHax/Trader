package ru.trader.view.support.cells;

import javafx.beans.NamedArg;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import ru.trader.view.support.NaNComparator;

public class DoubleCell<T> implements Callback<TableColumn<T, Double>, TableCell<T, Double>> {
    private String format = "%.0f";

    public DoubleCell() {
    }

    public DoubleCell(@NamedArg("format")String format) {
        this.format = format;
    }

    @Override
    public TableCell<T, Double> call(TableColumn<T, Double> param) {
        param.setComparator(new NaNComparator<>());
        return new TableCell<T, Double>(){
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                    setGraphic(null);
                } else {
                     setText(String.format(format, item));
                     setGraphic(null);
                }
            }
        };
    }
}
