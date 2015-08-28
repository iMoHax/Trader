package ru.trader.analysis;

import ru.trader.analysis.graph.Edge;
import ru.trader.analysis.graph.Traversal;

public interface RouteSpecification<T> {

    public boolean specified(Edge<T> edge, Traversal<T> entry);
    public default int lastFound(Edge<T> edge, Traversal<T> entry){
        return specified(edge, entry) ? 0 : matchCount();
    }
    public default int matchCount(){return 1;}
    public default boolean updateMutated(){return false;}
    public default boolean mutable(){return false;}
    public default void update(Traversal<T> entry){}

    default RouteSpecification<T> and(final RouteSpecification<T> other){
        return new RouteSpecification<T>() {
            @Override
            public boolean specified(Edge<T> edge, Traversal<T> entry) {
                return RouteSpecification.this.specified(edge, entry) && other.specified(edge, entry);
            }

            @Override
            public int lastFound(Edge<T> edge, Traversal<T> entry) {
                return RouteSpecification.this.lastFound(edge, entry) + other.lastFound(edge, entry);
            }

            @Override
            public int matchCount() {
                return RouteSpecification.this.matchCount() + other.matchCount();
            }

            @Override
            public boolean updateMutated() {
                return RouteSpecification.this.updateMutated() || other.updateMutated();
            }

            @Override
            public boolean mutable() {
                return RouteSpecification.this.mutable() || other.mutable();
            }

            @Override
            public void update(Traversal<T> entry) {
                RouteSpecification.this.update(entry);
                other.update(entry);
            }
        };
    }

    default RouteSpecification<T> or(final RouteSpecification<T> other){
        return new RouteSpecification<T>() {
            @Override
            public boolean specified(Edge<T> edge, Traversal<T> entry) {
                return RouteSpecification.this.specified(edge, entry) || other.specified(edge, entry);
            }

            @Override
            public int lastFound(Edge<T> edge, Traversal<T> entry) {
                return Math.min(RouteSpecification.this.lastFound(edge, entry), other.lastFound(edge, entry));
            }

            @Override
            public int matchCount() {
                return Math.min(RouteSpecification.this.matchCount(), other.matchCount());
            }

            @Override
            public boolean updateMutated() {
                return RouteSpecification.this.updateMutated() || other.updateMutated();
            }
            @Override
            public boolean mutable() {
                return RouteSpecification.this.mutable() || other.mutable();
            }

            @Override
            public void update(Traversal<T> entry) {
                RouteSpecification.this.update(entry);
                other.update(entry);
            }
        };
    }

    default RouteSpecification<T> negate(){
        return (edge, entry) -> !specified(edge, entry);
    }
}
