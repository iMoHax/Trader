package ru.trader.model;

import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.model.support.ModelBindings;

public class OfferDescModel extends ItemDescModelImpl implements ItemDescModel{
    private final static Logger LOG = LoggerFactory.getLogger(OfferDescModel.class);

    protected DoubleProperty diff;
    protected final OfferModel offer;
    protected DoubleProperty maxProfit;
    protected DoubleProperty minProfit;
    protected DoubleProperty avgProfit;

    public OfferDescModel(OfferModel offer, ItemStatModel statSell, ItemStatModel statBuy) {
        super(offer.getItem(), statSell, statBuy);
        this.offer = offer;
    }

    public ReadOnlyDoubleProperty priceProperty() {
       return offer.priceProperty();
    }

    public double getPrice(){
        return offer.getPrice();
    }

    public ReadOnlyDoubleProperty profitProperty() {
        if (maxProfit == null){
            maxProfit = new SimpleDoubleProperty(0);
            switch (offer.getType()) {
                case SELL: maxProfit.bind(ModelBindings.diff(bestBuyProperty(), priceProperty()));
                    break;
                case BUY:  maxProfit.bind(ModelBindings.diff(priceProperty(), bestSellProperty()));
                    break;
            }
        }
        return maxProfit;
    }

    public ReadOnlyDoubleProperty avgProfitProperty() {
        if (avgProfit == null){
            avgProfit = new SimpleDoubleProperty(0);
            switch (offer.getType()) {
                case SELL: avgProfit.bind(avgBuyProperty().subtract(priceProperty()));
                    break;
                case BUY:  avgProfit.bind(priceProperty().subtract(avgSellProperty()));
                    break;
            }
        }
        return avgProfit;
    }

    public ReadOnlyDoubleProperty minProfitProperty() {
        if (minProfit == null){
            minProfit = new SimpleDoubleProperty(0);
            switch (offer.getType()) {
                case SELL: minProfit.bind(ModelBindings.diff(minBuyProperty(), priceProperty()));
                    break;
                case BUY:  minProfit.bind(ModelBindings.diff(priceProperty(), minSellProperty()));
                    break;
            }
        }
        return minProfit;
    }

    public ReadOnlyDoubleProperty diffProperty(){
        if (diff == null){
            diff = new SimpleDoubleProperty(0);
            switch (offer.getType()) {
                case SELL: diff.bind(Bindings.subtract(priceProperty(), avgSellProperty()));
                    break;
                case BUY:  diff.bind(Bindings.subtract(priceProperty(), avgBuyProperty()));
                    break;
            }
        }
        return diff;
    }

    public double getDiff(){
        return diffProperty().get();
    }

    public OfferModel getOffer(){
        return offer;
    }



}
