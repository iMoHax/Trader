package ru.trader.view.support.cells;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import ru.trader.view.support.NaNComparator;

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
                if (empty) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(distanceToString(item));
                    setGraphic(null);
                }
            }
        };
    }

    public static String distanceToString(double distance){
        if (distance < 0.01) return String.format("%.0f Ls", distance / 0.00000003169);
        return String.format("%.2f LY", distance);
    }
}
