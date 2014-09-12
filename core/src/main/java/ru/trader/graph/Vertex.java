package ru.trader.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;

public class Vertex<T extends Connectable<T>> {
    private final ArrayList<Edge<T>> edges = new ArrayList<>();
    private final T entry;
    private volatile int level = -1;

    public Vertex(T entry) {
        this.entry = entry;
    }

    public T getEntry() {
        return entry;
    }

    public boolean isConnected(Vertex<T> other){
        return isConnected(other.entry);
    }

    public boolean isConnected(T other){
        return edges.stream().anyMatch((e) -> e.isConnect(other));
    }

    public void addEdge(Edge<T> edge){
        synchronized (edges){
            if (edges.contains(edge)) return;
            edges.add(edge);
        }
    }

    public Collection<Edge<T>> getEdges() {
        return edges;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vertex vertex = (Vertex) o;
        return entry.equals(vertex.entry);

    }

    @Override
    public int hashCode() {
        return entry.hashCode();
    }

    public void sortEdges(Comparator<Edge> comparator){
        edges.sort(comparator);
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getEdgesCount(){
        return edges.size();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Vertex{");
        sb.append(entry);
        sb.append(", lvl=").append(level);
        sb.append('}');
        return sb.toString();
    }

    public Edge<T> getEdge(Vertex<T> target) {
        for (Edge<T> edge : edges) {
            if (edge.isConnect(target)) return edge;
        }
        return null;
    }
}
