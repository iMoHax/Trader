package ru.trader.view.support.cells;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Callback;
import javafx.util.converter.DoubleStringConverter;
import ru.trader.model.OfferDescModel;


public class PriceCellImpl implements Callback<TableColumn<OfferDescModel, Double>, TableCell<OfferDescModel, Double>> {
    private final static String CSS_BAD = "bad";
    private final static String CSS_GOOD = "good";
    private final static String CSS_DIFF = "diff";

    @Override
    public TableCell<OfferDescModel, Double> call(TableColumn<OfferDescModel, Double> param) {
        return new PriceCell();
    }

    private class PriceCell extends EditingCell<OfferDescModel, Double> {
        protected PriceCell() {
            super(new DoubleStringConverter());
        }

        @Override
        protected void outText() {
            OfferDescModel offerDesc = (OfferDescModel) getTableRow().getItem();
            if (offerDesc!=null){
                double d = offerDesc.getDiff();
                TextFlow txt = new TextFlow();
                Text price = new Text(String.format("%.0f", offerDesc.getPrice()));
                Text diff = new Text(String.format(" (%+.0f)", d));
                diff.getStyleClass().add(CSS_DIFF);
                txt.getChildren().addAll(price, diff);
                this.getStyleClass().removeAll(CSS_BAD, CSS_GOOD);
                String cssClass = (d == 0 || Double.isNaN(d) ? "" : d * offerDesc.getOffer().getType().getOrder() > 0 ? CSS_BAD : CSS_GOOD );
                this.getStyleClass().add(cssClass);
                this.setText(null);
                this.setGraphic(txt);

            }
        }
    }




}