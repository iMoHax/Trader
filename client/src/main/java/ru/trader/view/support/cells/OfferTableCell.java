package ru.trader.view.support.cells;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import ru.trader.model.OfferModel;
import ru.trader.model.support.ModelBindings;

public class OfferTableCell<T> implements Callback<TableColumn<OfferModel, T>, TableCell<OfferModel, T>> {

    @Override
    public TableCell<OfferModel, T> call(TableColumn<OfferModel, T> param) {
        return new TableCell<OfferModel, T>(){
            private OfferModel o;

            @Override
            public void updateItem(T value, boolean empty) {
                super.updateItem(value, empty);
                if (!empty){
                    OfferModel offer = (OfferModel) getTableRow().getItem();
                    if (o != offer){
                        textProperty().unbind();
                        textProperty().bind(ModelBindings.asString(offer));
                        o = offer;
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
