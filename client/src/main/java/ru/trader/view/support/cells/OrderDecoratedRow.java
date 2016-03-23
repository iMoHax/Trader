package ru.trader.view.support.cells;

import javafx.collections.ObservableList;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.util.Callback;
import ru.trader.model.OrderModel;
import ru.trader.view.support.ViewUtils;

public class OrderDecoratedRow extends DecoratedRowFactory<OrderModel> {
    public OrderDecoratedRow() {
        super();
    }

    public OrderDecoratedRow(Callback<TableView<OrderModel>, TableRow<OrderModel>> decorated) {
        super(decorated);
    }

    @Override
    protected void doStyle(TableRow<OrderModel> row, OrderModel entry) {
        ObservableList<String> styles = row.getStyleClass();
        styles.remove(ViewUtils.ILLEGAL_ITEM_STYLE);
        if (entry != null && entry.isIllegal()){
            styles.add(ViewUtils.ILLEGAL_ITEM_STYLE);
        }
    }
}
