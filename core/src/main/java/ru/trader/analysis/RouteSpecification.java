package ru.trader.analysis;

import ru.trader.analysis.graph.Edge;
import ru.trader.analysis.graph.Traversal;

public interface RouteSpecification<T> {

    public boolean specified(Edge<T> edge, Traversal<T> entry);
           default boolean content(Edge<T> edge, Traversal<T> entry){return specified(edge, entry);}
    public default int lastFound(Edge<T> edge, Traversal<T> entry){
        return specified(edge, entry) ? 0 : matchCount();
    }
    public default int matchCount(){return 1;}
    public default boolean updateMutated(){return false;}
    public default boolean mutable(){return false;}
    public default void update(Traversal<T> entry){}
    public default long getStart(){return 0;}
    public default long getEnd(){return Long.MAX_VALUE;}

    default RouteSpecification<T> and(final RouteSpecification<T> other){
        if (other instanceof RouteSpecificationAndMixer){
            ((RouteSpecificationAndMixer<T>) other).mix(this);
            return other;
        }
        return RouteSpecificationAndMixer.mix(this, other);
    }

    default RouteSpecification<T> or(final RouteSpecification<T> other){
        if (other instanceof RouteSpecificationOrMixer){
            ((RouteSpecificationOrMixer<T>) other).mix(this);
            return other;
        }
        return RouteSpecificationOrMixer.mix(this, other);
    }

}
