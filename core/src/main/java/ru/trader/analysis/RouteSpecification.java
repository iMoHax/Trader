package ru.trader.analysis;

import ru.trader.analysis.graph.Edge;
import ru.trader.analysis.graph.Traversal;

public interface RouteSpecification<T> {

    public boolean specified(Edge<T> edge, Traversal<T> entry);
    public default boolean updateSpecified(Edge<T> edge, Traversal<T> entry){
        return specified(edge, entry);
    }

    default RouteSpecification<T> and(final RouteSpecification<T> other){
        return (edge, entry) -> RouteSpecification.this.specified(edge, entry) && other.specified(edge, entry);
    }

    default RouteSpecification<T> or(final RouteSpecification<T> other){
        return (edge, entry) -> RouteSpecification.this.specified(edge, entry) || other.specified(edge, entry);
    }

    default RouteSpecification<T> negate(){
        return (edge, entry) -> !specified(edge, entry);
    }
}
