package ru.trader.graph;

import ru.trader.core.Vendor;

import java.util.*;

public class RouteGraph extends Graph<Vendor> {

    private double balance;
    private int limit;

    private Comparator<Path<Vendor>> comparator = (p1, p2) -> {

        PathRoute r1 = (PathRoute) p1.getRoot();
        PathRoute r2 = (PathRoute) p2.getRoot();
        int cmp = Double.compare(r2.getProfit()/r2.getLandsCount(), r1.getProfit()/r1.getLandsCount());
        if (cmp != 0 ) return cmp;
        cmp = Double.compare(r1.getDistance(), r2.getDistance());
        if (cmp != 0) return cmp;
        cmp = Double.compare(r1.getLandsCount(), r2.getLandsCount());
        if (cmp != 0) return cmp;
        return cmp;
    };


    public RouteGraph(Vendor start, Collection<Vendor> set, double stock, double maxDistance, boolean withRefill, int maxDeep) {
        super(start, set, stock, maxDistance, withRefill, maxDeep, PathRoute::new);
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    @Override
    protected boolean onFindPath(ArrayList<Path<Vendor>> paths, int max, Path<Vendor> path) {
        PathRoute route = (PathRoute) path;
        route.sort(balance, limit);
        if (paths.size() == max){
            int index = Collections.binarySearch(paths, route, comparator);
            if (index < 0) index = -1 - index;
            if (index == max) return false;
            paths.add(index, path);
            paths.remove(max);

        } else {
            if (paths.size() < max-1){
                paths.add(route);
            } else {
                paths.add(route);
                paths.sort(comparator);
            }
        }
        return false;
    }


}
