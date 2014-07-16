package ru.trader.view.support.cells;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import ru.trader.view.support.NaNComparator;

public class DoubleCell<T> implements Callback<TableColumn<T, Double>, TableCell<T, Double>> {
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
                     setText(String.format("%.0f", item));
                     setGraphic(null);
                }
            }
        };
    }
}
