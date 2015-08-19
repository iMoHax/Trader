package ru.trader.analysis;

import ru.trader.analysis.graph.Edge;
import ru.trader.core.Vendor;

import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

public class CrawlerSpecificationByProfit extends AbstractCrawlerSpecification {

    public CrawlerSpecificationByProfit(Consumer<List<Edge<Vendor>>> onFoundFunc) {
        super(null, onFoundFunc);
    }

    public CrawlerSpecificationByProfit(RouteSpecification<Vendor> routeSpecification, Consumer<List<Edge<Vendor>>> onFoundFunc) {
        super(routeSpecification, onFoundFunc);
    }

    @Override
    public double computeWeight(VendorsCrawler.VendorsEdge edge) {
        return edge.getTime()/edge.getProfitByTonne();
    }

    @Override
    public double computeWeight(VendorsCrawler.VendorsTraversalEntry entry) {
        double profit = 0; double time = 0;
        Iterator<Edge<Vendor>> iterator = entry.routeIterator();
        while (iterator.hasNext()){
            VendorsCrawler.VendorsEdge edge = (VendorsCrawler.VendorsEdge)iterator.next();
            if (edge != null){
                profit += edge.getProfitByTonne();
                time += edge.getTime();
            }
        }
        return profit > 1 ? time / profit : time;
    }

}
