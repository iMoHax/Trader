package ru.trader.graph;

public interface Connectable<T> {

    public double getDistance(T other);

    public boolean canRefill();

}
