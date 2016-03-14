package ru.trader.core;

import ru.trader.analysis.MarketUtils;
import ru.trader.analysis.graph.Connectable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;

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

    default Stream<Offer> getSellOffers(){
        return MarketUtils.getOffers(getAllSellOffers());
    }

    default Collection<Offer> getAllBuyOffers(){
        return get(OFFER_TYPE.BUY);
    }

    default Stream<Offer> getBuyOffers(){
        return MarketUtils.getOffers(getAllBuyOffers());
    }

    default Offer getSell(Item item){
        Offer offer = get(OFFER_TYPE.SELL, item);
        return MarketUtils.isIncorrect(offer) ? null : offer;
    }

    default Offer getBuy(Item item){
        Offer offer = get(OFFER_TYPE.BUY, item);
        return MarketUtils.isIncorrect(offer) ? null : offer;
    }

    default boolean hasSell(Item item){
        return has(OFFER_TYPE.SELL, item) && !MarketUtils.isIncorrect(this, item, OFFER_TYPE.SELL);
    }

    default boolean hasBuy(Item item){
        return has(OFFER_TYPE.BUY, item) && !MarketUtils.isIncorrect(this, item, OFFER_TYPE.BUY);
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
