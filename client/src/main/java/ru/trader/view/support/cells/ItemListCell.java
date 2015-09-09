package ru.trader.view.support.cells;

import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;
import ru.trader.model.ItemModel;

public class ItemListCell implements Callback<ListView<ItemModel>, ListCell<ItemModel>> {

    @Override
    public ListCell<ItemModel> call(ListView<ItemModel> param){
        return new ListCell<ItemModel>(){
            private ItemModel i;

            @Override
            public void updateItem(ItemModel item, boolean empty) {
                super.updateItem(item, empty);
                if (!empty){
                    if (i != item){
                        textProperty().unbind();
                        textProperty().bind(item.nameProperty());
                        i = item;
                    }
                } else {
                    textProperty().unbind();
                    i = null;
                    setText(null);
                    setGraphic(null);
                }
            }
        };
    }
}
