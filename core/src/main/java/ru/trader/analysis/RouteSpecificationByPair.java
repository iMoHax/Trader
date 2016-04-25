package ru.trader.analysis;

import ru.trader.analysis.graph.Edge;
import ru.trader.analysis.graph.Traversal;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;

public class RouteSpecificationByPair<T> implements RouteSpecification<T> {
    private final Collection<T> first;
    private final T second;
    private final long start;
    private final long end;

    public RouteSpecificationByPair(T first, T second) {
        this(first, second, Long.MAX_VALUE);
    }

    public RouteSpecificationByPair(T first, T second, long time) {
        this(Collections.singleton(first), second, 0, time);
    }

    public RouteSpecificationByPair(Collection<T> first, T second) {
        this(first, second, 0, Long.MAX_VALUE);
    }

    public RouteSpecificationByPair(Collection<T> first, T second, long time) {
        this(first, second, 0, time);
    }

    public RouteSpecificationByPair(Collection<T> first, T second, long startTime, long endTime) {
        this.first = new HashSet<>(first);
        this.second = second;
        this.start = startTime;
        this.end = endTime;

    }

    public T getTarget(){
        return second;
    }

    public Collection<T> getTargets(){
        return first;
    }

    @Override
    public long getStart() {
        return start;
    }

    @Override
    public long getEnd() {
        return end;
    }

    private boolean checkTime(){
        return end < Long.MAX_VALUE;
    }

    @Override
    public boolean specified(Edge<T> edge, Traversal<T> entry) {
        return searchPair(edge, entry, false) == 0;
    }

    @Override
    public boolean content(Edge<T> edge, Traversal<T> entry) {
        if (checkTime()){
            long time = edge.getTime();
            if (time < start || time > end) return false;
        }
        T obj = edge.getTarget().getEntry();
        return second.equals(obj) || first.contains(obj);
    }

    @Override
    public int lastFound(Edge<T> edge, Traversal<T> entry) {
        return searchPair(edge, entry, true);
    }

    @Override
    public int maxMatches() {
        return 2;
    }

    private int searchPair(Edge<T> edge, Traversal<T> entry, boolean full){
        int fIndex = -1;
        int sIndex = -1;

        T obj = edge.getTarget().getEntry();
        boolean check = true;
        if (checkTime()){
            long time = edge.getTime();
            if (time < start || time > end) check = false;
        }
        if (check){
            if (second.equals(obj)){
                sIndex = 0;
            } else
            if (first.contains(obj)){
                fIndex = 0;
            }
        }

        int i = 0;
        Optional<Traversal<T>> t = Optional.of(entry);
        while (t.isPresent()){
            i++;
            Traversal<T> e = t.get();
            t = e.getHead();
            if (checkTime()){
                long time = e.getTime();
                if (time > end) continue;
                if (time < start) break;
            }
            T target = e.getTarget().getEntry();
            if (sIndex == -1 && second.equals(target)){
                sIndex = i;
            }
            //low index last
            if (sIndex != -1 && fIndex != -1 && fIndex >= sIndex){
                return 0;
            }
            if ((full || sIndex != -1) && (fIndex == -1 || fIndex < sIndex && sIndex != -1)  && first.contains(target)){
                fIndex = i;
            }
        }

        if (sIndex == -1 && fIndex != -1) return 1;
        if (fIndex == -1) return 2;
        return fIndex >= sIndex ? 0 : 1;
    }

}
