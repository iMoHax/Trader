package ru.trader.analysis.graph;

import ru.trader.graph.Connectable;

public class ConnectibleEdge<T extends Connectable<T>> extends Edge<T> {
    protected boolean refill;
    protected double fuelCost;

    public ConnectibleEdge(Vertex<T> source, Vertex<T> target) {
        super(source, target);
    }

    public boolean isRefill() {
        return refill;
    }

    protected void setRefill(boolean refill) {
        this.refill = refill;
    }

    public double getFuelCost() {
        return fuelCost;
    }

    public void setFuelCost(double fuel) {
        this.fuelCost = fuel;
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
