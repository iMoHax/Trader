package ru.trader.graph;

public interface Connectable<T> extends Comparable<Connectable<T>>{

    public double getDistance(T other);

    public boolean canRefill();

}
