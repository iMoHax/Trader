package ru.trader.analysis;

import ru.trader.analysis.graph.Connectable;
import ru.trader.core.*;

import java.util.Collection;
import java.util.stream.Collectors;

public class FilteredVendor implements Vendor {
    private final Vendor vendor;
    private final VendorFilter filter;

    public FilteredVendor(Vendor vendor, VendorFilter filter) {
        this.vendor = vendor;
        this.filter = filter;
    }

    @Override
    public String getName() {
        return vendor.getName();
    }

    @Override
    public void setName(String name) {
        vendor.setName(name);
    }

    @Override
    public String getFullName() {
        return vendor.getFullName();
    }

    @Override
    public Place getPlace() {
        return vendor.getPlace();
    }

    @Override
    public double getDistance() {
        return vendor.getDistance();
    }

    @Override
    public void setDistance(double distance) {
        vendor.setDistance(distance);
    }

    @Override
    public FACTION getFaction() {
        return vendor.getFaction();
    }

    @Override
    public void setFaction(FACTION faction) {
        vendor.setFaction(faction);
    }

    @Override
    public GOVERNMENT getGovernment() {
        return vendor.getGovernment();
    }

    @Override
    public void setGovernment(GOVERNMENT government) {
        vendor.setGovernment(government);
    }

    @Override
    public void add(SERVICE_TYPE service) {
        vendor.add(service);
    }

    @Override
    public void remove(SERVICE_TYPE service) {
        vendor.remove(service);
    }

    @Override
    public boolean has(SERVICE_TYPE service) {
        return vendor.has(service);
    }

    @Override
    public Collection<SERVICE_TYPE> getServices() {
        return vendor.getServices();
    }

    @Override
    public void add(Offer offer) {
        vendor.add(offer);
    }

    @Override
    public Offer addOffer(OFFER_TYPE type, Item item, double price, long count) {
        return vendor.addOffer(type, item, price, count);
    }

    @Override
    public void remove(Offer offer) {
        vendor.remove(offer);
    }

    @Override
    public Collection<Offer> get(OFFER_TYPE type) {
        if (vendor.isTransit()) return vendor.get(type);
        return vendor.get(type).stream().filter(o -> !filter.isFiltered(o)).collect(Collectors.toList());
    }

    @Override
    public Offer get(OFFER_TYPE type, Item item) {
        Offer offer = vendor.get(type, item);
        if (offer == null || filter.isFiltered(offer)) return null;
        return offer;
    }

    @Override
    public boolean has(OFFER_TYPE type, Item item) {
        return vendor.has(type, item) && !filter.isFiltered(vendor, item, type);
    }

    @Override
    public Collection<Offer> getAllSellOffers() {
        if (vendor.isTransit()) return vendor.getAllSellOffers();
        return vendor.getAllSellOffers().stream().filter(o -> !filter.isFiltered(o)).collect(Collectors.toList());
    }

    @Override
    public Collection<Offer> getAllBuyOffers() {
        if (vendor.isTransit()) return vendor.getAllBuyOffers();
        return vendor.getAllBuyOffers().stream().filter(o -> !filter.isFiltered(o)).collect(Collectors.toList());
    }

    @Override
    public Offer getSell(Item item) {
        Offer offer = vendor.getSell(item);
        if (offer == null || filter.isFiltered(offer)) return null;
        return offer;
    }

    @Override
    public Offer getBuy(Item item) {
        Offer offer = vendor.getBuy(item);
        if (offer == null || filter.isFiltered(offer)) return null;
        return offer;
    }

    @Override
    public boolean hasSell(Item item) {
        return vendor.hasSell(item) && !filter.isFiltered(vendor, item, OFFER_TYPE.SELL);
    }

    @Override
    public boolean hasBuy(Item item) {
        return vendor.hasBuy(item) && !filter.isFiltered(vendor, item, OFFER_TYPE.BUY);
    }

    @Override
    public double getDistance(Vendor other) {
        return vendor.getDistance(other);
    }

    @Override
    public boolean canRefill() {
        return vendor.canRefill();
    }

    @Override
    public void clear() {
        vendor.clear();
    }

    @Override
    public boolean isTransit() {
        return vendor.isTransit();
    }

    @Override
    public int compareTo(Connectable<Vendor> o) {
        return vendor.compareTo(o);
    }

    @Override
    public String toString() {
        return vendor.toString();
    }

    public Vendor unwrap(){
        return vendor;
    }
}
