package ru.trader.view.support.cells;

import javafx.application.Platform;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.text.Text;
import javafx.util.Callback;
import javafx.util.StringConverter;
import org.controlsfx.glyphfont.Glyph;
import org.controlsfx.glyphfont.GlyphFontRegistry;
import ru.trader.model.support.VendorUpdater;

public class EditOfferCell extends TextFieldCell<VendorUpdater.FakeOffer, Double> {
    private final static String CSS_CHANGE = "change";
    private final static String CSS_ADD = "add";
    private final static String CSS_REMOVE = "remove";
    private final static String CSS_RESET_ICON = "reset";
    private boolean isSell;

    public EditOfferCell(StringConverter<Double> converter, boolean isSell) {
        super(converter);
        this.isSell = isSell;
    }

    public static Callback<TableColumn<VendorUpdater.FakeOffer,Double>, TableCell<VendorUpdater.FakeOffer,Double>> forTable(final StringConverter<Double> converter, boolean isSell) {
        return list -> new EditOfferCell(converter, isSell);
    }

    @Override
    protected void outItem() {
        VendorUpdater.FakeOffer offer = (VendorUpdater.FakeOffer) getTableRow().getItem();
        double d = isSell? offer.getSprice() - offer.getOldSprice() : offer.getBprice() - offer.getOldBprice();
        getStyleClass().removeAll(CSS_ADD, CSS_CHANGE, CSS_REMOVE);
        if (d !=0 ){
            HBox hBox = new HBox();
            hBox.prefWidthProperty().bind(getTableColumn().widthProperty());
            Text nTxt = new Text(getConverter().toString(isSell ? offer.getSprice() : offer.getBprice()));
            if (isSell){
                getStyleClass().add(offer.isNewSell()? CSS_ADD : offer.isRemoveSell() ? CSS_REMOVE : CSS_CHANGE);
            } else {
                getStyleClass().add(offer.isNewBuy()? CSS_ADD : offer.isRemoveBuy() ? CSS_REMOVE : CSS_CHANGE);
            }
            hBox.getChildren().add(nTxt);
            if (!offer.isRemoveBuy() && !offer.isRemoveSell() && !offer.isNewBuy() && !offer.isNewSell()){
                Text diff = new Text(String.format(" (%+.0f)", d));
                hBox.getChildren().add(diff);
            }
            Glyph glyph = (Glyph) GlyphFontRegistry.glyph("FontAwesome|REMOVE");
            glyph.addEventFilter(MouseEvent.MOUSE_CLICKED, (e) -> {
                Platform.runLater(() -> {
                    if (isSell) {
                        offer.setSprice(offer.getOldSprice());
                    } else {
                        offer.setBprice(offer.getOldBprice());
                    }
                });
                e.consume();
            });
            HBox icon = new HBox(glyph);
            icon.getStyleClass().add(CSS_RESET_ICON);
            HBox.setHgrow(icon, Priority.ALWAYS);
            hBox.getChildren().add(icon);
            setText(null);
            setGraphic(hBox);
        } else {
            setText(getItemText());
            setGraphic(null);
        }


    }
}
