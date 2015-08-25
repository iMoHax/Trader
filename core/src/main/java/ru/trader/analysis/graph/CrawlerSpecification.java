package ru.trader.analysis.graph;

import ru.trader.analysis.RouteSpecification;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public interface CrawlerSpecification<T> {
    public RouteSpecification<T> routeSpecification();

    public Consumer<List<Edge<T>>> onFoundFunc();

    public Function<Traversal<T>, Object> getGroupGetter(boolean target);

    public int getGroupCount();

}
