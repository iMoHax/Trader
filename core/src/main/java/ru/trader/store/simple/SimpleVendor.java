package ru.trader.store.simple;

import ru.trader.core.Item;
import ru.trader.core.OFFER_TYPE;
import ru.trader.core.Offer;
import ru.trader.core.Vendor;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SimpleVendor extends Vendor {
    private String name;
    private double x;
    private double y;
    private double z;

    protected Map<Item, Offer> sell;
    protected Map<Item, Offer> buy;

    public SimpleVendor() {
        initOffers();
    }

    public SimpleVendor(String name) {
        this.name = name;
        initOffers();
    }

    protected void initOffers(){
        sell = new ConcurrentHashMap<>(20, 0.9f, 2);
        buy = new ConcurrentHashMap<>(20, 0.9f, 2);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public double getX() {
        return x;
    }

    @Override
    public void setX(double x) {
        this.x = x;
    }

    @Override
    public double getY() {
        return y;
    }

    @Override
    public void setY(double y) {
        this.y = y;
    }

    @Override
    public double getZ() {
        return z;
    }

    @Override
    public void setZ(double z) {
        this.z = z;
    }

    @Override
    protected Collection<Offer> getOffers(OFFER_TYPE offerType) {
        switch (offerType) {
            case SELL: return sell.values();
            case BUY: return buy.values();
        }
        throw new IllegalArgumentException("Wrong offer type: "+offerType);
    }

    @Override
    protected Collection<Offer> getOffers() {
        ArrayList<Offer> offers = new ArrayList<>(sell.values());
        offers.addAll(buy.values());
        return offers;
    }

    @Override
    protected Collection<Item> getItems(OFFER_TYPE offerType) {
        switch (offerType) {
            case SELL: return sell.keySet();
            case BUY: return buy.keySet();
        }
        throw new IllegalArgumentException("Wrong offer type: "+offerType);
    }

    @Override
    protected Offer getOffer(OFFER_TYPE offerType, Item item) {
        switch (offerType) {
            case SELL: return sell.get(item);
            case BUY: return buy.get(item);
        }
        throw new IllegalArgumentException("Wrong offer type: "+offerType);
    }

    @Override
    protected boolean hasOffer(OFFER_TYPE offerType, Item item) {
        switch (offerType) {
            case SELL: return sell.containsKey(item);
            case BUY: return buy.containsKey(item);
        }
        throw new IllegalArgumentException("Wrong offer type: "+offerType);
    }

    @Override
    protected void addOffer(Offer offer) {
        switch (offer.getType()) {
            case SELL: sell.put(offer.getItem(), offer);
                break;
            case BUY: buy.put(offer.getItem(), offer);
                break;
        }
    }

    @Override
    protected void removeOffer(Offer offer) {
        switch (offer.getType()) {
            case SELL: sell.remove(offer.getItem(), offer);
                break;
            case BUY: buy.remove(offer.getItem(), offer);
                break;
        }
    }

}
