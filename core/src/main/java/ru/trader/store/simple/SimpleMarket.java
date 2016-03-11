package ru.trader.store.simple;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.core.*;

import java.util.*;

public class SimpleMarket extends AbstractMarket {
    private final static Logger LOG = LoggerFactory.getLogger(SimpleMarket.class);

    protected Set<Place> systems;
    protected List<Item> items;
    protected List<Group> groups;

    //caching
    private final Map<Item,SimpleItemStat> sellItems = new HashMap<>();
    private final Map<Item,SimpleItemStat> buyItems = new HashMap<>();

    public SimpleMarket() {
        init();
    }

    protected void init() {
        systems = new TreeSet<>();
        items = new  ArrayList<>();
        groups = new ArrayList<>();
    }

    private Map<Item,SimpleItemStat> getItemCache(OFFER_TYPE offerType){
        switch (offerType) {
            case SELL: return sellItems;
            case BUY: return buyItems;
            default:
                throw new IllegalArgumentException("Wrong offer type: "+offerType);
        }
    }

    private void put(Map<Item, SimpleItemStat> cache, Offer offer){
        Item item = offer.getItem();
        SimpleItemStat entry = cache.get(item);
        if (entry==null){
            entry = new SimpleItemStat(item, offer.getType());
            cache.put(item, entry);
        }
        entry.put(offer);
    }

    private void remove(Map<Item, SimpleItemStat> cache, Offer offer){
        Item item = offer.getItem();
        SimpleItemStat entry = cache.get(item);
        if (entry!=null){
            entry.remove(offer);
            if (entry.isEmpty())
                cache.remove(item);
        }
    }

    @Override
    protected Place createPlace(String name, double x, double y, double z) {
        return new SimplePlace(name, x, y, z);
    }

    @Override
    protected Group createGroup(String name, GROUP_TYPE type) {
        return new SimpleGroup(name, type);
    }

    @Override
    protected Item createItem(String name, Group group) {
        return new SimpleItem(name, group);
    }

    @Override
    protected void addPlace(Place place) {
        systems.add(place);
        for (Vendor vendor : place.get()) {
            onAdd(vendor);
        }
    }

    @Override
    protected void removePlace(Place place) {
        systems.remove(place);
        for (Vendor vendor : place.get()) {
            onRemove(vendor);
        }
    }

    @Override
    protected void addGroup(Group group) {
        groups.add(group);
    }

    @Override
    protected void removeGroup(Group group) {
        groups.remove(group);
    }

    @Override
    protected void addItem(Item item) {
        items.add(item);
    }

    @Override
    protected void removeItem(Item item) {
        ItemStat stat = sellItems.get(item);
        for (Offer offer : stat.getOffers()) {
            SimpleVendor vendor = (SimpleVendor) offer.getVendor();
            vendor.removeOffer(offer);
        }
        stat = buyItems.get(item);
        for (Offer offer : stat.getOffers()) {
            SimpleVendor vendor = (SimpleVendor) offer.getVendor();
            vendor.removeOffer(offer);
        }
        items.remove(item);
        sellItems.remove(item);
        buyItems.remove(item);
    }

    @Override
    public Collection<Place> get() {
        return systems;
    }

    @Override
    public Collection<Group> getGroups() {
        return groups;
    }

    @Override
    public Collection<Item> getItems() {
        return items;

    }

    @Override
    public ItemStat getStat(OFFER_TYPE offerType, Item item) {
        ItemStat entry = getItemCache(offerType).get(item);
        if (entry == null){
            entry = new SimpleItemStat(item, offerType);
            getItemCache(offerType).put(item, (SimpleItemStat) entry);
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

    @Override
    protected void updateName(AbstractPlace place, String name) {
        LOG.trace("Replace system {} on change name", place);
        systems.remove(place);
        super.updateName(place, name);
        systems.add(place);
    }

}
