package ru.trader.core;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;

public interface Vendor extends Comparable<Vendor> {

    String getName();
    void setName(String name);

    Place getPlace();

    double getDistance();
    void setDistance(double distance);

    void add(SERVICE_TYPE service);
    void remove(SERVICE_TYPE service);
    boolean has(SERVICE_TYPE service);

    void add(Offer offer);
    Offer addOffer(OFFER_TYPE type, Item item, double price, long count);
    void remove(Offer offer);

    Collection<Offer> get(OFFER_TYPE type);
    Offer get(OFFER_TYPE type, Item item);
    boolean has(OFFER_TYPE type, Item item);

    default Collection<Offer> getAllSellOffers(){
        return get(OFFER_TYPE.SELL);
    }

    default Collection<Offer> getAllBuyOffers(){
        return get(OFFER_TYPE.BUY);
    }

    default Offer getSell(Item item){
        return get(OFFER_TYPE.SELL, item);
    }

    default Offer getBuy(Item item){
        return get(OFFER_TYPE.BUY, item);
    }

    default boolean hasSell(Item item){
        return has(OFFER_TYPE.SELL, item);
    }

    default boolean hasBuy(Item item){
        return has(OFFER_TYPE.BUY, item);
    }


    @Override
    default int compareTo(@NotNull Vendor other) {
        Objects.requireNonNull(other, "Not compare with null");
        if (this == other) return 0;
        int cmp = Double.compare(getDistance(), other.getDistance());
        if (cmp!=0) return cmp;
        String name = getName();
        String otherName = other.getName();
        return name != null ? otherName != null ? name.compareTo(otherName) : -1 : 0;
    }
}
