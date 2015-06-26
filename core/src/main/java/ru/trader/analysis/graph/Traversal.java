package ru.trader.analysis.graph;

import java.util.*;

public interface Traversal<T> {
    Vertex<T> getTarget();
    Optional<Traversal<T>> getHead();
    Edge<T> getEdge();
    List<Edge<T>> getEdges();
    Iterator<Edge<T>> iterator();
    void sort();

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
}
