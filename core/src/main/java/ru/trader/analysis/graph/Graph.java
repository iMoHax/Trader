package ru.trader.analysis.graph;

public interface Graph<T> {
    boolean isAccessible(T entry);

    Vertex<T> getVertex(T entry);

    Vertex<T> getRoot();

    int getMinJumps();

    int getMinLevel();
}
