package ru.trader.analysis;

import ru.trader.analysis.graph.Edge;
import ru.trader.analysis.graph.Traversal;

public class RouteSpecificationByTarget<T> implements RouteSpecification<T> {
    private final T target;
    private final long start;
    private final long end;

    public RouteSpecificationByTarget(T target) {
        this(target, 0, Long.MAX_VALUE);
    }

    public RouteSpecificationByTarget(T target, long time) {
        this(target, 0, time);
    }

    public RouteSpecificationByTarget(T target, long startTime, long endTime) {
        this.target = target;
        this.start = startTime;
        this.end = endTime;
    }

    private boolean checkTime(){
        return end < Long.MAX_VALUE;
    }

    public T getTarget(){
        return target;
    }

    @Override
    public long getStart(){
        return start;
    }

    @Override
    public long getEnd(){
        return end;
    }

    @Override
    public boolean specified(Edge<T> edge, Traversal<T> entry) {
        if (target == null) return true;
        if (checkTime()){
            long time = edge.getTime();
            if (time < start || time > end) return false;
        }
        return edge.isConnect(target);
    }
}
