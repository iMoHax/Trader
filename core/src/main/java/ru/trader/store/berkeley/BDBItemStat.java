package ru.trader.store.berkeley;

import ru.trader.core.AbstractItemStat;
import ru.trader.core.Item;
import ru.trader.core.OFFER_TYPE;
import ru.trader.core.Offer;

import java.util.NavigableSet;


//TODO: implement
public class BDBItemStat extends AbstractItemStat {
    private final ItemProxy item;
    private final OFFER_TYPE type;
    private final BDBStore store;

    public BDBItemStat(ItemProxy item, OFFER_TYPE type, BDBStore store) {
        this.item = item;
        this.type = type;
        this.store = store;
    }

    synchronized void put(Offer offer){
    }

    synchronized void remove(Offer offer){
    }


    @Override
    public OFFER_TYPE getType() {
        return type;
    }

    @Override
    public Item getItem() {
        return item;
    }

    @Override
    public Offer getMin() {
        return null;
    }

    @Override
    public double getAvg() {
        return 0;
    }

    @Override
    public Offer getMax() {
        return null;
    }

    @Override
    public Offer getBest() {
        return null;
    }

    @Override
    public NavigableSet<Offer> getOffers() {
        return null;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }
}
