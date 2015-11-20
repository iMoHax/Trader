package ru.trader.analysis.graph;

import java.util.*;

public interface Traversal<T> {
    Vertex<T> getTarget();
    Optional<Traversal<T>> getHead();
    Edge<T> getEdge();
    List<Edge<T>> getEdges();
    Iterator<Edge<T>> iterator();
    void sort();
    void setSkipped(boolean skipped);
    boolean isSkipped();
    boolean containsSkipped();

    default boolean isConnect(T target){
        Edge<T> edge = getEdge();
        return edge != null && edge.isConnect(target);
    }

    default boolean isRoot(){
        return !getHead().isPresent();
    }

    default int size(){
        int s = 0;
        Optional<Traversal<T>> t = this.getHead();
        while (t.isPresent()){
            s++;
            t = t.get().getHead();
        }
        return s;
    }

    default long getTime(){
        Edge<T> edge = this.getEdge();
        long t = edge != null ? edge.getTime() : 0;
        if (this.getHead().isPresent()){
            t += getHead().get().getTime();
        }
        return t;
    }

    @SuppressWarnings("unchecked")
    default List<Edge<T>> toEdges(){
        int s = size();
        Edge<T>[] res = new Edge[s];
        int i = s - 1;
        Traversal<T> entry = this;
        while (i >= 0){
            Edge<T> edge = entry.getEdge();
            res[i] = edge;
            if (i > 0)
                entry = entry.getHead().get();
            i--;
        }
        return Arrays.asList(res);
    }

    @SuppressWarnings("unchecked")
    default List<Traversal<T>> toList(){
        int s = size();
        Traversal<T>[] res = new Traversal[s];
        int i = s - 1;
        Traversal<T> entry = this;
        while (i >= 0){
            res[i]=entry;
            if (i > 0)
                entry = entry.getHead().get();
            i--;
        }
        return Arrays.asList(res);

    }

    default Iterator<Edge<T>> routeIterator(){
        return new Iterator<Edge<T>>() {
            private Edge<T> next = getEdge();
            private Traversal<T> entry = Traversal.this;

            @Override
            public boolean hasNext() {
                return next != null;
            }

            @Override
            public Edge<T> next() {
                Edge<T> res = next;
                Optional<Traversal<T>> head = entry.getHead();
                if (head.isPresent()){
                    entry = head.get();
                    next = entry.getEdge();
                } else {
                    next = null;
                }
                return res;
            }
        };
    }
}
