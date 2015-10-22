package ru.trader.analysis;

import ru.trader.analysis.graph.Edge;
import ru.trader.analysis.graph.Traversal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class RouteSpecificationByPair<T> implements RouteSpecification<T> {
    protected final Collection<T> first;
    protected final T second;
    private boolean checkSecond;

    public RouteSpecificationByPair(T first, T second) {
        this.first = new ArrayList<>();
        this.first.add(first);
        this.second = second;
        checkSecond = true;
    }

    public RouteSpecificationByPair(Collection<T> first, T second) {
        this.first = new ArrayList<>(first);
        this.second = second;
        checkSecond = true;
    }

    @Override
    public boolean specified(Edge<T> edge, Traversal<T> entry) {
        return searchPair(edge, entry) == 0;
    }

    @Override
    public boolean content(Edge<T> edge, Traversal<T> entry) {
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
        T obj = edge.getTarget().getEntry();
        int fIndex = -1;
        int sIndex = -1;
        if (first.contains(obj)){
            fIndex = 0;
        }
        if (second.equals(obj)){
            sIndex = 0;
        }
        if (sIndex != -1 && fIndex >= sIndex) return 0;

        Iterator<Edge<T>> iterator = entry.routeIterator();
        int i = 1;
        while (iterator.hasNext()){
            obj = iterator.next().getTarget().getEntry();
            if (sIndex == -1 && second.equals(obj)){
                sIndex = i;
            }
            if (sIndex != -1 && fIndex >= sIndex){
                return 0;
            }
            if (first.contains(obj)){
                fIndex = i;
            }
            i++;
        }
        if (fIndex == -1) return checkSecond ? 2 : 1;
        if (sIndex == -1) return 1;
        return fIndex >= sIndex ? 0 : 1;
    }


    @Override
    public void onAnd(RouteSpecification<T> other) {
        if (other instanceof RouteSpecificationByTarget){
            if (checkSecond){
                T otherTarget = ((RouteSpecificationByTarget<T>)other).target;
                checkSecond = !second.equals(otherTarget);
            }
        } else
        if (other instanceof RouteSpecificationByPair){
            RouteSpecificationByPair<T> os = (RouteSpecificationByPair<T>)other;
            if (checkSecond && os.checkSecond){
                checkSecond = !second.equals(os.second);
            }
        }
    }
}
