package ru.trader.analysis;

import ru.trader.analysis.graph.Edge;
import ru.trader.core.Vendor;

import java.util.List;
import java.util.function.Consumer;

public interface CrawlerSpecification {

    public double computeWeight(VendorsCrawler.VendorsEdge edge);

    public double computeWeight(VendorsCrawler.VendorsTraversalEntry entry);

    public RouteSpecification<Vendor> routeSpecification();

    public Consumer<List<Edge<Vendor>>> onFoundFunc();

}
