package ru.trader.analysis;

import ru.trader.analysis.graph.Edge;
import ru.trader.analysis.graph.Traversal;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;

public class RouteSpecificationByTargets<T> implements RouteSpecification<T> {
    private final Collection<T> targets;
    private final boolean all;
    private final boolean targetOnly;
    private final long start;
    private final long end;

    private RouteSpecificationByTargets(Collection<T> targets, long startTime, long endTime, boolean all, boolean targetOnly) {
        this.all = all;
        this.targetOnly = targetOnly;
        this.targets = new HashSet<>(targets);
        this.start = startTime;
        this.end = endTime;
    }

    private boolean checkTime(){
        return end < Long.MAX_VALUE;
    }

    public Collection<T> getTargets(){
        return targets;
    }

    @Override
    public long getStart() {
        return start;
    }

    @Override
    public long getEnd() {
        return end;
    }

    public boolean isAny(){
        return targetOnly;
    }

    public boolean isAll(){
        return all;
    }

    public boolean isContainAny(){
        return !targetOnly && !all;
    }

    @Override
    public boolean specified(Edge<T> edge, Traversal<T> entry) {
        if (targets.isEmpty()) return true;
        if (checkTime()){
            long time = edge.getTime();
            if (targetOnly && (time < start || time > end)) return false;
        }
        return all ? containsAll(edge, entry) == 0 : containsAny(edge, entry) == 0;
    }

    @Override
    public boolean content(Edge<T> edge, Traversal<T> entry) {
        if (checkTime()){
            long time = edge.getTime();
            if (time < start || time > end) return false;
        }
        return targets.contains(edge.getTarget().getEntry());
    }

    @Override
    public int lastFound(Edge<T> edge, Traversal<T> entry) {
        if (targets.isEmpty()) return 0;
        if (checkTime()){
            long time = edge.getTime();
            if (targetOnly && (time < start || time > end)) return maxMatches();
        }
        return all ? containsAll(edge, entry) : containsAny(edge, entry);
    }

    @Override
    public int maxMatches() {
        if (targets.isEmpty()) return 0;
        return all ? targets.size() : 1;
    }

    private int containsAll(Edge<T> edge, Traversal<T> entry) {
        Collection<T> founds = new HashSet<>();

        Optional<Traversal<T>> t = Optional.of(entry);
        while (t.isPresent()){
            Traversal<T> e = t.get();
            t = e.getHead();
            if (checkTime()){
                long time = e.getTime();
                if (time > end) continue;
                if (time < start) break;
            }
            T target = e.getTarget().getEntry();
            if (targets.contains(target)){
                founds.add(target);
            }
            if (targets.size() == founds.size()) return 0;
        }

        T target = edge.getTarget().getEntry();
        if (checkTime()){
            long time = edge.getTime();
            if (time < start || time > end) return targets.size() - founds.size();
        }
        if (targets.contains(target)){
            founds.add(target);
        }
        return targets.size() - founds.size();
    }

    private int containsAny(Edge<T> edge, Traversal<T> entry) {
        T obj = edge.getTarget().getEntry();
        boolean check = true;
        if (checkTime()){
            long time = edge.getTime();
            if (time < start || time > end){
                if (targetOnly) return 1;
                check = false;
            }
        }
        if (check && targets.contains(obj)){
            return 0;
        }
        if (targetOnly){
            return 1;
        }
        Optional<Traversal<T>> t = Optional.of(entry);
        while (t.isPresent()){
            Traversal<T> e = t.get();
            t = e.getHead();
            if (checkTime()){
                long time = e.getTime();
                if (time > end) continue;
                if (time < start) break;
            }
            T target = e.getTarget().getEntry();
            if (targets.contains(target)){
                return 0;
            }
        }
        return 1;
    }

    public static <T> RouteSpecificationByTargets<T> all(Collection<T> targets){
        return all(targets, Long.MAX_VALUE);
    }

    public static <T> RouteSpecificationByTargets<T> all(Collection<T> targets, long time){
        return new RouteSpecificationByTargets<>(targets, 0, time, true, false);
    }

    public static <T> RouteSpecificationByTargets<T> all(Collection<T> targets, long startTime, long endTime){
        return new RouteSpecificationByTargets<>(targets, startTime, endTime, true, false);
    }

    public static <T> RouteSpecificationByTargets<T> any(Collection<T> targets){
        return any(targets, Long.MAX_VALUE);
    }

    public static <T> RouteSpecificationByTargets<T> any(Collection<T> targets, long time){
        return new RouteSpecificationByTargets<>(targets, 0, time, false, true);
    }

    public static <T> RouteSpecificationByTargets<T> any(Collection<T> targets, long startTime, long endTime){
        return new RouteSpecificationByTargets<>(targets, startTime, endTime, false, true);
    }

    public static <T> RouteSpecificationByTargets<T> containAny(Collection<T> targets){
        return containAny(targets, Long.MAX_VALUE);
    }

    public static <T> RouteSpecificationByTargets<T> containAny(Collection<T> targets, long time){
        return new RouteSpecificationByTargets<>(targets, 0, time, false, false);
    }

    public static <T> RouteSpecificationByTargets<T> containAny(Collection<T> targets, long startTime, long endTime){
        return new RouteSpecificationByTargets<>(targets, startTime, endTime, false, false);
    }
}
