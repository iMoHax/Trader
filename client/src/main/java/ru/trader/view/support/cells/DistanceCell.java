package ru.trader.view.support.cells;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import ru.trader.view.support.NaNComparator;
import ru.trader.view.support.ViewUtils;

public class DistanceCell<T> implements Callback<TableColumn<T, Double>, TableCell<T, Double>> {
    public DistanceCell() {
    }

    @Override
    public TableCell<T, Double> call(TableColumn<T, Double> param) {
        param.setComparator(new NaNComparator<>());
        return new TableCell<T, Double>(){
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(ViewUtils.distanceToString(item));
                    setGraphic(null);
                }
            }
        };
    }

}
