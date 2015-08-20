package ru.trader.analysis;


import ru.trader.analysis.graph.Edge;
import ru.trader.core.Vendor;

import java.util.List;
import java.util.function.Consumer;

public abstract class AbstractCrawlerSpecification implements CrawlerSpecification {
    private final RouteSpecification<Vendor> routeSpecification;
    private final Consumer<List<Edge<Vendor>>> onFoundFunc;
    private final boolean loop;

    protected AbstractCrawlerSpecification(RouteSpecification<Vendor> routeSpecification, Consumer<List<Edge<Vendor>>> onFoundFunc, boolean loop) {
        this.routeSpecification = routeSpecification;
        this.onFoundFunc = onFoundFunc;
        this.loop = loop;
    }

    protected boolean isLoop() {
        return loop;
    }

    @Override
    public RouteSpecification<Vendor> routeSpecification() {
        return routeSpecification;
    }

    @Override
    public Consumer<List<Edge<Vendor>>> onFoundFunc() {
        return onFoundFunc;
    }
}
