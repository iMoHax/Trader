package ru.trader.analysis;

import ru.trader.analysis.graph.Edge;
import ru.trader.analysis.graph.Traversal;

public interface RouteSpecification<T> {

    public boolean specified(Edge<T> edge, Traversal<T> entry);
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
