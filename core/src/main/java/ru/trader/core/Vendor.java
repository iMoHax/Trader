package ru.trader.core;

import ru.trader.analysis.graph.Connectable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;

public interface Vendor extends Connectable<Vendor> {

    String getName();
    void setName(String name);

    default String getFullName(){
        return getPlace().getName()+": "+getName();
    }

    Place getPlace();

    double getDistance();
    void setDistance(double distance);

    FACTION getFaction();
    void setFaction(FACTION faction);

    GOVERNMENT getGovernment();
    void setGovernment(GOVERNMENT government);

    void add(SERVICE_TYPE service);
    void remove(SERVICE_TYPE service);
    boolean has(SERVICE_TYPE service);
    Collection<SERVICE_TYPE> getServices();

    STATION_TYPE getType();
    void setType(STATION_TYPE type);

    ECONOMIC_TYPE getEconomic();
    void setEconomic(ECONOMIC_TYPE economic);

    ECONOMIC_TYPE getSubEconomic();
    void setSubEconomic(ECONOMIC_TYPE economic);

    LocalDateTime getModifiedTime();
    void setModifiedTime(LocalDateTime time);

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


    static  double LS = 0.00000003169;

    default double getDistance(Vendor other){
        Place place = getPlace();
        Place otherPlace = other.getPlace();
        if (!place.equals(otherPlace)){
            return getPlace().getDistance(other.getPlace()) + other.getDistance() * LS;
        }
        return (getDistance() + other.getDistance() + Math.abs(getDistance() - other.getDistance())) * LS / 2;
    }

    default boolean canRefill(){
        return has(SERVICE_TYPE.REFUEL);
    }

    default void clear(){
        Collection<Offer> offers = new ArrayList<>(getAllSellOffers());
        offers.addAll(getAllBuyOffers());
        for (Offer offer : offers) {
            remove(offer);
        }
    }

    default boolean isTransit(){
        return this instanceof TransitVendor;
    }
}
