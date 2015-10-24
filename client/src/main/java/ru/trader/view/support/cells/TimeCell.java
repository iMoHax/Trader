package ru.trader.view.support.cells;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import ru.trader.view.support.ViewUtils;

public class TimeCell<T> implements Callback<TableColumn<T, Long>, TableCell<T, Long>> {

    @Override
    public TableCell<T, Long> call(TableColumn<T, Long> param) {
        return new TableCell<T, Long>(){
            @Override
            protected void updateItem(Long item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(ViewUtils.timeToString(item));
                    setGraphic(null);
                }
            }
        };
    }
}
