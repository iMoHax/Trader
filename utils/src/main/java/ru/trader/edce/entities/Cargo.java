package ru.trader.edce.entities;

import java.util.Objects;

public class Cargo {
    private int capacity;
    private int qty;

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cargo cargo = (Cargo) o;
        return Objects.equals(capacity, cargo.capacity) &&
                Objects.equals(qty, cargo.qty);
    }

    @Override
    public int hashCode() {
        return Objects.hash(capacity);
    }
}
