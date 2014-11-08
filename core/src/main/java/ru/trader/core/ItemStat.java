package ru.trader.core;

import java.util.NavigableSet;

public interface ItemStat {

    OFFER_TYPE getType();
    Item getItem();

    Offer getMin();
    double getAvg();
    Offer getMax();

    Offer getBest();
    NavigableSet<Offer> getOffers();
    boolean isEmpty();
}
