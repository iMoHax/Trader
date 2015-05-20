package ru.trader.analysis.graph;

import ru.trader.graph.Connectable;

public class ConnectibleEdge<T extends Connectable<T>> extends Edge<T> {
    protected final boolean refill;
    protected final double fuel;

    public ConnectibleEdge(Vertex<T> source, Vertex<T> target, boolean refill, double fuel) {
        super(source, target);
        this.refill = refill;
        this.fuel = fuel;
    }

    public boolean isRefill() {
        return refill;
    }

    public double getFuel() {
        return fuel;
    }

    @Override
    protected double computeWeight() {
        T s = source.getEntry();
        T t = target.getEntry();
        return s.getDistance(t);
    }

    @Override
    public String toString() {
        return source.getEntry().toString() + " - "+ weight
               + (refill ? "R" : "")
               +" -> " + target.getEntry().toString();
    }
}
