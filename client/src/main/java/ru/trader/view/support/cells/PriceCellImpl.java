package ru.trader.view.support.cells;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.util.Callback;
import javafx.util.converter.DoubleStringConverter;
import ru.trader.model.OfferModel;


public class PriceCellImpl implements Callback<TableColumn<OfferModel, Double>, TableCell<OfferModel, Double>> {
    private final static String CSS_BAD = "bad";
    private final static String CSS_GOOD = "good";
    private final static String CSS_DIFF = "diff";

    @Override
    public TableCell<OfferModel, Double> call(TableColumn<OfferModel, Double> param) {
        return new PriceCell();
    }

    private class PriceCell extends EditingCell<OfferModel, Double> {
        protected PriceCell() {
            super(new DoubleStringConverter());
        }

        @Override
        protected void outText() {
            OfferModel offer = (OfferModel) getTableRow().getItem();
            if (offer!=null){
                double d = offer.getDiff();
                HBox txt = new HBox();
                Text price = new Text(String.format("%.0f", offer.getPrice()));
                Text diff = new Text(String.format(" (%+.0f)", d));
                diff.getStyleClass().add(CSS_DIFF);
                txt.getChildren().addAll(price, diff);
                this.getStyleClass().removeAll(CSS_BAD, CSS_GOOD);
                String cssClass = (d == 0 || Double.isNaN(d) ? "" : d * offer.getType().getOrder() > 0 ? CSS_BAD : CSS_GOOD );
                this.getStyleClass().add(cssClass);
                this.setText(null);
                this.setGraphic(txt);

            }
        }
    }




}