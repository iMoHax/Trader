package ru.trader.analysis;


import ru.trader.analysis.graph.CrawlerSpecification;
import ru.trader.analysis.graph.Edge;
import ru.trader.analysis.graph.Traversal;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public class SimpleCrawlerSpecification<T> implements CrawlerSpecification<T> {
    private final RouteSpecification<T> routeSpecification;
    private final Consumer<List<Edge<T>>> onFoundFunc;
    private final boolean loop;
    private int groupCount;

    public SimpleCrawlerSpecification(Consumer<List<Edge<T>>> onFoundFunc) {
        this(null, onFoundFunc, false);
    }

    public SimpleCrawlerSpecification(RouteSpecification<T> routeSpecification, Consumer<List<Edge<T>>> onFoundFunc) {
        this(routeSpecification, onFoundFunc, false);
    }

    public SimpleCrawlerSpecification(RouteSpecification<T> routeSpecification, Consumer<List<Edge<T>>> onFoundFunc, boolean loop) {
        this.routeSpecification = routeSpecification;
        this.onFoundFunc = onFoundFunc;
        this.loop = loop;
    }

    protected boolean isLoop() {
        return loop;
    }

    public void setGroupCount(int groupCount) {
        this.groupCount = groupCount;
    }

    @Override
    public RouteSpecification<T> routeSpecification() {
        return routeSpecification;
    }

    @Override
    public Consumer<List<Edge<T>>> onFoundFunc() {
        return onFoundFunc;
    }

    public Function<Traversal<T>, Object> getQueueGroupGetter(){
        return e -> {
            Traversal<T> curr = e;
            Traversal<T> target = e;
            Optional<Traversal<T>> head = e.getHead();
            while (head.isPresent()) {
                target = curr;
                curr = head.get();
                head = curr.getHead();
            }
            return target.getTarget();
        };
    }

    @Override
    public Function<Traversal<T>, Object> getGroupGetter(boolean target) {
        if (target){
            return Traversal::getTarget;
        } else {
            return getQueueGroupGetter();
        }
    }

    @Override
    public int getGroupCount() {
        return groupCount;
    }
}
