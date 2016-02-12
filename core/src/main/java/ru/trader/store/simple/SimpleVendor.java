package ru.trader.store.simple;

import ru.trader.core.*;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SimpleVendor extends AbstractVendor {
    private String name;
    private Place place;
    private double distance;
    private EnumSet<SERVICE_TYPE> services = EnumSet.noneOf(SERVICE_TYPE.class);
    private FACTION faction;
    private GOVERNMENT government;
    private STATION_TYPE type;
    private ECONOMIC_TYPE economic;
    private ECONOMIC_TYPE subEconomic;
    private LocalDateTime modified;

    protected Map<Item, Offer> sell;
    protected Map<Item, Offer> buy;

    public SimpleVendor() {
        initOffers();
    }

    public SimpleVendor(String name) {
        this.name = name;
        initOffers();
    }

    public SimpleVendor(String name, double x, double y, double z) {
        this(name);
        place = new SimplePlace(name, x, y, z, this);
    }

    protected void initOffers(){
        sell = new ConcurrentHashMap<>(20, 0.9f, 2);
        buy = new ConcurrentHashMap<>(20, 0.9f, 2);
    }

    @Override
    protected Offer createOffer(OFFER_TYPE type, Item item, double price, long count) {
        return new SimpleOffer(type, item, price, count);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    protected void updateName(String name) {
        this.name = name;
    }


    @Override
    public FACTION getFaction() {
        return faction;
    }

    @Override
    protected void updateFaction(FACTION faction) {
        this.faction = faction;
    }

    @Override
    public GOVERNMENT getGovernment() {
        return government;
    }

    @Override
    protected void updateGovernment(GOVERNMENT government) {
        this.government = government;
    }

    @Override
    public STATION_TYPE getType() {
        return type;
    }

    @Override
    protected void updateType(STATION_TYPE type) {
        this.type = type;
    }

    @Override
    public ECONOMIC_TYPE getEconomic() {
        return economic;
    }

    @Override
    protected void updateEconomic(ECONOMIC_TYPE economic) {
        this.economic = economic;
    }

    @Override
    public ECONOMIC_TYPE getSubEconomic() {
        return subEconomic;
    }

    @Override
    protected void updateSubEconomic(ECONOMIC_TYPE economic) {
        this.subEconomic = economic;
    }

    @Override
    public Place getPlace() {
        return place;
    }

    protected void setPlace(Place place){
        assert this.place == null;
        this.place = place;
    }

    @Override
    public double getDistance() {
        return distance;
    }

    @Override
    public void updateDistance(double distance) {
        this.distance = distance;
    }

    @Override
    public void addService(SERVICE_TYPE service) {
        services.add(service);
    }

    @Override
    public void removeService(SERVICE_TYPE service) {
        services.remove(service);
    }

    @Override
    public boolean has(SERVICE_TYPE service) {
        return services.contains(service);
    }

    @Override
    public Collection<SERVICE_TYPE> getServices() {
        return services;
    }

    @Override
    public Collection<Offer> get(OFFER_TYPE offerType) {
        switch (offerType) {
            case SELL: return sell.values();
            case BUY: return buy.values();
        }
        throw new IllegalArgumentException("Wrong offer type: "+offerType);
    }

    @Override
    public Offer get(OFFER_TYPE offerType, Item item) {
        switch (offerType) {
            case SELL: return sell.get(item);
            case BUY: return buy.get(item);
        }
        throw new IllegalArgumentException("Wrong offer type: "+offerType);
    }

    @Override
    public boolean has(OFFER_TYPE offerType, Item item) {
        switch (offerType) {
            case SELL: return sell.containsKey(item);
            case BUY: return buy.containsKey(item);
        }
        throw new IllegalArgumentException("Wrong offer type: "+offerType);
    }

    @Override
    protected void addOffer(Offer offer) {
        if (offer instanceof SimpleOffer){
            ((SimpleOffer) offer).setVendor(this);
        }
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

    @Override
    public LocalDateTime getModifiedTime() {
        return modified;
    }

    @Override
    protected void updateModifiedTime(LocalDateTime time) {
        this.modified = time;
    }
}
