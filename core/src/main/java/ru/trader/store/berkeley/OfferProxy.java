package ru.trader.store.berkeley;

import ru.trader.core.*;
import ru.trader.store.berkeley.entities.BDBOffer;

public class OfferProxy extends AbstractOffer {
    private final BDBOffer offer;
    private final Item item;
    private BDBStore store;
    private Vendor vendor;

    public OfferProxy(BDBOffer offer, BDBStore store) {
        this.offer = offer;
        this.item = store.getItemAccessor().get(offer.getItemId());
        this.store = store;
    }

    protected long getId() {
        return offer.getId();
    }

    protected BDBOffer getEntity() {
        return offer;
    }

    protected void setVendor(VendorProxy vendor) {
        offer.setVendor(vendor.getId());
        this.vendor = vendor;
        store.getOfferAccessor().update(offer);
    }

    @Override
    protected void updatePrice(double price) {
        offer.setPrice(price);
        store.getOfferAccessor().update(offer);
    }

    @Override
    protected void updateCount(long count) {
        offer.setCount(count);
        store.getOfferAccessor().update(offer);
    }

    @Override
    public Item getItem() {
        return item;
    }

    @Override
    public OFFER_TYPE getType() {
        return offer.getType();
    }

    @Override
    public Vendor getVendor() {
        if (vendor == null){
            vendor = store.getVendorAccessor().get(offer.getVendorId());
        }
        return vendor;
    }

    @Override
    public double getPrice() {
        return offer.getPrice();
    }

    @Override
    public long getCount() {
        return offer.getCount();
    }

}
