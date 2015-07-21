package ru.trader.analysis;

import ru.trader.analysis.graph.Edge;
import ru.trader.analysis.graph.Traversal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class RouteSpecificationByTargets<T> implements RouteSpecification<T> {
    protected final Collection<T> targets;
    protected final boolean all;
    protected final boolean targetOnly;

    private RouteSpecificationByTargets(Collection<T> targets, boolean all, boolean targetOnly) {
        this.all = all;
        this.targetOnly = targetOnly;
        this.targets = new ArrayList<>(targets);
    }

    @Override
    public boolean specified(Edge<T> edge, Traversal<T> entry) {
        return all ? containsAll(edge, entry) : containsAny(edge, entry);
    }

    private boolean containsAll(Edge<T> edge, Traversal<T> entry) {
        T obj = edge.getTarget().getEntry();
        Collection<T> set = new ArrayList<>(targets.size());
        set.add(obj);
        entry.routeIterator().forEachRemaining(e -> set.add(e.getTarget().getEntry()));
        return targets.containsAll(set);
    }

    private boolean containsAny(Edge<T> edge, Traversal<T> entry) {
        T obj = edge.getTarget().getEntry();
        if (targets.contains(obj)) return true;
        if (targetOnly){
            return false;
        }
        Iterator<Edge<T>> iterator = entry.routeIterator();
        while (iterator.hasNext()){
            if (targets.contains(iterator.next().getTarget().getEntry())){
                return true;
            }
        }
        return false;
    }

    public static <T> RouteSpecificationByTargets<T> all(Collection<T> targets){
        return new RouteSpecificationByTargets<>(targets, true, false);
    }

    public static <T> RouteSpecificationByTargets<T> any(Collection<T> targets){
        return new RouteSpecificationByTargets<>(targets, false, true);
    }

    public static <T> RouteSpecificationByTargets<T> containAny(Collection<T> targets){
        return new RouteSpecificationByTargets<>(targets, false, false);
    }
}
