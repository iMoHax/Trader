package ru.trader.store.berkeley;

import ru.trader.core.*;
import ru.trader.store.berkeley.entities.BDBPlace;
import ru.trader.store.berkeley.entities.BDBVendor;

import java.util.Collection;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

public class PlaceProxy extends AbstractPlace {
    private final BDBPlace place;
    private BDBStore store;
    private Collection<Vendor> vendors;
    private final ReentrantLock lock = new ReentrantLock();

    public PlaceProxy(BDBPlace place, BDBStore store) {
        this.place = place;
        this.store = store;
        vendors = null;
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
    protected void updatePopulation(long population) {
        place.setPopulation(population);
        store.getPlaceAccessor().update(place);
    }

    @Override
    protected void updateFaction(FACTION faction) {
        place.setFaction(faction);
        store.getPlaceAccessor().update(place);
    }

    @Override
    protected void updateGovernment(GOVERNMENT government) {
        place.setGovernment(government);
        store.getPlaceAccessor().update(place);
    }

    @Override
    protected void updatePower(POWER power, POWER_STATE state) {
        place.setPower(power, state);
        store.getPlaceAccessor().update(place);
    }

    @Override
    protected void updateUpkeep(long upkeep) {
        place.setUpkeep(upkeep);
        store.getPlaceAccessor().update(place);
    }

    @Override
    protected void updateIncome(long income) {
        place.setIncome(income);
        store.getPlaceAccessor().update(place);
    }

    @Override
    protected void updatePosition(double x, double y, double z) {
        place.setPosition(x, y, z);
        store.getPlaceAccessor().update(place);
    }

    @Override
    protected void addVendor(Vendor vendor) {
        store.getVendorAccessor().put(((VendorProxy) vendor).getEntity());
        if (vendors != null || lock.isLocked()) {
            unsafe( (v) -> {
                if (vendors != null){
                    vendors.add(vendor);
                }
            });
        }
    }

    @Override
    protected void removeVendor(Vendor vendor) {
        store.getVendorAccessor().delete(((VendorProxy) vendor).getEntity());
        if (vendors != null || lock.isLocked()) {
            unsafe( (v) -> {
                if (vendors != null){
                    vendors.remove(vendor);
                }
            });
        }
    }

    @Override
    public String getName() {
        return place.getName();
    }

    @Override
    public long getPopulation() {
        return place.getPopulation();
    }

    @Override
    public FACTION getFaction() {
        return place.getFaction();
    }

    @Override
    public GOVERNMENT getGovernment() {
        return place.getGovernment();
    }

    @Override
    public POWER getPower() {
        return place.getPower();
    }

    @Override
    public POWER_STATE getPowerState() {
        return place.getPowerState();
    }

    @Override
    public long getUpkeep() {
        return place.getUpkeep();
    }

    @Override
    public long getIncome() {
        return place.getIncome();
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
        if (vendors == null){
            unsafe( (v) -> {
                if (vendors == null){
                    vendors = store.getVendorAccessor().getAllByPlace(place.getId());
                }
            });
        }
        return vendors;
    }

    @Override
    public long count() {
        if (vendors != null){
            return vendors.size();
        }
        return get().size();
    }

    @Override
    public boolean isEmpty() {
        if (vendors != null){
            return vendors.isEmpty();
        }
        return !store.getVendorAccessor().contains(place.getId());
    }

    @Override
    public boolean canRefill() {
        return store.getVendorAccessor().contains(place.getId(), v -> v.has(SERVICE_TYPE.REFUEL));
    }

    @Override
    public Collection<String> getVendorNames() {
        return store.getVendorAccessor().getNamesByPlace(place.getId());
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

    private void unsafe(Consumer<Void> operation){
        lock.lock();
        try {
            operation.accept(null);
        } finally {
            lock.unlock();
        }
    }
}
