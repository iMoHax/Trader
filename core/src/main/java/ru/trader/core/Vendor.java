package ru.trader.core;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.graph.Connectable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public abstract class Vendor implements Comparable<Vendor>, Connectable<Vendor> {
    private final static Logger LOG = LoggerFactory.getLogger(Vendor.class);

    public abstract String getName();
    public abstract void setName(String name);

    public abstract double getX();
    public abstract void setX(double x);

    public abstract double getY();
    public abstract void setY(double y);

    public abstract double getZ();
    public abstract void setZ(double z);


    protected abstract Collection<Offer> getOffers();
    protected abstract Collection<Item> getItems(OFFER_TYPE offerType);
    protected abstract Offer getOffer(OFFER_TYPE offerType, Item item);
    protected abstract boolean hasOffer(OFFER_TYPE offerType, Item item);
    protected abstract void  addOffer(Offer offer);
    protected abstract void  removeOffer(Offer offer);

    protected Collection<Offer> getOffers(OFFER_TYPE offerType){
        List<Offer> offers = getOffers()
                .stream()
                .filter(offer -> offer.hasType(offerType))
                .sorted()
                .collect(Collectors.toList());
        return Collections.unmodifiableCollection(offers);
    }

    public final Collection<Offer> getAllOffers(){
        return Collections.unmodifiableCollection(getOffers());
    }

    public final Collection<Offer> getAllSellOffers(){
        return Collections.unmodifiableCollection(getOffers(OFFER_TYPE.SELL));
    }

    public final Collection<Offer> getAllBuyOffers(){
        return Collections.unmodifiableCollection(getOffers(OFFER_TYPE.BUY));
    }

    public final Offer getSell(Item item){
        return getOffer(OFFER_TYPE.SELL, item);
    }

    public final Offer getBuy(Item item){
        return getOffer(OFFER_TYPE.BUY, item);
    }

    public final boolean hasSell(Item item){
        return hasOffer(OFFER_TYPE.SELL, item);
    }

    public final boolean hasBuy(Item item){
        return hasOffer(OFFER_TYPE.BUY, item);
    }

    public final void add(Offer offer){
        LOG.trace("Add offer {} to vendor {}", offer, this);
        offer.setVendor(this);
        addOffer(offer);
    }

    public final void remove(Offer offer){
        LOG.trace("Remove offer {} from vendor {}", offer, this);
        assert this.equals(offer.getVendor());
        removeOffer(offer);
    }

    public final Collection<Item> getSellItems() {
        return getItems(OFFER_TYPE.SELL);
    }

    public final Collection<Item> getBuyItems() {
        return getItems(OFFER_TYPE.BUY);
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public int compareTo(@NotNull Vendor other) {
        Objects.requireNonNull(other, "Not compare with null");
        if (this == other) return 0;
        String name = getName();
        String otherName = other.getName();
        return name != null ? otherName != null ? name.compareTo(otherName) : -1 : 0;
    }

    @Override
    public double getDistance(Vendor other){
        return getDistance(other.getX(), other.getY(), other.getZ());
    }

    @Override
    public boolean canRefill() {
        return !getAllSellOffers().isEmpty() || !getAllBuyOffers().isEmpty();
    }

    public double getDistance(double x, double y, double z){
        return Math.sqrt(Math.pow(x - getX(), 2) + Math.pow(y - getY(), 2) + Math.pow(z - getZ(), 2));
    }


}
