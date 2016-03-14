package ru.trader.view.support.cells;

import javafx.collections.ObservableList;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;
import ru.trader.model.OrderModel;
import ru.trader.view.support.ViewUtils;

public class OrderDecoratedListCell extends DecoratedListCellFactory<OrderModel> {
    public OrderDecoratedListCell(boolean isBuy) {
        this(new OrderListCell(isBuy));
    }

    public OrderDecoratedListCell(Callback<ListView<OrderModel>, ListCell<OrderModel>> decorated) {
        super(decorated);
    }

    @Override
    void doStyle(ListCell<OrderModel> cell, OrderModel item) {
        ObservableList<String> styles = cell.getStyleClass();
        styles.remove(ViewUtils.ILLEGAL_ITEM_STYLE);
        if (item != null && item.isIllegal()){
            styles.add(ViewUtils.ILLEGAL_ITEM_STYLE);
        }
    }
}
