package ru.trader.model;

import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.core.OFFER_TYPE;
import ru.trader.core.Offer;
import ru.trader.core.Place;
import ru.trader.core.Vendor;
import ru.trader.model.support.ModelBindings;

import java.util.List;

public class OfferModel {
    private final static Logger LOG = LoggerFactory.getLogger(OfferModel.class);
    private final Offer offer;
    private final MarketModel market;
    private final ItemModel item;

    private DoubleProperty price;
    private LongProperty count;

    private DoubleProperty diff;
    private DoubleProperty maxProfit;
    private DoubleProperty minProfit;
    private DoubleProperty avgProfit;

    private SystemModel asModel(Place system){
        return market.getModeler().get(system);
    }
    private StationModel asModel(Vendor station){
        return market.getModeler().get(station);
    }

    public OfferModel(Offer offer, MarketModel market) {
        this(offer, market.getModeler().get(offer.getItem()), market);
    }

    public OfferModel(Offer offer, ItemModel item, MarketModel market) {
        this.offer = offer;
        this.market = market;
        this.item = item;
    }

    Offer getOffer(){
        return offer;
    }

    public ItemModel getItem(){
        return item;
    }

    public boolean isIllegal() {
        return offer.isIllegal();
    }

    public OFFER_TYPE getType(){
        return offer.getType();
    }

    public StationModel getStation(){
        return asModel(offer.getVendor());
    }

    public SystemModel getSystem(){
        return asModel(offer.getVendor().getPlace());
    }

    public double getPrice() {return price != null ? price.get() : offer.getPrice();}

    public void setPrice(double value) {
        double old = getPrice();
        if (old == value) return;
        LOG.info("Change price offer {} to {}", offer, value);
        offer.setPrice(value);
        if (price != null) price.set(value);
        refresh();
        market.getNotificator().sendPriceChange(this, old, value);
    }

    public ReadOnlyDoubleProperty priceProperty() {
        if (price == null) {
            price = new SimpleDoubleProperty(offer.getPrice());
        }
        return price;
    }

    public long getCount() {return count != null ? count.get() : offer.getCount();}

    public void setCount(long value) {
        if (getCount() == value) return;
        LOG.info("Change count offer {} to {}", offer, value);
        offer.setCount(value);
        if (count != null) count.set(value);
    }

    public ReadOnlyLongProperty countProperty() {
        if (count == null) {
            count = new SimpleLongProperty(offer.getCount());
        }
        return count;
    }

    public ReadOnlyStringProperty nameProperty() {
        return item.nameProperty();
    }

    public ReadOnlyDoubleProperty avgBuyProperty() {
        return item.avgBuyProperty();
    }

    public ReadOnlyObjectProperty<OfferModel> minBuyProperty() {
        return item.minBuyProperty();
    }

    public ReadOnlyObjectProperty<OfferModel> maxBuyProperty() {
        return item.maxBuyProperty();
    }

    public ReadOnlyObjectProperty<OfferModel> bestBuyProperty() {
        return item.bestBuyProperty();
    }

    public ReadOnlyDoubleProperty avgSellProperty() {
        return item.avgSellProperty();
    }

    public ReadOnlyObjectProperty<OfferModel> minSellProperty() {
        return item.minSellProperty();
    }

    public ReadOnlyObjectProperty<OfferModel> maxSellProperty() {
        return item.maxSellProperty();
    }

    public ReadOnlyObjectProperty<OfferModel> bestSellProperty() {
        return item.bestSellProperty();
    }

    public List<OfferModel> getSeller() {
        return item.getSeller();
    }

    public List<OfferModel> getBuyer() {
        return item.getBuyer();
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

    public String toStationString(){
        return String.format("%.0f (%s - %s (%.0f Ls))", getPrice(), getSystem().getName(), getStation().getName(), getStation().getDistance());
    }

    public String toItemString(){
        return String.format("%s (%.0f)", getItem().getName(), getPrice());
    }

    public void refresh(){
        item.refresh(offer.getType());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OfferModel)) return false;
        OfferModel that = (OfferModel) o;
        return offer.equals(that.offer);

    }

    @Override
    public int hashCode() {
        return offer.hashCode();
    }

    @Override
    public String toString() {
        if (LOG.isTraceEnabled()){
            final StringBuilder sb = new StringBuilder("OfferModel{");
            sb.append("priceProp=").append(price);
            sb.append(", offer=").append(offer.toString());
            sb.append(", item=").append(item.toString());
            sb.append('}');
            return sb.toString();
        }
        return offer.toString();
    }


}
