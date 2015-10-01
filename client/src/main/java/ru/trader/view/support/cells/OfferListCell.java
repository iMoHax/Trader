package ru.trader.view.support.cells;

import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;
import ru.trader.model.OfferModel;
import ru.trader.model.support.ModelBindings;

public class OfferListCell implements Callback<ListView<OfferModel>, ListCell<OfferModel>> {
    private final boolean asItem;

    public OfferListCell(){
        this(false);
    }

    public OfferListCell(boolean toItemString){
        asItem = toItemString;
    }

    @Override
    public ListCell<OfferModel> call(ListView<OfferModel> param){
        return new ListCell<OfferModel>(){
            private OfferModel o;

            @Override
            public void updateItem(OfferModel offer, boolean empty) {
                super.updateItem(offer, empty);
                if (!empty){
                    if (o != offer){
                        textProperty().unbind();
                        textProperty().bind(asItem ? ModelBindings.asItemString(offer) : ModelBindings.asString(offer));
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
