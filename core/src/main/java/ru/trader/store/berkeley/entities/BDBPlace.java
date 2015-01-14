package ru.trader.store.berkeley.entities;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;

@Entity(version = 1)
public class BDBPlace {

    @PrimaryKey(sequence = "P_ID")
    private long id;

    @SecondaryKey(relate = Relationship.MANY_TO_ONE)
    private double distance;

    private String name;
    private double x;
    private double y;
    private double z;


    private BDBPlace() {
    }

    public BDBPlace(String name, double x, double y, double z) {
        this.name = name;
        setPosition(x,y,z);
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

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public void setPosition(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.distance = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2));
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BDBPlace)) return false;

        BDBPlace bdbPlace = (BDBPlace) o;

        if (id != bdbPlace.id) return false;
        if (Double.compare(bdbPlace.x, x) != 0) return false;
        if (Double.compare(bdbPlace.y, y) != 0) return false;
        if (Double.compare(bdbPlace.z, z) != 0) return false;
        if (!name.equals(bdbPlace.name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }
}
