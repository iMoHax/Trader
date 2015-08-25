package ru.trader.analysis;

import ru.trader.analysis.graph.Edge;
import ru.trader.core.Vendor;

import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

public class CrawlerSpecificationByTime extends SimpleCrawlerSpecification<Vendor> implements VendorsCrawlerSpecification {
    public CrawlerSpecificationByTime(Consumer<List<Edge<Vendor>>> onFoundFunc) {
        super(null, onFoundFunc, false);
    }

    public CrawlerSpecificationByTime(RouteSpecification<Vendor> routeSpecification, Consumer<List<Edge<Vendor>>> onFoundFunc, boolean loop) {
        super(routeSpecification, onFoundFunc, loop);
    }

    @Override
    public double computeWeight(VendorsCrawler.VendorsEdge edge) {
        double profit = edge.getProfitByTonne();
        return profit > 0 ? edge.getTime() + 1/profit : edge.getTime();
    }

    @Override
    public double computeWeight(VendorsCrawler.VendorsTraversalEntry entry) {
        double profit = 0; long time = 0;
        Iterator<Edge<Vendor>> iterator = entry.routeIterator();
        boolean first = true;
        while (iterator.hasNext()){
            VendorsCrawler.VendorsEdge edge = (VendorsCrawler.VendorsEdge)iterator.next();
            if (edge != null){
                if (!isLoop() || first || iterator.hasNext()){
                    profit += edge.getProfitByTonne();
                    time += edge.getTime();
                }
            }
            first = false;
        }
        return profit > 0 ? time + 1/profit : time + 0.999999999999999d;
    }

}
