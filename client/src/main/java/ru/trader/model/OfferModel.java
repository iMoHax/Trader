package ru.trader.model;

import javafx.beans.property.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.core.OFFER_TYPE;
import ru.trader.core.Offer;

public class OfferModel{
    private final static Logger LOG = LoggerFactory.getLogger(OfferModel.class);
    private final MarketModel market;
    private final Offer offer;

    private DoubleProperty price;


    OfferModel(Offer offer, MarketModel market) {
        this.market = market;
        this.offer = offer;
    }


    public double getPrice() {return price != null ? price.get() : offer.getPrice();}

    public void setPrice(double value) {
        if (getPrice() == value) return;
        LOG.info("Change price offer {} to {}", offer, value);
        market.updatePrice(this, value);
        if (price != null) price.set(value);
    }

    public ReadOnlyDoubleProperty priceProperty() {
        if (price == null) {
            price = new SimpleDoubleProperty(offer.getPrice());
        }
        return price;
    }

    public ItemModel getItem() {
        return market.asModel(offer.getItem());
    }

    public VendorModel getVendor() {
        return market.asModel(offer.getVendor());
    }

    public OFFER_TYPE getType(){
        return offer.getType();
    }

    public boolean isModel(Offer offer) {
        return this.offer == offer;
    }

    public boolean hasVendor(VendorModel vendor){
        return offer.getVendor().equals(vendor.getVendor());
    }

    public boolean hasItem(ItemModel item){
        return offer.getItem().equals(item.getItem());
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
            sb.append('}');
            return sb.toString();
        }
        return offer.toString();
    }

    Offer getOffer() {
        return offer;
    }

    public String toVString(){
        return offer.toVString();
    }

    public String toIString(){
        return offer.toIString();
    }

}
