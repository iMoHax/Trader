package ru.trader.analysis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.analysis.graph.Crawler;
import ru.trader.analysis.graph.Edge;
import ru.trader.core.Order;
import ru.trader.core.TransitVendor;
import ru.trader.core.Vendor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RouteSearcher {
    private final static Logger LOG = LoggerFactory.getLogger(RouteSearcher.class);
    private final VendorsGraph vGraph;

    public RouteSearcher(Scorer scorer) {
        vGraph = new VendorsGraph(scorer);
    }

    public List<Route> getRoutes(Vendor from, Vendor to, Collection<Vendor> vendors){
        return search(from, to, vendors);
    }

    public List<Route> getRoutes(Vendor from, Collection<Vendor> vendors){
        return search(from, null, vendors);
    }

    private List<Route> search(Vendor source, Vendor target, Collection<Vendor> vendors){
        LOG.trace("Start search route to {} from {}", source, target);
        vGraph.build(source, vendors);

        RouteCollector collector = new RouteCollector();
        Crawler<Vendor> crawler = vGraph.crawler(collector::add);
        if (target == null){
            int count = vGraph.getProfile().getRoutesCount() / vendors.size();
            for (Vendor vendor : vendors) {
                crawler.findMin(vendor, count);
            }
        } else {
            crawler.findMin(target, vGraph.getProfile().getRoutesCount());
        }
        return collector.get();

    }

    private class RouteCollector {
        private List<Route> routes = new ArrayList<>();

        public void add(List<Edge<Vendor>> edges){
            Route route = toRoute(edges);
            route.setBalance(vGraph.getProfile().getBalance());
            routes.add(route);
        }

        public List<Route> get() {
            return routes;
        }

        private Route toRoute(List<Edge<Vendor>> edges){
            List<RouteEntry> entries = new ArrayList<>(edges.size()+1);
            Vendor buyer = null;
            VendorsGraph.VendorsEdge edge = null;
            for (int i = 0; i < edges.size(); i++) {
                edge = (VendorsGraph.VendorsEdge) edges.get(i);
                Vendor vendor = edge.getSource().getEntry();
                RouteEntry entry = new RouteEntry(vendor, edge.isRefill(), edge.getFuel(), edge.getWeight());
                if (buyer != null && vendor.equals(buyer)){
                    entry.setLand(true);
                }
                List<Order> orders = edge.getOrders();
                if (!orders.isEmpty()){
                    buyer = orders.get(0).getBuyer();
                    if (vendor instanceof TransitVendor){
                        Vendor seller = orders.get(0).getSell().getVendor();
                        for (int j = i-1; j >= 0; j--) {
                            RouteEntry sEntry = entries.get(j);
                            if (sEntry.is(seller)){
                                sEntry.addAll(orders);
                                break;
                            }
                        }
                    } else {
                        entry.addAll(orders);
                    }
                }
                entries.add(entry);
            }
            if (edge != null) {
                RouteEntry entry = new RouteEntry(edge.getTarget().getEntry(), false, 0, 0);
                if (buyer != null) entry.setLand(true);
                entries.add(entry);
            }
            return new Route(entries);
        }

    }
}
