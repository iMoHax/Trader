package ru.trader.analysis;

import java.util.Collection;

public interface RouteSpecificationMixer<T> {

    RouteSpecification<T> mix(RouteSpecification<T> specification);
    Collection<RouteSpecification<T>> getMixed();

}