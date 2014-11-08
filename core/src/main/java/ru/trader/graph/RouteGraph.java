package ru.trader.graph;

import ru.trader.core.Place;

import java.util.*;

public class RouteGraph extends Graph<Place> {

    private double balance;
    private int cargo;
    private boolean groupRes;

    public static Comparator<PathRoute> byProfitComparator = (p1, p2) -> {
        PathRoute r1 = p1.getRoot();
        PathRoute r2 = p2.getRoot();
        int cmp = Double.compare(r2.getAvgProfit(), r1.getAvgProfit());
        if (cmp != 0 ) return cmp;
        cmp = Double.compare(r1.getDistance(), r2.getDistance());
        if (cmp != 0) return cmp;
        cmp = Double.compare(r1.getLandsCount(), r2.getLandsCount());
        if (cmp != 0) return cmp;
        return cmp;
    };

    public static Comparator<PathRoute> groupByLengthComparator = (p1, p2) -> {
        int cmp = Integer.compare(p1.getLength(), p2.getLength());
        if (cmp != 0 ) return cmp;
        return byProfitComparator.compare(p1, p2);
    };

    public RouteGraph(Place start, Collection<Place> set, double stock, double maxDistance, boolean withRefill, int maxDeep) {
        this(start, set, stock, maxDistance, withRefill, maxDeep, false);
    }

    public RouteGraph(Place start, Collection<Place> set, double stock, double maxDistance, boolean withRefill, int maxDeep, boolean groupRes) {
        super(start, set, stock, maxDistance, withRefill, maxDeep, groupRes ? PathRoute::buildAvg : PathRoute::new);
        if (groupRes){
            this.groupRes = maxDeep > minJumps;
        }
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public void setCargo(int cargo) {
        this.cargo = cargo;
    }

    @Override
    protected TopList<Path<Place>> newTopList(int count) {
        int groupSize = 0;
        if (groupRes && getMinJumps() > 1){
            groupSize = Math.floorDiv(count, root.getLevel());
        }
        return new TopRoutes(count, groupSize);
    }

    private class TopRoutes extends TopList<Path<Place>> {
        private final int groupSize;

        public TopRoutes(int limit, int groupSize) {
            super(limit, (p1, p2) -> groupSize > 0 ? groupByLengthComparator.compare((PathRoute)p1, (PathRoute)p2) : RouteGraph.byProfitComparator.compare((PathRoute)p1, (PathRoute)p2));
            this.groupSize = groupSize;
        }

        @Override
        public boolean add(Path<Place> entry) {
            if (comparator != null){
                ((PathRoute)entry).sort(balance, cargo);
            }
            if (groupSize>0){
                addToGroupTop(list, entry, limit, comparator, (e) -> e.getLength()-1, groupSize);
            } else {
                if (comparator != null){
                    addToTop(list, entry, limit, comparator);
                } else {
                    if (list.size() >= limit) return false;
                    list.add(entry);
                    if (list.size() >= limit) return false;
                }
            }
            return true;
        }
    }

}
