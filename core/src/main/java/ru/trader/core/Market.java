package ru.trader.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Optional;

public interface Market {

    void add(Place place);
    Place addPlace(String name, double x, double y, double z);
    void remove(Place place);
    default Place get(String name){
        Optional<Place> place = get().stream().filter(p -> name.equals(p.getName())).findFirst();
        return place.isPresent() ? place.get() : null;
    }

    void add(Group group);
    Group addGroup(String name, GROUP_TYPE type);
    void remove(Group group);

    void add(Item item);
    Item addItem(String name, Group group);
    void remove(Item item);

    Collection<Place> get();
    default Collection<Vendor> getVendors(){
        return new PlacesWrapper(get());
    }
    Collection<Group> getGroups();
    Collection<Item> getItems();
    ItemStat getStat(OFFER_TYPE type, Item item);

    boolean isChange();
    void commit();

    default ItemStat getStat(Offer offer){
        return getStat(offer.getType(), offer.getItem());
    }

    default ItemStat getStatSell(Item item){
        return getStat(OFFER_TYPE.SELL, item);
    }

    default ItemStat getStatBuy(Item item){
        return getStat(OFFER_TYPE.BUY, item);
    }

    default Collection<Offer> getSell(Item item){
        return getStatSell(item).getOffers();
    }

    default Collection<Offer> getBuy(Item item){
        return getStatBuy(item).getOffers();
    }

    default void add(Market market){
        // add groups
        Collection<Group> groups = market.getGroups();
        HashMap<Group, Group> mapGroups = new HashMap<>(groups.size(), 0.9f);
        for (Group group : groups) {
            Group nGroup = this.addGroup(group.getName(), group.getType());
            mapGroups.put(group, nGroup);
        }
        // add items
        Collection<Item> items = market.getItems();
        HashMap<Item, Item> mapItems = new HashMap<>(items.size(), 0.9f);
        for (Item item : items) {
            Item nItem = this.addItem(item.getName(), mapGroups.get(item.getGroup()));
            mapItems.put(item, nItem);
        }
        mapGroups.clear();
        // add places and vendors
        for (Place place : market.get()) {
            Place nPlace = this.addPlace(place.getName(), place.getX(), place.getY(), place.getZ());
            for (Vendor vendor : place.get()) {
                Vendor nVendor = nPlace.addVendor(vendor.getName());
                nVendor.setDistance(vendor.getDistance());
                // add services
                for (SERVICE_TYPE service : vendor.getServices()) {
                    nVendor.add(service);
                }
                // add offers
                for (Offer offer : vendor.getAllBuyOffers()) {
                    nVendor.addOffer(offer.getType(), mapItems.get(offer.getItem()), offer.getPrice(), offer.getCount());
                }
                for (Offer offer : vendor.getAllSellOffers()) {
                    nVendor.addOffer(offer.getType(), mapItems.get(offer.getItem()), offer.getPrice(), offer.getCount());
                }
            }
        }
    }
    
    default void clear(){
        clear(true, true, true, true, true);
    }

    default void clearGroups(){
        clear(true, false, false, true, true);
    }

    default void clearItems(){
        clear(true, false, false, true, false);
    }

    default void clearPlaces(){
        clear(false, false, true, false, false);
    }

    default void clearVendors(){
        clear(false, true, false, false, false);
    }

    default void clearOffers(){
        clear(true, false, false, false, false);
    }


    default void clear(boolean offers, boolean vendors, boolean places, boolean items, boolean groups){
        if (places){
            Collection<Place> p = new ArrayList<>(get());
            for (Place place : p) {
                remove(place);
            }
        } else {
            if (vendors || offers){
                for (Place place : get()) {
                    if (vendors) {
                        place.clear();
                    } else {
                        place.clearOffers();
                    }
                }
            }
        }
        if (items){
            Collection<Item> i = new ArrayList<>(getItems());
            for (Item item : i) {
                remove(item);
            }
        }
        if (groups){
            Collection<Group> g = new ArrayList<>(getGroups());
            for (Group group : g) {
                remove(group);
            }
        }
    }

}
