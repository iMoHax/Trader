package ru.trader.analysis.graph;

import ru.trader.core.Ship;
import ru.trader.graph.Connectable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Path<T extends Connectable<T>> {
    private final List<ConnectibleGraph<T>.BuildEdge> entries;
    private double minFuel;
    private double maxFuel;
    private double fuelCost;
    private int refillCount;


    public Path(Collection<ConnectibleGraph<T>.BuildEdge> edges) {
        entries = new ArrayList<>(edges);
        updateStat();
    }

    private void updateStat(){
        Ship ship = entries.get(0).getShip();
        double fuel = ship.getTank();
        minFuel = 0; maxFuel = 0; fuelCost = 0; refillCount = 0;
        boolean adding = true;
        for (int i = entries.size() - 1; i >= 0; i--) {
            ConnectibleGraph<T>.BuildEdge edge = entries.get(i);
            if (i < entries.size() - 1 && edge.getSource().getEntry().canRefill()){
                adding = false;
                fuel = edge.getMaxFuel();
            }
            if (fuel < 0 || fuel < edge.getMinFuel()){
                minFuel = ship.getTank()+1;
            }
            double cost = edge.getFuelCost(fuel);
            fuelCost += cost;
            fuel -= cost;
            if (adding) {
                minFuel += edge.getMinFuel();
            }
        }
        maxFuel = -1;
        for (ConnectibleGraph<T>.BuildEdge edge : entries) {
            if (maxFuel != -1){
                maxFuel += edge.getFuelCost(maxFuel + edge.getMinFuel());
            }
            if (maxFuel == -1 || edge.getMaxFuel() < maxFuel){
                maxFuel = edge.getMaxFuel();
            }
        }
        refillCount = getRefillCount(ship.getTank());
    }

    public List<ConnectibleGraph<T>.BuildEdge> getEdges() {
        return entries;
    }

    public double getFuelCost() {
        return fuelCost;
    }

    public double getMinFuel() {
        return minFuel;
    }

    public double getMaxFuel() {
        return maxFuel;
    }

    public int getSize(){
        return entries.size();
    }

    public int getRefillCount() {
        return refillCount;
    }

    public int getRefillCount(double fuel){
        int res = 0;
        for (int i = entries.size() - 1; i >= 0; i--) {
            ConnectibleGraph<T>.BuildEdge edge = entries.get(i);
            fuel -= edge.getFuelCost(fuel);
            if (fuel < 0) {
                res++;
                fuel = edge.getMaxFuel();
            }
        }
        return res;
    }

    public Path<T> add(ConnectibleGraph<T>.BuildEdge edge){
        Path<T> res = new Path<>(entries);
        res.entries.add(edge);
        res.updateStat();
        return res;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        for (int i = entries.size() - 1; i >= 0; i--) {
            ConnectibleGraph<T>.BuildEdge entry = entries.get(i);
            sb.append(entry);
            if (i>0)
                sb.append(", ");
        }
        sb.append('}');
        return sb.toString();
    }
}
