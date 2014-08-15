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

    private String name;
    private double x;
    private double y;
    private double z;

    protected abstract Collection<Offer> getOffers();
    protected abstract Collection<Item> getItems(OFFER_TYPE offerType);
    protected abstract Offer getOffer(OFFER_TYPE offerType, Item item);
    protected abstract boolean hasOffer(OFFER_TYPE offerType, Item item);
    protected abstract void  addOffer(Offer offer);
    protected abstract void  removeOffer(Offer offer);

    protected Vendor() {
    }

    protected Vendor(String name) {
        this.name = name;
    }

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public int compareTo(@NotNull Vendor other) {
        Objects.requireNonNull(other, "Not compare with null");
        if (this == other) return 0;
        return name != null ? other.name != null ? name.compareTo(other.name) : -1 : 0;
    }

    @Override
    public double getDistance(Vendor other){
        return getDistance(other.x, other.y, other.z);
    }

    @Override
    public boolean canRefill() {
        return !getAllSellOffers().isEmpty() || !getAllBuyOffers().isEmpty();
    }

    public double getDistance(double x, double y, double z){
        return Math.sqrt(Math.pow(x - this.x, 2) + Math.pow(y-this.y, 2) + Math.pow(z - this.z, 2));
    }


    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }
}
