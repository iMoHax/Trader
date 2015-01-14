package ru.trader.store.berkeley;

import ru.trader.core.*;
import ru.trader.store.berkeley.entities.BDBOffer;
import ru.trader.store.berkeley.entities.BDBVendor;

import java.util.Collection;

public class VendorProxy extends AbstractVendor {
    private final BDBVendor vendor;
    private final BDBStore store;
    private Place place;

    public VendorProxy(BDBVendor vendor, BDBStore store) {
        this.vendor = vendor;
        this.store = store;
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
    }

    @Override
    protected void removeOffer(Offer offer) {
        store.getOfferAccessor().delete(((OfferProxy) offer).getEntity());
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
        return store.getOfferAccessor().getAllByType(vendor.getId(), type);
    }

    @Override
    public Offer get(OFFER_TYPE type, Item item) {
        return store.getOfferAccessor().get(vendor.getId(), type, ((ItemProxy)item).getId());
    }

    @Override
    public boolean has(OFFER_TYPE type, Item item) {
        return store.getOfferAccessor().has(vendor.getId(), type, ((ItemProxy)item).getId());
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
