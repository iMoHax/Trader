package ru.trader.core;

import ru.trader.analysis.graph.Connectable;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;

public class TransitVendor implements Vendor {
    protected Place place;

    public TransitVendor(Place place) {
        this.place = place;
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public void setName(String name) {
        throw new UnsupportedOperationException("Is fake vendor, change unsupported");
    }

    @Override
    public FACTION getFaction() {
        return FACTION.NONE;
    }

    @Override
    public void setFaction(FACTION faction) {
        throw new UnsupportedOperationException("Is fake vendor, change unsupported");
    }

    @Override
    public GOVERNMENT getGovernment() {
        return GOVERNMENT.NONE;
    }

    @Override
    public void setGovernment(GOVERNMENT government) {
        throw new UnsupportedOperationException("Is fake vendor, change unsupported");
    }

    @Override
    public STATION_TYPE getType() {
        return STATION_TYPE.STARPORT;
    }

    @Override
    public void setType(STATION_TYPE government) {
        throw new UnsupportedOperationException("Is fake vendor, change unsupported");
    }

    @Override
    public ECONOMIC_TYPE getEconomic() {
        return ECONOMIC_TYPE.NONE;
    }

    @Override
    public void setEconomic(ECONOMIC_TYPE government) {
        throw new UnsupportedOperationException("Is fake vendor, change unsupported");
    }

    @Override
    public ECONOMIC_TYPE getSubEconomic() {
        return ECONOMIC_TYPE.NONE;
    }

    @Override
    public void setSubEconomic(ECONOMIC_TYPE government) {
        throw new UnsupportedOperationException("Is fake vendor, change unsupported");
    }

    @Override
    public LocalDateTime getModifiedTime() {
        return LocalDateTime.now();
    }

    @Override
    public void setModifiedTime(LocalDateTime time) {
        throw new UnsupportedOperationException("Is fake vendor, change unsupported");
    }

    @Override
    public Place getPlace() {
        return place;
    }

    @Override
    public double getDistance() {
        return 0;
    }

    @Override
    public void setDistance(double distance) {
        throw new UnsupportedOperationException("Is fake vendor, change unsupported");
    }

    @Override
    public void add(SERVICE_TYPE service) {
        throw new UnsupportedOperationException("Is fake vendor, change unsupported");
    }

    @Override
    public void remove(SERVICE_TYPE service) {
        throw new UnsupportedOperationException("Is fake vendor, change unsupported");
    }

    @Override
    public boolean has(SERVICE_TYPE service) {
        return false;
    }

    @Override
    public Collection<SERVICE_TYPE> getServices() {
        return Collections.emptyList();
    }

    @Override
    public Offer addOffer(OFFER_TYPE type, Item item, double price, long count) {
        throw new UnsupportedOperationException("Is fake vendor, change unsupported");
    }

    @Override
    public void add(Offer offer) {
        throw new UnsupportedOperationException("Is fake vendor, change unsupported");
    }

    @Override
    public void remove(Offer offer) {
        throw new UnsupportedOperationException("Is fake vendor, change unsupported");
    }

    @Override
    public Collection<Offer> get(OFFER_TYPE type) {
        return Collections.emptyList();
    }

    @Override
    public Offer get(OFFER_TYPE type, Item item) {
        return null;
    }

    @Override
    public boolean has(OFFER_TYPE type, Item item) {
        return false;
    }

    @Override
    public int compareTo(Connectable<Vendor> o) {
        double d = getDistance((Vendor)o);
        return d == 0 ? 0 : d > 0 ? 1 : -1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TransitVendor)) return false;
        TransitVendor that = (TransitVendor) o;
        return place.equals(that.place);
    }

    @Override
    public boolean canRefill() {
        return getPlace().canRefill();
    }

    @Override
    public int hashCode() {
        return place.hashCode();
    }

    @Override
    public String toString() {
        return "Transit "+place;
    }
}
