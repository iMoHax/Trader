package ru.trader.analysis;


import ru.trader.analysis.graph.ConnectibleEdge;
import ru.trader.analysis.graph.ConnectibleGraph;
import ru.trader.analysis.graph.Path;
import ru.trader.core.Vendor;

import java.util.ArrayList;
import java.util.List;

public class TransitPath {
    private final List<ConnectibleEdge<Vendor>> entries;
    private double fuelCost;
    private double remain;
    private int refillCount;

    public TransitPath(Path<Vendor> path, double fuel) {
        List<ConnectibleGraph<Vendor>.BuildEdge> edges = path.getEdges();
        entries = new ArrayList<>(edges.size());
        createEdges(edges, fuel);
    }

    private void createEdges(List<ConnectibleGraph<Vendor>.BuildEdge> edges, double fuel) {
        fuelCost = 0; refillCount = 0;
        for (int i = edges.size() - 1; i >= 0; i--) {
            ConnectibleGraph<Vendor>.BuildEdge edge = edges.get(i);
            ConnectibleEdge<Vendor> cEdge = new ConnectibleEdge<>(edge.getSource(), edge.getTarget());
            double fuelCost = edge.getFuelCost(fuel);
            this.fuelCost += fuelCost;
            cEdge.setFuelCost(fuelCost);
            entries.add(cEdge);
            if (fuel < 0 || fuel < edge.getMinFuel()){
                if (refillCount == 0){
                    fuel = refill(edges, 0);
                    if (fuel < 0){
                        fuel = refill(edges, entries.size()-1);
                    }
                } else {
                    fuel = refill(edges, entries.size()-1);
                }
                if (fuel < 0)
                    throw new IllegalStateException("Is not exists path");
                refillCount++;
            } else {
                fuel -= fuelCost;
            }
        }
        remain = fuel;
    }

    private double refill(List<ConnectibleGraph<Vendor>.BuildEdge> edges, int startIndex){
        double max = -1;
        for (int i = startIndex; i >= 0; i--) {
            ConnectibleGraph<Vendor>.BuildEdge e = edges.get(edges.size()-1-i);
            if (max != -1){
                max += e.getFuelCost(max + e.getMinFuel());
            }
            Vendor source = e.getSource().getEntry();
            if (source.canRefill()){
                ConnectibleEdge<Vendor> ce = entries.get(i);
                if (ce.isRefill()){
                    throw new IllegalStateException("Is not exists path");
                }
                double remain = max != -1 ? Math.min(max, e.getRefill()) : e.getRefill();
                double fuelCost = e.getFuelCost(remain);
                double fuel = updateFuelCost(edges, i+1, startIndex, remain-fuelCost);
                if (fuel < 0){
                    continue;
                }
                this.fuelCost += fuelCost - ce.getFuelCost();
                ce.setFuelCost(fuelCost);
                ce.setRefill(remain);
                return fuel;
            }
            if (max == -1 || e.getMaxFuel() < max){
                max = e.getMaxFuel();
            }
        }
        return -1;
    }

    private double updateFuelCost(List<ConnectibleGraph<Vendor>.BuildEdge> edges, int startIndex, int endIndex, double fuel){
        for (int i = startIndex+1; i <= endIndex; i++) {
            ConnectibleGraph<Vendor>.BuildEdge e = edges.get(edges.size()-1-i);
            ConnectibleEdge<Vendor> ce = entries.get(i);
            double fuelCost = e.getFuelCost(fuel);
            this.fuelCost = fuelCost - ce.getFuelCost();
            ce.setFuelCost(fuelCost);
            fuel -= fuelCost;
            if (fuel < 0 || fuel < e.getMinFuel()){
                return -1;
            }
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
