package ru.trader.analysis;

import ru.trader.analysis.graph.Edge;
import ru.trader.core.Vendor;

import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

public class CrawlerSpecificationByTime extends AbstractCrawlerSpecification {
    public CrawlerSpecificationByTime(Consumer<List<Edge<Vendor>>> onFoundFunc) {
        super(null, onFoundFunc);
    }

    public CrawlerSpecificationByTime(RouteSpecification<Vendor> routeSpecification, Consumer<List<Edge<Vendor>>> onFoundFunc) {
        super(routeSpecification, onFoundFunc);
    }

    @Override
    public double computeWeight(VendorsCrawler.VendorsEdge edge) {
        return edge.getTime();
    }

    @Override
    public double computeWeight(VendorsCrawler.VendorsTraversalEntry entry) {
        double time = 0;
        Iterator<Edge<Vendor>> iterator = entry.routeIterator();
        while (iterator.hasNext()){
            VendorsCrawler.VendorsEdge edge = (VendorsCrawler.VendorsEdge)iterator.next();
            if (edge != null){
                time += edge.getTime();
            }
        }
        return time;
    }

}
