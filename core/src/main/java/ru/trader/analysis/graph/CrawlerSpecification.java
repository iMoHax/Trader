package ru.trader.analysis.graph;

import ru.trader.analysis.RouteSpecification;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public interface CrawlerSpecification<T> {
    RouteSpecification<T> routeSpecification();

    Consumer<List<Edge<T>>> onFoundFunc();

    Function<Traversal<T>, Object> getGroupGetter(boolean target);

    int getGroupCount();

    default int getMinLands(){
        return routeSpecification() != null ? routeSpecification().minMatches() : 1;
    }

    default int getMaxLands(){
        return routeSpecification() != null ? routeSpecification().maxMatches() : 1;
    }
}
