package ru.trader.view.support.cells;

import javafx.collections.ObservableList;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;
import ru.trader.model.OfferModel;
import ru.trader.model.OrderModel;
import ru.trader.view.support.ViewUtils;

public class OfferDecoratedListCell extends DecoratedListCellFactory<OfferModel> {
    public OfferDecoratedListCell() {
        this(new OfferListCell());
    }

    public OfferDecoratedListCell(boolean asItem) {
        this(new OfferListCell(asItem));
    }

    public OfferDecoratedListCell(Callback<ListView<OfferModel>, ListCell<OfferModel>> decorated) {
        super(decorated);
    }

    @Override
    void doStyle(ListCell<OfferModel> cell, OfferModel item) {
        ObservableList<String> styles = cell.getStyleClass();
        styles.remove(ViewUtils.ILLEGAL_ITEM_STYLE);
        if (item != null && item.isIllegal()){
            styles.add(ViewUtils.ILLEGAL_ITEM_STYLE);
        }
    }
}
