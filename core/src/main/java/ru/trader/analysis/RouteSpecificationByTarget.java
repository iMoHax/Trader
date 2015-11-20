package ru.trader.analysis;

import ru.trader.analysis.graph.Edge;
import ru.trader.analysis.graph.Traversal;

public class RouteSpecificationByTarget<T> implements RouteSpecification<T> {
    private T target;
    protected final long time;

    public RouteSpecificationByTarget(T target) {
        this(target, Long.MAX_VALUE);
    }

    public RouteSpecificationByTarget(T target, long time) {
        this.target = target;
        this.time = time;
    }

    private boolean checkTime(){
        return time < Long.MAX_VALUE;
    }

    protected T getTarget(){
        return target;
    }

    protected void remove(){
        target = null;
    }

    @Override
    public boolean specified(Edge<T> edge, Traversal<T> entry) {
        if (target == null) return true;
        if (checkTime() && edge.getTime() + entry.getTime() > time) return false;
        return edge.isConnect(target);
    }
}
