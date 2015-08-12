package ru.trader.store.berkeley;

import ru.trader.core.AbstractPlace;
import ru.trader.core.Vendor;
import ru.trader.store.berkeley.entities.BDBPlace;
import ru.trader.store.berkeley.entities.BDBVendor;

import java.util.Collection;

public class PlaceProxy extends AbstractPlace {
    private final BDBPlace place;
    private BDBStore store;
    private long count;

    public PlaceProxy(BDBPlace place, BDBStore store) {
        this.place = place;
        this.store = store;
        count = -1;
    }

    public PlaceProxy(BDBPlace place, BDBMarket market, BDBStore store) {
        this(place, store);
        setMarket(market);
    }

    protected long getId(){
        return place.getId();
    }

    protected BDBPlace getEntity(){
        return place;
    }

    @Override
    protected Vendor createVendor(String name) {
        return new VendorProxy(new BDBVendor(name, place.getId()), store);
    }

    @Override
    protected void updateName(String name) {
        place.setName(name);
        store.getPlaceAccessor().update(place);
    }

    @Override
    protected void updatePosition(double x, double y, double z) {
        place.setPosition(x, y, z);
        store.getPlaceAccessor().update(place);
    }

    @Override
    protected void addVendor(Vendor vendor) {
        store.getVendorAccessor().put(((VendorProxy)vendor).getEntity());
        if (count != -1) count++;
    }

    @Override
    protected void removeVendor(Vendor vendor) {
        store.getVendorAccessor().delete(((VendorProxy)vendor).getEntity());
        if (count != -1) count--;
    }

    @Override
    public String getName() {
        return place.getName();
    }

    @Override
    public double getX() {
        return place.getX();
    }

    @Override
    public double getY() {
        return place.getY();
    }

    @Override
    public double getZ() {
        return place.getZ();
    }

    @Override
    public Collection<Vendor> get() {
        return store.getVendorAccessor().getAllByPlace(place.getId());
    }

    @Override
    public long count() {
        if (count == -1){
            count = store.getVendorAccessor().count(place.getId());
        }
        return count;
    }

    @Override
    public boolean isEmpty() {
        if (count > -1){
            return count == 0;
        }
        return !store.getVendorAccessor().contains(place.getId());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PlaceProxy)) return false;

        PlaceProxy that = (PlaceProxy) o;

        if (!place.equals(that.place)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return place.hashCode();
    }
}
