package ru.trader.analysis;

public interface MutableRouteSpecification<T> extends RouteSpecification<T> {

    @Override
    default boolean updateMutated(){return true;}
    @Override
    default boolean mutable(){return true;}

}
