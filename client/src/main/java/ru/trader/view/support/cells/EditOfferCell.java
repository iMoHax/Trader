package ru.trader.view.support.cells;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.util.Callback;
import javafx.util.StringConverter;
import ru.trader.controllers.VendorEditorController;

public class EditOfferCell extends TextFieldCell<VendorEditorController.FakeOffer, Double> {
    private final static String CSS_CHANGE = "change";
    private final static String CSS_ADD = "add";
    private final static String CSS_REMOVE = "remove";
    private boolean isSell;

    public EditOfferCell(StringConverter<Double> converter, boolean isSell) {
        super(converter);
        this.isSell = isSell;
    }

    public static Callback<TableColumn<VendorEditorController.FakeOffer,Double>, TableCell<VendorEditorController.FakeOffer,Double>> forTable(final StringConverter<Double> converter, boolean isSell) {
        return list -> new EditOfferCell(converter, isSell);
    }

    @Override
    protected void outItem() {
        VendorEditorController.FakeOffer offer = (VendorEditorController.FakeOffer) getTableRow().getItem();
        double d = isSell? offer.getSprice() - offer.getOldSprice() : offer.getBprice() - offer.getOldBprice();
        getStyleClass().removeAll(CSS_ADD, CSS_CHANGE, CSS_REMOVE);
        if (d!=0){
            HBox hBox = new HBox();
            Text nTxt = new Text(getConverter().toString(isSell ? offer.getSprice() : offer.getBprice()));
            Text diff = new Text(String.format(" (%+.0f)", d));
            if (isSell){
                getStyleClass().add(offer.isNewSell()? CSS_ADD : offer.isRemoveSell() ? CSS_REMOVE : CSS_CHANGE);
            } else {
                getStyleClass().add(offer.isNewBuy()? CSS_ADD : offer.isRemoveBuy() ? CSS_REMOVE : CSS_CHANGE);
            }
            hBox.getChildren().add(nTxt);
            if (!offer.isRemoveBuy() && !offer.isRemoveSell() && !offer.isNewBuy() && !offer.isNewSell()){
                hBox.getChildren().add(diff);
            }
            setText(null);
            setGraphic(hBox);
        } else {
            setText(getItemText());
            setGraphic(null);
        }


    }
}
