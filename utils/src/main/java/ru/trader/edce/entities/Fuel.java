package ru.trader.edce.entities;

import java.util.Objects;

public class Fuel {
    private double capacity;
    private double level;

    public double getCapacity() {
        return capacity;
    }

    public void setCapacity(double capacity) {
        this.capacity = capacity;
    }

    public double getLevel() {
        return level;
    }

    public void setLevel(double level) {
        this.level = level;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Fuel fuel = (Fuel) o;
        return Objects.equals(capacity, fuel.capacity) &&
                Objects.equals(level, fuel.level);
    }

    @Override
    public int hashCode() {
        return Objects.hash(capacity);
    }
}
