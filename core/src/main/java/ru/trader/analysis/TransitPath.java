package ru.trader.analysis;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.analysis.graph.ConnectibleEdge;
import ru.trader.analysis.graph.ConnectibleGraph;
import ru.trader.analysis.graph.Path;
import ru.trader.core.Vendor;

import java.util.*;

public class TransitPath {
    private final static Logger LOG = LoggerFactory.getLogger(TransitPath.class);
    private final List<ConnectibleEdge<Vendor>> entries;
    private double fuelCost;
    private double remain;
    private int refillCount;

    public TransitPath(Path<Vendor> path, double fuel) {
        entries = new ArrayList<>(path.getSize());
        createEdges(path, fuel);
    }

    private void createEdges(Path<Vendor> path, double fuel) {
        fuelCost = 0;
        refillCount = 0;

        for (Iterator<ConnectibleGraph<Vendor>.BuildEdge> iterator = path.listIterator(0); iterator.hasNext(); ) {
            ConnectibleGraph<Vendor>.BuildEdge edge = iterator.next();
            double cost = edge.getFuelCost(fuel);

            ConnectibleEdge<Vendor> cEdge = new ConnectibleEdge<>(edge.getSource(), edge.getTarget());
            cEdge.setFuelCost(cost);
            fuelCost += cost;
            entries.add(cEdge);

            if (fuel < 0 || fuel < edge.getMinFuel()) {
                //TODO: improve best refill place search
                fuel = refill(path, false);
                if (fuel < 0){
                    fuel = refill(path, true);
                }
                if (fuel < 0)
                    LOG.error("Incorrect path, path = {}, fuel = {}, ship = {}", path, fuel, edge.getShip());
                    throw new IllegalStateException("Is not exists path");
            } else {
                fuel -= cost;
            }
        }
        remain = fuel;
    }

    private double refill(Path<Vendor> path, boolean includeTransit){
        int lastIndex = entries.size() - 1;
        double max = path.getMaxFuel(true, lastIndex);

        ListIterator<ConnectibleEdge<Vendor>> iterator = entries.listIterator(lastIndex+1);
        int refillIndex = lastIndex;
        ConnectibleEdge<Vendor> refillEdge = null;
        while (iterator.hasPrevious()){
            refillEdge = iterator.previous();
            if (refillEdge.getSource().getEntry().canRefill()){
                if (refillEdge.isRefill()){
                    return -1;
                }
                if (includeTransit || !refillEdge.getSource().getEntry().isTransit()){
                    break;
                }
            }
            refillIndex--;
        }

        if (refillIndex < 0 || refillEdge == null) return -1;

        ConnectibleGraph<Vendor>.BuildEdge pathEdge = path.get(refillIndex);
        double fuel = Math.min(pathEdge.getShip().getRoundFuel(max), pathEdge.getRefill());
        refillEdge.setRefill(fuel);
        refillCount++;

        ListIterator<ConnectibleGraph<Vendor>.BuildEdge> pathIterator = path.listIterator(refillIndex);
        while (iterator.hasNext()){
            ConnectibleEdge<Vendor> edge = iterator.next();
            pathEdge = pathIterator.next();
            fuelCost -= edge.getFuelCost();
            double cost = pathEdge.getFuelCost(fuel);
            edge.setFuelCost(cost);
            fuelCost += cost;

            fuel -= cost;
        }

        if (fuel < 0){
            refillEdge.setRefill(0);
            refillCount--;
        }

        return fuel;
    }

    public List<ConnectibleEdge<Vendor>> getEntries() {
        return entries;
    }

    public double getFuelCost() {
        return fuelCost;
    }

    public double getRemain() {
        return remain;
    }

    public int getRefillCount() {
        return refillCount;
    }

    public boolean isRefill() {
        return entries.get(0).isRefill();
    }

    public int size() {
        return entries.size();
    }
}
