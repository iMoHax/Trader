package ru.trader.analysis;

import ru.trader.analysis.graph.Edge;
import ru.trader.analysis.graph.Traversal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class RouteSpecificationByPair<T> implements RouteSpecification<T> {
    protected final Collection<T> first;
    protected final T second;
    protected final long time;
    private boolean checkSecond;

    public RouteSpecificationByPair(T first, T second) {
        this(first, second, Long.MAX_VALUE);
    }

    public RouteSpecificationByPair(T first, T second, long time) {
        this.first = new ArrayList<>();
        this.first.add(first);
        this.second = second;
        this.time = time;
        checkSecond = true;
    }

    public RouteSpecificationByPair(Collection<T> first, T second) {
        this(first, second, Long.MAX_VALUE);
    }

    public RouteSpecificationByPair(Collection<T> first, T second, long time) {
        this.first = new ArrayList<>(first);
        this.second = second;
        this.time = time;
        checkSecond = true;
    }

    private boolean checkTime(){
        return time < Long.MAX_VALUE;
    }

    protected void remove(){
        checkSecond = false;
    }

    @Override
    public boolean specified(Edge<T> edge, Traversal<T> entry) {
        return searchPair(edge, entry) == 0;
    }

    @Override
    public boolean content(Edge<T> edge, Traversal<T> entry) {
        if (checkTime() && edge.getTime() + entry.getTime() > time) return false;
        T obj = edge.getTarget().getEntry();
        return second.equals(obj) || first.contains(obj);
    }

    @Override
    public int lastFound(Edge<T> edge, Traversal<T> entry) {
        return searchPair(edge, entry);
    }

    @Override
    public int matchCount() {
        return checkSecond ? 2 : 1;
    }

    private int searchPair(Edge<T> edge, Traversal<T> entry){
        int fIndex = -1;
        int sIndex = -1;
        List<Traversal<T>> entries = entry.toList();
        int max = entries.size();
        for (int i = 0; i < max; i++) {
            Traversal<T> e = entries.get(i);
            T target = e.getTarget().getEntry();
            if (second.equals(target)){
                if (checkTime() && e.getTime() > time) return checkSecond ? 2 : 1;
                sIndex = i;
            }
            if (sIndex != -1 && fIndex != -1 && fIndex <= sIndex){
                return 0;
            }
            if (fIndex == -1 && first.contains(target)){
                fIndex = i;
            }
        }
        T obj = edge.getTarget().getEntry();
        if (fIndex == -1 && first.contains(obj)){
            fIndex = max;
        }
        if (second.equals(obj)){
            if (checkTime() && edge.getTime() + entry.getTime() > time) return checkSecond ? 2 : 1;
            sIndex = max;
        }
        if (fIndex == -1) return checkSecond ? 2 : 1;
        if (sIndex == -1) return checkSecond ? 1 : 0;
        return fIndex <= sIndex ? 0 : 1;
    }

}
