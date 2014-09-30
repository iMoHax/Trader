package ru.trader.store.berkeley.entities;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;

@Entity
public class Vendor {

    @PrimaryKey(sequence = "V_ID")
    private long id;

    private String name;

    @SecondaryKey(relate = Relationship.ONE_TO_ONE)
    private double x;
    @SecondaryKey(relate = Relationship.ONE_TO_ONE)
    private double y;
    @SecondaryKey(relate = Relationship.ONE_TO_ONE)
    private double z;

    private Vendor() {
    }

    public Vendor(String name) {
        this.name = name;
    }

    public long getId() {
        return id;
    }
}
