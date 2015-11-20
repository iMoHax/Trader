package ru.trader.analysis;

import ru.trader.analysis.graph.Edge;
import ru.trader.analysis.graph.Traversal;

import java.util.*;

public class RouteSpecificationByTargets<T> implements RouteSpecification<T> {
    protected final Collection<T> targets;
    protected final boolean all;
    protected final boolean targetOnly;
    protected final long time;

    protected RouteSpecificationByTargets(Collection<T> targets, long time, boolean all, boolean targetOnly) {
        this.all = all;
        this.targetOnly = targetOnly;
        this.targets = new HashSet<>(targets);
        this.time = time;
    }

    private boolean checkTime(){
        return time < Long.MAX_VALUE;
    }

    @Override
    public boolean specified(Edge<T> edge, Traversal<T> entry) {
        if (targets.isEmpty()) return true;
        if (checkTime() && targetOnly && edge.getTime() + entry.getTime() > time) return false;
        return all ? containsAll(edge, entry) == 0 : containsAny(edge, entry) == 0;
    }

    @Override
    public boolean content(Edge<T> edge, Traversal<T> entry) {
        if (checkTime() && edge.getTime() + entry.getTime() > time) return false;
        return targets.contains(edge.getTarget().getEntry());
    }

    @Override
    public int lastFound(Edge<T> edge, Traversal<T> entry) {
        if (targets.isEmpty()) return 0;
        if (checkTime() && targetOnly && edge.getTime() + entry.getTime() > time) return matchCount();
        return all ? containsAll(edge, entry) : containsAny(edge, entry);
    }

    @Override
    public int matchCount() {
        if (targets.isEmpty()) return 0;
        return all ? targets.size() : 1;
    }

    private int containsAll(Edge<T> edge, Traversal<T> entry) {
        Collection<T> founds = new HashSet<>();
        List<Traversal<T>> entries = entry.toList();
        for (Traversal<T> e : entries) {
            T target = e.getTarget().getEntry();
            if (targets.contains(target)){
                if (checkTime() && e.getTime() > time){
                    return targets.size() - founds.size();
                } else {
                    founds.add(target);
                }
            }
            if (targets.size() == founds.size()) return 0;
        }
        T target = edge.getTarget().getEntry();
        if (targets.contains(target)){
            if (checkTime() && edge.getTime() + entry.getTime() > time){
                return targets.size() - founds.size();
            } else {
                founds.add(target);
            }
        }
        return targets.size() - founds.size();
    }

    private int containsAny(Edge<T> edge, Traversal<T> entry) {
        T obj = edge.getTarget().getEntry();
        if (targets.contains(obj)){
            if (targetOnly){
                if (checkTime() && edge.getTime() + entry.getTime() > time) return 1;
                 else return 0;
            } else {
                return 0;
            }
        }
        if (targetOnly){
            return 1;
        }
        List<Traversal<T>> entries = entry.toList();
        for (Traversal<T> e : entries) {
            T target = e.getTarget().getEntry();
            if (targets.contains(target)){
                if (checkTime() && e.getTime() > time){
                    return 1;
                } else {
                    return 0;
                }
            }
        }
        return 1;
    }

    public static <T> RouteSpecificationByTargets<T> all(Collection<T> targets){
        return all(targets, Long.MAX_VALUE);
    }

    public static <T> RouteSpecificationByTargets<T> all(Collection<T> targets, long time){
        return new RouteSpecificationByTargets<>(targets, time, true, false);
    }

    public static <T> RouteSpecificationByTargets<T> any(Collection<T> targets){
        return any(targets, Long.MAX_VALUE);
    }

    public static <T> RouteSpecificationByTargets<T> any(Collection<T> targets, long time){
        return new RouteSpecificationByTargets<>(targets, time, false, true);
    }

    public static <T> RouteSpecificationByTargets<T> containAny(Collection<T> targets){
        return containAny(targets, Long.MAX_VALUE);
    }

    public static <T> RouteSpecificationByTargets<T> containAny(Collection<T> targets, long time){
        return new RouteSpecificationByTargets<>(targets, time, false, false);
    }
}
