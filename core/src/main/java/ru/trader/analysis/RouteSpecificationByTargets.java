package ru.trader.analysis;

import ru.trader.analysis.graph.Edge;
import ru.trader.analysis.graph.Traversal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

public class RouteSpecificationByTargets<T> implements RouteSpecification<T> {
    protected final Collection<T> targets;
    protected final boolean all;
    protected final boolean targetOnly;

    private RouteSpecificationByTargets(Collection<T> targets, boolean all, boolean targetOnly) {
        this.all = all;
        this.targetOnly = targetOnly;
        this.targets = new HashSet<>(targets);
    }

    @Override
    public boolean specified(Edge<T> edge, Traversal<T> entry) {
        return all ? containsAll(edge, entry) == 0 : containsAny(edge, entry) == 0;
    }

    @Override
    public boolean content(Edge<T> edge, Traversal<T> entry) {
        return targets.contains(edge.getTarget().getEntry());
    }

    @Override
    public int lastFound(Edge<T> edge, Traversal<T> entry) {
        return all ? containsAll(edge, entry) : containsAny(edge, entry);
    }

    @Override
    public int matchCount() {
        return all ? targets.size() : 1;
    }

    @Override
    public void onAnd(RouteSpecification<T> other) {
        if (other instanceof RouteSpecificationByTarget){
            T otherTarget = ((RouteSpecificationByTarget<T>)other).target;
            targets.remove(otherTarget);
        } else
        if (other instanceof RouteSpecificationByTargets){
            RouteSpecificationByTargets<T> os = ((RouteSpecificationByTargets<T>)other);
            if (os.all){
                Collection<T> otherTargets = ((RouteSpecificationByTargets<T>)other).targets;
                targets.removeAll(otherTargets);
            }
        } else
        if (other instanceof RouteSpecificationByPair){
            T otherTarget = ((RouteSpecificationByPair<T>)other).second;
            targets.remove(otherTarget);
        }
    }

    private int containsAll(Edge<T> edge, Traversal<T> entry) {
        T obj = edge.getTarget().getEntry();
        Collection<T> set = new ArrayList<>(targets.size());
        set.add(obj);
        entry.routeIterator().forEachRemaining(e -> set.add(e.getTarget().getEntry()));
        int last = targets.size();
        for (T target : targets) {
            if (set.contains(target)){
                last--;
            }
        }
        return last;
    }

    private int containsAny(Edge<T> edge, Traversal<T> entry) {
        T obj = edge.getTarget().getEntry();
        if (targets.contains(obj)) return 0;
        if (targetOnly){
            return 1;
        }
        Iterator<Edge<T>> iterator = entry.routeIterator();
        while (iterator.hasNext()){
            if (targets.contains(iterator.next().getTarget().getEntry())){
                return 0;
            }
        }
        return 1;
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
