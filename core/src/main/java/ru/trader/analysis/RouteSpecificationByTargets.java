package ru.trader.analysis;

import ru.trader.analysis.graph.Edge;
import ru.trader.analysis.graph.Traversal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class RouteSpecificationByTargets<T> implements RouteSpecification<T> {
    protected final Collection<T> targets;
    protected final boolean all;

    private RouteSpecificationByTargets(Collection<T> targets, boolean all) {
        this.all = all;
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
        Iterator<Edge<T>> iterator = entry.routeIterator();
        while (iterator.hasNext()){
            if (targets.contains(iterator.next().getTarget().getEntry())){
                return true;
            }
        }
        return false;
    }

    public static <T> RouteSpecificationByTargets<T> all(Collection<T> targets){
        return new RouteSpecificationByTargets<>(targets, true);
    }

    public static <T> RouteSpecificationByTargets<T> any(Collection<T> targets){
        return new RouteSpecificationByTargets<>(targets, false);
    }

}
