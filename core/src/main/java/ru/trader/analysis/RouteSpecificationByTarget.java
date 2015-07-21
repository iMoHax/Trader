package ru.trader.analysis;

import ru.trader.analysis.graph.Edge;
import ru.trader.analysis.graph.Traversal;

public class RouteSpecificationByTarget<T> implements RouteSpecification<T> {
    protected final T target;

    public RouteSpecificationByTarget(T target) {
        this.target = target;
    }

    @Override
    public boolean specified(Edge<T> edge, Traversal<T> entry) {
        return edge.isConnect(target);
    }
}
