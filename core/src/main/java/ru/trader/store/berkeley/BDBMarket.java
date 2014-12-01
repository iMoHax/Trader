package ru.trader.store.berkeley;

import ru.trader.core.*;
import ru.trader.store.berkeley.entities.BDBGroup;
import ru.trader.store.berkeley.entities.BDBItem;
import ru.trader.store.berkeley.entities.BDBPlace;

import java.util.*;

public class BDBMarket extends AbstractMarket {
    private final BDBStore store;

    //caching
    private final Map<Item,BDBItemStat> sellItems = new HashMap<>();
    private final Map<Item,BDBItemStat> buyItems = new HashMap<>();


    private Map<Item,BDBItemStat> getItemCache(OFFER_TYPE offerType){
        switch (offerType) {
            case SELL: return sellItems;
            case BUY: return buyItems;
            default:
                throw new IllegalArgumentException("Wrong offer type: "+offerType);
        }
    }

    private void put(Map<Item, BDBItemStat> cache, Offer offer){
        Item item = offer.getItem();
        BDBItemStat entry = cache.get(item);
        if (entry == null){
            entry = new BDBItemStat((ItemProxy) item, offer.getType(), store);
            cache.put(item, entry);
        }
        entry.put(offer);
    }

    private void remove(Map<Item, BDBItemStat> cache, Offer offer){
        Item item = offer.getItem();
        BDBItemStat entry = cache.get(item);
        if (entry!=null){
            entry.remove(offer);
            if (entry.isEmpty())
                cache.remove(item);
        }
    }

    public BDBMarket(BDBStore store) {
        this.store = store;
    }

    @Override
    protected Place createPlace(String name, double x, double y, double z) {
        return new PlaceProxy(new BDBPlace(name, x, y, z), store);
    }

    @Override
    protected Group createGroup(String name, GROUP_TYPE type) {
        return new BDBGroup(name, type);
    }

    @Override
    protected Item createItem(String name, Group group) {
        return new ItemProxy(new BDBItem(name, group.getName()), store);
    }

    @Override
    protected void addPlace(Place place) {
        store.getPlaceAccessor().put(((PlaceProxy) place).getEntity());
    }

    @Override
    protected void removePlace(Place place) {
        store.getPlaceAccessor().delete(((PlaceProxy) place).getEntity());
    }

    @Override
    protected void addGroup(Group group) {
        store.getGroupAccessor().put((BDBGroup) group);
    }

    @Override
    protected void removeGroup(Group group) {
        store.getGroupAccessor().delete((BDBGroup) group);
    }

    @Override
    protected void addItem(Item item) {
        store.getItemAccessor().put(((ItemProxy) item).getEntity());
    }

    @Override
    protected void removeItem(Item item) {
        store.getItemAccessor().delete(((ItemProxy) item).getEntity());
    }

    @Override
    public Collection<Place> get() {
        return store.getPlaceAccessor().getAll();
    }

    @Override
    public Collection<Group> getGroups() {
        return store.getGroupAccessor().getAll();
    }

    @Override
    public Collection<Item> getItems() {
        return store.getItemAccessor().getAll();
    }

    @Override
    public ItemStat getStat(OFFER_TYPE type, Item item) {
        //TODO: добавить
        return null;
    }

}
