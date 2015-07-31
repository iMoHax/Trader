package ru.trader.analysis.graph;

public interface Connectable<T> extends Comparable<Connectable<T>>{

    public double getDistance(T other);

    public boolean canRefill();

}
