package ru.trader.store.berkeley;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.core.*;
import ru.trader.store.berkeley.entities.BDBGroup;
import ru.trader.store.berkeley.entities.BDBItem;
import ru.trader.store.berkeley.entities.BDBPlace;

import java.util.*;

public class BDBMarket extends AbstractMarket {
    private final static Logger LOG = LoggerFactory.getLogger(BDBMarket.class);
    private final BDBStore store;

    //caching
    private final Map<Item,BDBItemStat> sellItems = new HashMap<>();
    private final Map<Item,BDBItemStat> buyItems = new HashMap<>();

    public BDBMarket(BDBStore store) {
        this.store = store;
        store.setMarket(this);
    }

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
        } else {
            entry.put(offer);
        }
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
        for (Vendor vendor : place.get()) {
            onRemove(vendor);
        }
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
        sellItems.remove(item);
        buyItems.remove(item);
    }

    @Override
    public Place get(String name) {
        return store.getPlaceAccessor().get(name);
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
    public Item getItem(String name) {
        return store.getItemAccessor().get(name);
    }

    @Override
    public Collection<Item> getItems() {
        return store.getItemAccessor().getAll();
    }

    @Override
    public Collection<Vendor> getVendors(boolean includeTransit) {
        Collection<Vendor> vendors = store.getVendorAccessor().getAll();
        if (includeTransit){
            store.getPlaceAccessor().getAll().stream().map(Place::asTransit).forEach(vendors::add);
        }
        return vendors;
    }

    @Override
    public ItemStat getStat(OFFER_TYPE type, Item item) {
        ItemStat entry = getItemCache(type).get(item);
        if (entry == null){
            entry = new BDBItemStat((ItemProxy) item, type, store);
            getItemCache(type).put(item, (BDBItemStat) entry);
        }
        return entry;
    }

    @Override
    protected void onAdd(Vendor vendor) {
        LOG.trace("Cached on add vendor {}", vendor);
        Collection<Offer> offers = vendor.getAllSellOffers();
        for (Offer offer : offers) {
            put(sellItems, offer);
        }
        offers = vendor.getAllBuyOffers();
        for (Offer offer : offers) {
            put(buyItems, offer);
        }
    }

    @Override
    protected void onRemove(Vendor vendor) {
        LOG.trace("Remove cache of vendor {}", vendor);
        Collection<Offer> offers = vendor.getAllSellOffers();
        for (Offer offer : offers) {
            remove(sellItems, offer);
        }
        offers = vendor.getAllBuyOffers();
        for (Offer offer : offers) {
            remove(buyItems, offer);
        }
    }

    @Override
    protected void onAdd(Offer offer) {
        LOG.trace("Cached on add offer {}", offer);
        put(getItemCache(offer.getType()), offer);
    }

    @Override
    protected void onRemove(Offer offer) {
        LOG.trace("Remove cache of offer {}", offer);
        remove(getItemCache(offer.getType()), offer);
    }

}
