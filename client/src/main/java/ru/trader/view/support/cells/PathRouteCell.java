package ru.trader.view.support.cells;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.util.Callback;
import ru.trader.model.RouteModel;
import ru.trader.view.support.RouteNode;

public class PathRouteCell<T> implements Callback<TableColumn<RouteModel, T>, TableCell<RouteModel, T>> {

    @Override
    public TableCell<RouteModel, T> call(TableColumn<RouteModel, T> param) {
        return new TableCell<RouteModel, T>(){
            @Override
            public void updateItem(T value, boolean empty) {
                super.updateItem(value, empty);
                TableRow row = getTableRow();
                if (!empty && row !=null && row.getItem() != null){
                    RouteNode route = new RouteNode((RouteModel) row.getItem());
                    setText(null);
                    setGraphic(route.getNode());
                } else {
                    setText(null);
                    setGraphic(null);
                }
            }


        };
    }

}
