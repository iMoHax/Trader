package ru.trader.core;

import java.util.Collection;


public interface Market {
    boolean isChange();

    ItemStat getStat(Offer offer);

    ItemStat getStat(OFFER_TYPE type, Item item);

    ItemStat getStatSell(Item item);

    ItemStat getStatBuy(Item item);

    Collection<Offer> getSell(Item item);

    Collection<Offer> getBuy(Item item);

    void add(Vendor vendor);

    void add(Item item);

    void remove(Vendor vendor);

    void remove(Item item);

    Collection<Vendor> get();

    void add(Vendor vendor, Offer offer);

    void remove(Vendor vendor, Offer offer);

    Collection<Item> getItems();

    void addVendors(Collection<? extends Vendor> vendors);

    void addItems(Collection<? extends Item> items);

    void updatePrice(Offer offer, double price);

    void setChange(boolean change);

    Collection<Order> getTop(int limit, double balance, long max);

    void updateName(Vendor vendor, String name);

    void updateName(Item item, String name);

    void updatePosition(Vendor vendor, double x, double y, double z);
}
