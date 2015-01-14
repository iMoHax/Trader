package ru.trader.store.berkeley;

import ru.trader.core.*;
import ru.trader.store.berkeley.entities.BDBOffer;

public class OfferProxy extends AbstractOffer {
    private final BDBOffer offer;
    private Item item;
    private BDBStore store;
    private Vendor vendor;

    public OfferProxy(BDBOffer offer, BDBStore store) {
        this.offer = offer;
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
        if (item == null){
            item = store.getItemAccessor().get(offer.getItemId());
        }
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OfferProxy)) return false;

        OfferProxy that = (OfferProxy) o;

        if (!offer.equals(that.offer)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return offer.hashCode();
    }
}
