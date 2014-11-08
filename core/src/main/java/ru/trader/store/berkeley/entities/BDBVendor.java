package ru.trader.store.berkeley.entities;

import com.sleepycat.persist.model.*;
import ru.trader.core.SERVICE_TYPE;

import java.util.EnumSet;

@Entity(version = 1)
public class BDBVendor {

    @PrimaryKey(sequence = "V_ID")
    private long id;

    @SecondaryKey(relate = Relationship.MANY_TO_ONE,
            relatedEntity = BDBPlace.class,
            onRelatedEntityDelete = DeleteAction.NULLIFY)
    private long placeId;
    private String name;
    private double distance;

    @SecondaryKey(relate=Relationship.ONE_TO_MANY)
    EnumSet<SERVICE_TYPE> services = EnumSet.noneOf(SERVICE_TYPE.class);

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

}
