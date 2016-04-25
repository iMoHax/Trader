package ru.trader.analysis;

import ru.trader.analysis.graph.Edge;
import ru.trader.analysis.graph.Traversal;

public interface RouteSpecification<T> {

    boolean specified(Edge<T> edge, Traversal<T> entry);
           default boolean content(Edge<T> edge, Traversal<T> entry){return specified(edge, entry);}
    default int lastFound(Edge<T> edge, Traversal<T> entry){
        return specified(edge, entry) ? 0 : minMatches();
    }
    default int maxMatches(){return 1;}
    default int minMatches(){return maxMatches();}
    default boolean updateMutated(){return false;}
    default boolean mutable(){return false;}
    default void update(Traversal<T> entry){}
    default long getStart(){return 0;}
    default long getEnd(){return Long.MAX_VALUE;}

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
