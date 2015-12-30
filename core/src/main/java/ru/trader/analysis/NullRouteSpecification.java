package ru.trader.analysis;


import ru.trader.analysis.graph.Edge;
import ru.trader.analysis.graph.Traversal;

public class NullRouteSpecification<T> implements RouteSpecification<T> {
    @Override
    public boolean specified(Edge<T> edge, Traversal<T> entry) {
        return false;
    }

    @Override
    public boolean content(Edge<T> edge, Traversal<T> entry) {
        return false;
    }

    @Override
    public int lastFound(Edge<T> edge, Traversal<T> entry) {
        return Integer.MAX_VALUE;
    }

    @Override
    public int matchCount() {
        return 1;
    }
}
