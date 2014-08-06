package ru.trader.model;

import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;
import ru.trader.model.support.ModelBindings;

import java.util.List;

public class OrderModel {

    private final OfferDescModel offer;

    private final LongProperty count;
    private final ObjectProperty<OfferModel> buyer =  new SimpleObjectProperty<>();
    private long max;
    private DoubleProperty profit;
    private DoubleProperty distance;
    private DoubleProperty bestProfit;

    public OrderModel(OfferDescModel offer) {
        this.offer = offer;
        this.count = new SimpleLongProperty(0){
            @Override
            public void setValue(Number v) {
                if (max > 0 && v.longValue() > max){
                    super.setValue(max);
                } else {
                    super.setValue(v);
                }
            }
        };
    }

    public OrderModel(OfferDescModel offer, double balance, long limit) {
        this(offer);
        this.max = Math.min(limit, (long) Math.floor(balance / offer.getPrice()));
    }

    public OfferModel getOffer() {
        return offer.getOffer();
    }

    public ReadOnlyStringProperty nameProperty() {
        return offer.nameProperty();
    }

    public ReadOnlyDoubleProperty priceProperty() {
        return offer.priceProperty();
    }

    public ReadOnlyObjectProperty<OfferModel> bestProperty(){
        return offer.bestBuyProperty();
    }

    public long getCount() {
        return count.get();
    }

    public LongProperty countProperty() {
        return count;
    }

    public void setCount(long count) {
        this.count.set(count);
    }

    public ReadOnlyDoubleProperty profitProperty() {
        if (profit == null){
            profit = new SimpleDoubleProperty(0);
            profit.bind(ModelBindings.diff(buyer, offer.getOffer().priceProperty()).multiply(count));
        }
        return profit;
    }

    public ReadOnlyDoubleProperty bestProfitProperty() {
        if (bestProfit == null){
            bestProfit = new SimpleDoubleProperty(0);
            bestProfit.bind(offer.profitProperty().multiply(max));
        }
        return bestProfit;
    }

    public ObservableValue<Double> getProfit(OfferModel buyer) {
        return buyer.priceProperty().subtract(offer.getOffer().priceProperty()).multiply(max).asObject();
    }

    public double getProfit() {
        return profitProperty().get();
    }

    public ReadOnlyObjectProperty<OfferModel> buyerProperty() {
        return buyer;
    }

    public void setBuyer(OfferModel buyer) {
        this.buyer.set(buyer);
        if (distance!=null) distance.set(getVendor().getDistance(buyer.getVendor()));
    }

    public OfferModel getBuyer() {
        return buyer.get();
    }

    public VendorModel getVendor() {
        return offer.getOffer().getVendor();
    }

    public long getMax() {
        return max;
    }

    public void setMax(long max) {
        this.max = max;
    }

    public List<OfferModel> getBuyers(){
        return offer.getBuyer();
    }

    public ReadOnlyDoubleProperty distanceProperty() {
        if (distance == null){
            OfferModel buyOffer = getBuyer();
            distance = new SimpleDoubleProperty(buyOffer!=null ? getVendor().getDistance(buyOffer.getVendor()) : Double.NaN);
        }
        return distance;
    }


}
