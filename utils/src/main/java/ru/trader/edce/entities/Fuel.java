package ru.trader.edce.entities;

import java.util.Objects;

public class Fuel {
    private double capacity;
    private double lvl;

    public double getCapacity() {
        return capacity;
    }

    public void setCapacity(double capacity) {
        this.capacity = capacity;
    }

    public double getLvl() {
        return lvl;
    }

    public void setLvl(double lvl) {
        this.lvl = lvl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Fuel fuel = (Fuel) o;
        return Objects.equals(capacity, fuel.capacity) &&
                Objects.equals(lvl, fuel.lvl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(capacity);
    }
}
