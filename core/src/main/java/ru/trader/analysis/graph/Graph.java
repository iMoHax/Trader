package ru.trader.analysis.graph;

import java.util.Collection;
import java.util.Optional;

public interface Graph<T> {
    boolean isAccessible(T entry);

    Optional<Vertex<T>> getVertex(T entry);

    Vertex<T> getRoot();

    Collection<Vertex<T>> vertexes();

    int getMinJumps();

    int getMinLevel();

    int getSize();
}
