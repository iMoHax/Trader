package ru.trader.analysis.graph;

import java.util.Arrays;
import java.util.List;

public class PPath<T> {
    private final T[] points;

    private PPath(T[] points) {
        this.points = points;
    }

    private PPath(List<Edge<T>> edges) {
        //noinspection unchecked
        points = (T[]) new Object[edges.size()+1];
        for (int i = 0; i < edges.size(); i++) {
            Edge<T> edge = edges.get(i);
            if (i > 0 && !points[i].equals(edge.getSource().getEntry())){
                throw new IllegalArgumentException(String.format("Edges by index %d and %d is not linked", i-1, i));
            } else {
                points[i] = edge.getSource().getEntry();
            }
            points[i+1] = edge.getTarget().getEntry();
        }
    }

    @SafeVarargs
    public static <V> PPath<V> of(V... entries){
        return new PPath<>(entries);
    }

    public static <V> PPath<V> of(List<Edge<V>> edges){
        return new PPath<>(edges);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PPath)) return false;
        PPath pPath = (PPath) o;
        return Arrays.equals(points, pPath.points);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(points);
    }

    @Override
    public String toString() {
        return  Arrays.toString(points);
    }
}
