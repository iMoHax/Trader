package ru.trader.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public abstract class MarketSupport implements Market {
    private final static Logger LOG = LoggerFactory.getLogger(MarketSupport.class);

    protected abstract void addVendor(Vendor vendor);
    protected abstract void removeVendor(Vendor vendor);
    protected abstract void addItem(Item item);
    protected abstract void removeItem(Item item);
    protected abstract Collection<Vendor> getVendors();
    protected abstract Collection<Item> getItemList();

    @Override
    public abstract ItemStat getStat(OFFER_TYPE offerType, Item item);

    private boolean change;

    @Override
    public boolean isChange() {
        return change;
    }

    protected Collection<Vendor> getVendors(OFFER_TYPE offerType, Item item){
        List<Vendor> offers = getVendors()
                .stream()
                .filter(vendor -> vendor.hasOffer(offerType, item))
                .collect(Collectors.toList());
        return Collections.unmodifiableCollection(offers);
    }

    protected Collection<Offer> getOffers(OFFER_TYPE offerType, Item item){
        ItemStat entry = getStat(offerType, item);
        return entry!=null ? Collections.unmodifiableCollection(entry.getOffers()) : null;
    }

    @Override
    public final ItemStat getStat(Offer offer){
        return getStat(offer.getType(), offer.getItem());
    }

    @Override
    public final ItemStat getStatSell(Item item){
        return getStat(OFFER_TYPE.SELL, item);
    }

    @Override
    public final ItemStat getStatBuy(Item item){
        return getStat(OFFER_TYPE.BUY, item);
    }

    @Override
    public final Collection<Offer> getSell(Item item){
        return getOffers(OFFER_TYPE.SELL,item);
    }

    @Override
    public final Collection<Offer> getBuy(Item item){
        return getOffers(OFFER_TYPE.BUY,item);
    }

    @Override
    public final void add(Vendor vendor){
        LOG.debug("Add vendor {} to market {}", vendor, this);
        change = true;
        addVendor(vendor);
    }

    @Override
    public final void add(Item item){
        LOG.debug("Add item {} to market {}", item, this);
        change = true;
        addItem(item);
    }

    @Override
    public final void remove(Vendor vendor){
        LOG.debug("Remove vendor {} from market {}", vendor, this);
        change = true;
        removeVendor(vendor);
    }

    @Override
    public final void remove(Item item){
        LOG.debug("Remove item {} from market {}", item, this);
        change = true;
        removeItem(item);
    }

    @Override
    public final Collection<Vendor> get(){
        return Collections.unmodifiableCollection(getVendors());
    }

    // Execute on add or remove offer
    protected void onAdd(Offer offer){}
    protected void onRemove(Offer offer){}

    @Override
    public final void add(Vendor vendor, Offer offer){
        LOG.debug("Add offer {} to vendor {}", offer, vendor);
        change = true;
        vendor.add(offer);
        onAdd(offer);
    }

    @Override
    public final void remove(Vendor vendor, Offer offer){
        LOG.debug("Remove offer {} from vendor {}", offer, vendor);
        change = true;
        vendor.remove(offer);
        onRemove(offer);
    }

    @Override
    public final Collection<Item> getItems(){
        return Collections.unmodifiableCollection(getItemList());
    }

    @Override
    public void addVendors(Collection<? extends Vendor> vendors) {
        change = true;
        for (Vendor vendor : vendors) {
            add(vendor);
        }
    }

    @Override
    public void addItems(Collection<? extends Item> items) {
        change = true;
        for (Item item : items) {
            add(item);
        }
    }

    @Override
    public void updatePrice(Offer offer, double price){
        change = true;
        getStat(offer).update(offer, price);
    }

    @Override
    public void setChange(boolean change) {
        this.change = change;
    }
}

