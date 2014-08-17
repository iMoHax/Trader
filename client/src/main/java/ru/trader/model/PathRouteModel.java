package ru.trader.model;

import ru.trader.core.Vendor;
import ru.trader.graph.PathRoute;

public class PathRouteModel {
    private final double distance;
    private final double totalProfit;
    private final int jumps;
    private final int refuels;

    private final PathRoute path;

    public PathRouteModel(PathRoute path) {
        this.path = path;
        PathRoute p = path.getRoot();
        double pr =0, d = 0; int j = 0, r = 0;
        while (p.hasNext()){
            p = p.getNext();
            d += path.getDistance();
            pr += p.getMaxProfit();
            j++;
            if (p.isRefill()) r++;
        }
        totalProfit = pr;
        distance = d;
        jumps = j;
        refuels = r;
    }

    public double getDistance() {
        return distance;
    }

    public double getTotalProfit() {
        return totalProfit;
    }

    public int getJumps() {
        return jumps;
    }

    public int getRefuels() {
        return refuels;
    }

    public PathRoute getPath() {
        return path;
    }
}
