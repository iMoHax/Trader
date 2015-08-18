package ru.trader.view.support.cells;

import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;
import ru.trader.model.OrderModel;

public class OrderListCell implements Callback<ListView<OrderModel>, ListCell<OrderModel>> {
    private final boolean buy;

    public OrderListCell(boolean buy) {
        this.buy = buy;
    }

    @Override
    public ListCell<OrderModel> call(ListView<OrderModel> param){
        return new ListCell<OrderModel>(){
            private OrderModel o;

            @Override
            public void updateItem(OrderModel order, boolean empty) {
                super.updateItem(order, empty);
                if (!empty){
                    if (o != order){
                        textProperty().unbind();
                        textProperty().bind(order.asString(buy));
                        o = order;
                    }
                } else {
                    textProperty().unbind();
                    o = null;
                    setText(null);
                    setGraphic(null);
                }
            }


        };
    }
}
