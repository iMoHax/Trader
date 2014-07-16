package ru.trader.core;

import java.util.*;
import java.util.stream.Collectors;

public class SimpleMarket extends MarketSupport {
    protected Set<Vendor> vendors;
    protected List<Item> items;

    //caching
    private final Map<Item,ItemStat> sellItems = new HashMap<>();
    private final Map<Item,ItemStat> buyItems = new HashMap<>();

    public SimpleMarket() {
        init();
    }

    protected void init() {
        vendors = new TreeSet<>();
        items = new  ArrayList<>();
    }

    private Map<Item,ItemStat> getItemCache(OFFER_TYPE offerType){
        switch (offerType) {
            case SELL: return sellItems;
            case BUY: return buyItems;
            default:
                throw new IllegalArgumentException("Wrong offer type: "+offerType);
        }
    }

    private void put(Map<Item, ItemStat> cache, Offer offer){
        Item item = offer.getItem();
        ItemStat entry = cache.get(item);
        if (entry==null){
            entry = newItemStat(item, offer.getType());
            cache.put(item, entry);
        }
        entry.put(offer);
    }

    protected ItemStat newItemStat(Item item, OFFER_TYPE offerType){
        return new ItemStat(item, offerType);
    }

    private void remove(Map<Item, ItemStat> cache, Offer offer){
        Item item = offer.getItem();
        ItemStat entry = cache.get(item);
        if (entry!=null){
            entry.remove(offer);
            if (entry.getOffersCount()==0)
                cache.remove(item);
        }
    }

    @Override
    public void addVendor(Vendor vendor) {
        vendors.add(vendor);
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
    public void removeVendor(Vendor vendor) {
        vendors.remove(vendor);
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
    protected void addItem(Item item) {
        if (!items.contains(item))
            items.add(item);
    }

    @Override
    protected void removeItem(Item item) {
        items.remove(item);
        sellItems.remove(item);
        buyItems.remove(item);
    }

    @Override
    protected Collection<Vendor> getVendors() {
        return vendors;
    }

    @Override
    protected Collection<Vendor> getVendors(OFFER_TYPE offerType, Item item) {
        List<Vendor> result = null;
        ItemStat entry = getItemCache(offerType).get(item);
        if (entry!=null){
            result = entry.getOffers()
                          .stream()
                          .map(Offer::getVendor)
                          .collect(Collectors.toList());
        }

        return result!=null ? Collections.unmodifiableCollection(result) : null;
    }

    @Override
    public ItemStat getStat(OFFER_TYPE offerType, Item item) {
        ItemStat entry = getItemCache(offerType).get(item);
        return entry!=null ? entry : newItemStat(item, offerType);
    }

    @Override
    protected Collection<Item> getItemList() {
        return items;

    }

    @Override
    protected void onAdd(Offer offer) {
        put(getItemCache(offer.getType()), offer);
    }

    @Override
    protected void onRemove(Offer offer) {
        remove(getItemCache(offer.getType()), offer);
    }

    @Override
    public void addItems(Collection<? extends Item> items) {
        this.items.addAll(items);
    }

}
