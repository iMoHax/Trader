package ru.trader.core;

import ru.trader.analysis.graph.Connectable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

public interface Place extends Connectable<Place> {

    String getName();
    void setName(String name);

    double getX();
    double getY();
    double getZ();
    void setPosition(double x, double y, double z);

    long getPopulation();
    void setPopulation(long population);

    FACTION getFaction();
    void setFaction(FACTION faction);

    GOVERNMENT getGovernment();
    void setGovernment(GOVERNMENT government);

    POWER getPower();
    POWER_STATE getPowerState();
    void setPower(POWER power, POWER_STATE state);
    Collection<Place> getControllingSystems();
    long getUpkeep();
    void setUpkeep(long upkeep);
    long getIncome();
    void setIncome(long income);


    Collection<Vendor> get();
    default Collection<Vendor> get(boolean withTransit){
        if (withTransit){
            Collection<Vendor> vendors = new ArrayList<>();
            vendors.add(new TransitVendor(this));
            vendors.addAll(get());
            return vendors;
        } else {
            return get();
        }
    }
    default Collection<String> getVendorNames(){
        return get().stream().map(Vendor::getName).collect(Collectors.toList());
    }

    default Vendor get(String name){
        Optional<Vendor> vendor = get().stream().filter(p -> name.equalsIgnoreCase(p.getName())).findFirst();
        return vendor.isPresent() ? vendor.get() : null;
    }
    void add(Vendor vendor);
    Vendor addVendor(String name);
    void remove(Vendor vendor);

    default long count(){
        return get().size();
    }

    default boolean isEmpty(){
        return get().isEmpty();
    }

    @Override
    default boolean canRefill() {
        return !isEmpty() && get().stream().filter(v -> v.has(SERVICE_TYPE.REFUEL)).findAny().isPresent();
    }

    @Override
    default double getDistance(Place other){
        return getDistance(other.getX(), other.getY(), other.getZ());
    }

    default double getDistance(double x, double y, double z){
        return Math.sqrt(Math.pow(x - getX(), 2) + Math.pow(y - getY(), 2) + Math.pow(z - getZ(), 2));
    }

    default void clear(){
        Collection<Vendor> vendors = new ArrayList<>(get());
        for (Vendor vendor : vendors) {
            remove(vendor);
        }
    }

    default void clearOffers(){
        for (Vendor vendor : get()) {
            vendor.clear();
        }
    }

    default Vendor asTransit(){
        return new TransitVendor(this);
    }

    default boolean isPopulated(){
        return getPopulation() > 0;
    }


    default double computeUpkeep(Place headquarter){
        double distance = getDistance(headquarter);
        return distance * distance * 0.001  + 20.5;
    }

    //возможно log(0.32 * Население)
    static long[] CCgroups = new long[]{0,0,0,3_140,31_530,316_000,3_160_000,31_620_000,320_000_000,3_162_000_000L};

    default long computeCC(){
        return computeCC(CCgroups);
    }

    default long computeCC(long[] CCgroups){
        long population = getPopulation();
        if (population == 0) return 0;
        for (int i = 0; i < CCgroups.length; i++) {
            long minPop = CCgroups[i];
            if (population < minPop){
                return i+1;
            }
        }
        return CCgroups.length+1;
    }
}
