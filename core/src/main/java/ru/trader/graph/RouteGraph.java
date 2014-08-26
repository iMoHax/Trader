package ru.trader.graph;

import ru.trader.core.Vendor;

import java.util.*;

public class RouteGraph extends Graph<Vendor> {

    private double balance;
    private int limit;

     public static Comparator<PathRoute> comparator = (p1, p2) -> {
        PathRoute r1 = p1.getRoot();
        PathRoute r2 = p2.getRoot();
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
        addToTop(paths, route, max, (r1, r2) -> comparator.compare((PathRoute)r1, (PathRoute)r2));
        return false;
    }

    public static <T> void addToTop(List<T> list, T entry, int limit, Comparator<T> comparator){
        if (list.size() == limit){
            int index = Collections.binarySearch(list, entry, comparator);
            if (index < 0) index = -1 - index;
            if (index == limit) return;
            list.add(index, entry);
            list.remove(limit);

        } else {
            if (list.size() < limit-1){
                list.add(entry);
            } else {
                list.add(entry);
                list.sort(comparator);
            }
        }
    }
}
