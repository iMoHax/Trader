package ru.trader.store.simple;

import ru.trader.core.AbstractPlace;
import ru.trader.core.FACTION;
import ru.trader.core.GOVERNMENT;
import ru.trader.core.Vendor;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class SimplePlace extends AbstractPlace {
    private String name;
    private final List<Vendor> vendors;

    private double x;
    private double y;
    private double z;

    private FACTION faction;
    private GOVERNMENT government;

    public SimplePlace(String name) {
        this.name = name;
        this.vendors = new CopyOnWriteArrayList<>();
    }

    public SimplePlace(String name, double x, double y, double z) {
        this(name);
        setPosition(x, y, z);
    }

    public SimplePlace(String name, double x, double y, double z, Vendor vendor) {
        this(name,x,y,z);
        vendors.add(vendor);
    }

    @Override
    protected Vendor createVendor(String name) {
        return new SimpleVendor(name);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public FACTION getFaction() {
        return faction;
    }

    @Override
    public GOVERNMENT getGovernment() {
        return government;
    }

    @Override
    public double getX() {
        return x;
    }

    @Override
    public double getY() {
        return y;
    }

    @Override
    public double getZ() {
        return z;
    }

    @Override
    public Collection<Vendor> get() {
        return vendors;
    }

    @Override
    protected void updateName(String name) {
        this.name = name;
    }

    @Override
    protected void updateFaction(FACTION faction) {
        this.faction = faction;
    }

    @Override
    protected void updateGovernment(GOVERNMENT government) {
        this.government = government;
    }

    @Override
    protected void updatePosition(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    protected void addVendor(Vendor vendor) {
        if (vendor instanceof SimpleVendor){
            ((SimpleVendor) vendor).setPlace(this);
        }
        vendors.add(vendor);
    }

    @Override
    protected void removeVendor(Vendor vendor) {
        vendors.remove(vendor);
    }

}
