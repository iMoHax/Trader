package ru.trader.core;

import java.util.Collection;

public interface Market {

    void add(Place place);
    Place addPlace(String name, double x, double y, double z);
    void remove(Place place);

    void add(Group group);
    Group addGroup(String name, GROUP_TYPE type);
    void remove(Group group);

    void add(Item item);
    Item addItem(String name, Group group);
    void remove(Item item);

    Collection<Place> get();
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
}
