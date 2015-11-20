package ru.trader.analysis.graph;

import org.jetbrains.annotations.NotNull;

public abstract class Edge<T> implements Comparable<Edge>{
    protected Double weight;
    protected final Vertex<T> target;
    protected final Vertex<T> source;

    protected Edge(Vertex<T> source, Vertex<T> target) {
        this.target = target;
        this.source = source;
    }

    protected abstract double computeWeight();

    public Vertex<T> getTarget(){
        return target;
    }

    public Vertex<T> getSource() {
        return source;
    }

    public double getWeight(){
        if (weight == null){
            weight = computeWeight();
        }
        return weight;
    }

    public boolean isConnect(T other){
        return target.getEntry().equals(other);
    }

    public long getTime(){
        return 1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Edge edge = (Edge) o;
        return source.equals(edge.source) && target.equals(edge.target);
    }

    @Override
    public int hashCode() {
        int result = target.hashCode();
        result = 31 * result + source.hashCode();
        return result;
    }

    @Override
    public int compareTo(@NotNull Edge other) {
        return Double.compare(getWeight(), other.getWeight());
    }

    @Override
    public String toString() {
        return source.getEntry().toString() + " - "+ weight +" -> " + target.getEntry().toString();
    }
}
