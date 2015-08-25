package ru.trader.analysis;

public interface MutableRouteSpecification<T> extends RouteSpecification<T> {

    @Override
    public default boolean updateMutated(){return true;}
    @Override
    public default boolean mutable(){return true;}

}
