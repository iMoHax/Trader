package ru.trader.store.berkeley;

import ru.trader.core.*;
import ru.trader.store.berkeley.entities.BDBOffer;
import ru.trader.store.berkeley.entities.BDBVendor;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class VendorProxy extends AbstractVendor {
    private final BDBVendor vendor;
    private final BDBStore store;
    private Place place;
    protected Map<Long, Offer> sell;
    protected Map<Long, Offer> buy;

    public VendorProxy(BDBVendor vendor, BDBStore store) {
        this.vendor = vendor;
        this.store = store;
        init();
    }

    private void init(){
        sell = new ConcurrentHashMap<>(20, 0.9f, 2);
        buy = new ConcurrentHashMap<>(20, 0.9f, 2);
        for (Offer offer : store.getOfferAccessor().getAllByType(vendor.getId(), OFFER_TYPE.SELL)) {
            sell.put(((ItemProxy)offer.getItem()).getId(), offer);
        }
        for (Offer offer : store.getOfferAccessor().getAllByType(vendor.getId(), OFFER_TYPE.BUY)) {
            buy.put(((ItemProxy)offer.getItem()).getId(), offer);
        }
    }

    private Map<Long, Offer> getCache(OFFER_TYPE type){
        return type == OFFER_TYPE.SELL ? sell : buy;
    }

    protected long getId(){
        return vendor.getId();
    }

    protected BDBVendor getEntity(){
        return vendor;
    }

    protected void setPlace(PlaceProxy place) {
        vendor.setPlace(place.getId());
        store.getVendorAccessor().update(vendor);
        this.place = place;
    }

    @Override
    protected Offer createOffer(OFFER_TYPE type, Item item, double price, long count) {
        return new OfferProxy(new BDBOffer(type, ((ItemProxy)item).getId(), price, count, vendor.getId()), store);
    }

    @Override
    protected void updateName(String name) {
        vendor.setName(name);
        store.getVendorAccessor().update(vendor);
    }

    @Override
    protected void updateDistance(double distance) {
        vendor.setDistance(distance);
        store.getVendorAccessor().update(vendor);
    }

    @Override
    protected void addService(SERVICE_TYPE service) {
        vendor.add(service);
        store.getVendorAccessor().update(vendor);
    }

    @Override
    protected void removeService(SERVICE_TYPE service) {
        vendor.remove(service);
        store.getVendorAccessor().update(vendor);
    }

    @Override
    public Collection<SERVICE_TYPE> getServices() {
        return vendor.getServices();
    }

    @Override
    protected void addOffer(Offer offer) {
        OfferProxy oProxy = ((OfferProxy)offer);
        oProxy.setVendor(this);
        store.getOfferAccessor().put(oProxy.getEntity());
        getCache(offer.getType()).put(((ItemProxy)oProxy.getItem()).getId(), oProxy);
    }

    @Override
    protected void removeOffer(Offer offer) {
        OfferProxy oProxy = ((OfferProxy)offer);
        getCache(offer.getType()).remove(((ItemProxy)oProxy.getItem()).getId());
        store.getOfferAccessor().delete(oProxy.getEntity());
    }


    @Override
    public String getName() {
        return vendor.getName();
    }

    @Override
    public Place getPlace() {
        if (place == null){
            place = store.getPlaceAccessor().get(vendor.getPlaceId());
        }
        return place;
    }

    @Override
    public double getDistance() {
        return vendor.getDistance();
    }

    @Override
    public boolean has(SERVICE_TYPE service) {
        return vendor.has(service);
    }

    @Override
    public Collection<Offer> get(OFFER_TYPE type) {
        return getCache(type).values();
    }

    @Override
    public Offer get(OFFER_TYPE type, Item item) {
        return getCache(type).get(((ItemProxy)item).getId());
    }

    @Override
    public boolean has(OFFER_TYPE type, Item item) {
        return getCache(type).containsKey(((ItemProxy)item).getId());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VendorProxy)) return false;

        VendorProxy that = (VendorProxy) o;

        if (!vendor.equals(that.vendor)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return vendor.hashCode();
    }
}
