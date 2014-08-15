package ru.trader.graph;

public class Edge<T extends Connectable<T>> {
    protected double length;
    protected final Vertex<T> target;
    protected final Vertex<T> source;

    public Edge(Vertex<T> source, T target) {
        this(source, new Vertex<>(target));
    }

    public Edge(Vertex<T> source, Vertex<T> target) {
        this.target = target;
        this.source = source;
        this.length = source.getEntry().getDistance(target.getEntry());
    }

    public double getLength(){
        return length;
    }

    public boolean isConnect(T other){
        return target.getEntry().equals(other);
    }

    public boolean isConnect(Vertex<T> target){
        return getTarget().equals(target);
    }

    public Vertex<T> getTarget(){
        return target;
    }

    public Vertex<T> getSource() {
        return source;
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
    public String toString() {
        return source.toString() + " -> " + target.toString();
    }
}
