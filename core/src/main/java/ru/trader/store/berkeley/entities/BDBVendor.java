package ru.trader.store.berkeley.entities;

import com.sleepycat.persist.model.*;
import ru.trader.core.SERVICE_TYPE;

import java.util.Collection;
import java.util.HashSet;

@Entity(version = 1)
public class BDBVendor {

    @PrimaryKey(sequence = "V_ID")
    private long id;

    @SecondaryKey(relate = Relationship.MANY_TO_ONE,
                  relatedEntity = BDBPlace.class, onRelatedEntityDelete = DeleteAction.CASCADE)
    private long placeId;
    private String name;
    private double distance;

    @SecondaryKey(relate=Relationship.MANY_TO_MANY)
    Collection<SERVICE_TYPE> services = new HashSet<>();

    private BDBVendor() {
    }

    public BDBVendor(String name, long placeId) {
        this.name = name;
        this.placeId = placeId;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getPlaceId() {
        return placeId;
    }

    public void setPlace(long placeId){
        this.placeId = placeId;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public void add(SERVICE_TYPE service){
        services.add(service);
    }

    public void remove(SERVICE_TYPE service){
        services.remove(service);
    }

    public boolean has(SERVICE_TYPE service){
        return services.contains(service);
    }

    public Collection<SERVICE_TYPE> getServices() {
        return services;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BDBVendor)) return false;

        BDBVendor bdbVendor = (BDBVendor) o;

        if (Double.compare(bdbVendor.distance, distance) != 0) return false;
        if (id != bdbVendor.id) return false;
        if (placeId != bdbVendor.placeId) return false;
        if (!name.equals(bdbVendor.name)) return false;
        if (!services.equals(bdbVendor.services)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }
}
