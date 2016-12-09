package ru.trader.analysis.graph;

import ru.trader.core.Ship;

import java.util.*;

public class Path<T extends Connectable<T>> {
    private final LinkedList<ConnectibleGraph<T>.BuildEdge> entries;
    private double minFuel;
    private double maxFuel;
    private double fuelCost;
    private int refillCount;

    public Path(ConnectibleGraph<T>.BuildEdge edge) {
        this(edge, true);
    }

    public Path(Collection<ConnectibleGraph<T>.BuildEdge> edges) {
        this(edges, true);
    }

    private Path(Collection<ConnectibleGraph<T>.BuildEdge> edges, boolean update) {
        entries = new LinkedList<>(edges);
        if (update){
            updateStat();
        }
    }

    private Path(ConnectibleGraph<T>.BuildEdge edge, boolean update) {
        entries = new LinkedList<>();
        entries.add(edge);
        if (update){
            updateStat();
        }
    }

    private void updateStat(){
        Ship ship = entries.get(0).getShip();
        double fuel = ship.getTank();
        minFuel = 0; maxFuel = 0; fuelCost = 0; refillCount = 0;
        boolean refillFound = false;
        int index = -1; double f = ship.getTank();
        ConnectibleGraph<T>.BuildEdge prevEdge = null;
        for (ConnectibleGraph<T>.BuildEdge edge : entries) {
            index++;

            if (index > 0 && edge.getSource().getEntry().canRefill()){
                refillFound = true;
                f = edge.getMaxFuel();
            }

            if (!refillFound){
                if (prevEdge != null){
                    minFuel = Math.max(minFuel, edge.getMinFuel()) + prevEdge.getFuelCost(prevEdge.getMaxFuel());
                } else {
                    minFuel = edge.getMinFuel();
                }
            }

            double c = edge.getFuelCost(f);
            f -= c;


            double cost = edge.getFuelCost(fuel);
            fuel -= cost;
            if (fuel < 0){
                refillCount++;
                fuel = f;
                cost = c;
            }
            fuelCost += cost;

            if (f < 0){
                minFuel = ship.getTank()+1;
                break;
            }

            prevEdge = edge;
        }

        maxFuel = getMaxFuel(false, entries.size()-1);
    }

    public double getMaxFuel(boolean fromRefill, int endIndex){
        double maxFuel = -1;
        for (ListIterator<ConnectibleGraph<T>.BuildEdge> iterator = entries.listIterator(endIndex+1); iterator.hasPrevious(); ) {
            ConnectibleGraph<T>.BuildEdge edge = iterator.previous();
            if (maxFuel > -1) {
                maxFuel += edge.getFuelCost(edge.getMinFuel());
            }
            if (maxFuel == -1 || edge.getMaxFuel() < maxFuel) {
                maxFuel = edge.getMaxFuel();
            }
            if (fromRefill && edge.getSource().getEntry().canRefill()){
                break;
            }
        }
        return maxFuel;
    }

    public ConnectibleGraph<T>.BuildEdge get(int index){
        return entries.get(index);
    }

    public ListIterator<ConnectibleGraph<T>.BuildEdge> listIterator(int startIndex){
        return entries.listIterator(startIndex);
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
        for (ConnectibleGraph<T>.BuildEdge edge : entries) {
            fuel -= edge.getFuelCost(fuel);
            if (fuel < 0) {
                res++;
                fuel = edge.getMaxFuel();
            }
        }
        return res;
    }

    public Path<T> addFirst(ConnectibleGraph<T>.BuildEdge edge){
        Path<T> res = new Path<>(entries, false);
        res.entries.addFirst(edge);
        res.updateStat();
        return res;
    }

    public Path<T> addLast(ConnectibleGraph<T>.BuildEdge edge){
        Path<T> res = new Path<>(entries, false);
        res.entries.addLast(edge);
        res.updateStat();
        return res;
    }

    public T getSource(){
        return entries.getFirst().getSource().getEntry();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        int i = -1;
        for (ConnectibleGraph<T>.BuildEdge entry : entries) {
            i++;
            sb.append(entry);
            if (i < entries.size()-1){
                sb.append(", ");
            }
        }
        sb.append('}');
        return sb.toString();
    }
}
