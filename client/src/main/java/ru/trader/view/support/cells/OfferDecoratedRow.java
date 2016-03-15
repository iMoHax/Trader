package ru.trader.view.support.cells;

import javafx.collections.ObservableList;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.util.Callback;
import ru.trader.model.OfferModel;
import ru.trader.view.support.ViewUtils;

public class OfferDecoratedRow extends DecoratedRowFactory<OfferModel> {
    public OfferDecoratedRow() {
        super();
    }

    public OfferDecoratedRow(Callback<TableView<OfferModel>, TableRow<OfferModel>> decorated) {
        super(decorated);
    }

    @Override
    protected void doStyle(TableRow<OfferModel> row, OfferModel entry) {
        ObservableList<String> styles = row.getStyleClass();
        styles.remove(ViewUtils.ILLEGAL_ITEM_STYLE);
        if (entry != null && entry.isIllegal()){
            styles.add(ViewUtils.ILLEGAL_ITEM_STYLE);
        }
    }
}
