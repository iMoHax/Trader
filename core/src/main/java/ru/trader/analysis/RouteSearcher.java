package ru.trader.analysis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.analysis.graph.ConnectibleEdge;
import ru.trader.analysis.graph.Crawler;
import ru.trader.analysis.graph.Edge;
import ru.trader.core.Order;
import ru.trader.core.Vendor;

import java.util.*;

public class RouteSearcher {
    private final static Logger LOG = LoggerFactory.getLogger(RouteSearcher.class);
    private final Scorer scorer;

    public RouteSearcher(Scorer scorer) {
        this.scorer = scorer;
    }

    public List<Route> getRoutes(Vendor from, Vendor to, Collection<Vendor> vendors){
        return search(from, to, vendors);
    }

    public List<Route> getRoutes(Vendor from, Collection<Vendor> vendors){
        return search(from, null, vendors);
    }

    private List<Route> search(Vendor source, Vendor target, Collection<Vendor> vendors){
        LOG.trace("Start search route to {} from {}", source, target);
        VendorsGraph vGraph = new VendorsGraph(scorer);
        LOG.trace("Build vendors graph");
        vGraph.build(source, vendors);
        LOG.trace("Graph is builds");
        RouteCollector collector = new RouteCollector();
        Crawler<Vendor> crawler = vGraph.crawler(collector::add);
        crawler.setMaxSize(scorer.getProfile().getLands());
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

        public boolean add(List<Edge<Vendor>> edges){
            Route route = toRoute(edges);
            route.setBalance(scorer.getProfile().getBalance());
            routes.add(route);
            return true;
        }

        public List<Route> get() {
            return routes;
        }

        private Route toRoute(List<Edge<Vendor>> edges){
            List<RouteEntry> entries = new ArrayList<>(edges.size()+1);
            Vendor buyer = null;
            VendorsGraph.VendorsEdge vEdge = null;
            for (Edge<Vendor> e : edges) {
                vEdge = (VendorsGraph.VendorsEdge) e;
                List<ConnectibleEdge<Vendor>> transitEdges = vEdge.getPath().getEntries();
                for (int k = 0; k < transitEdges.size(); k++) {
                    ConnectibleEdge<Vendor> edge = transitEdges.get(k);
                    Vendor vendor = edge.getSource().getEntry();
                    RouteEntry entry = new RouteEntry(vendor, edge.isRefill(), edge.getFuelCost(), 0);
                    if (buyer != null && vendor.equals(buyer)) {
                        entry.setLand(true);
                        buyer = null;
                    }
                    if (k == 0) {
                        entry.setScore(vEdge.getWeight());
                        List<Order> orders = vEdge.getOrders();
                        if (!orders.isEmpty()) {
                            buyer = orders.get(0).getBuyer();
                            entry.addAll(orders);
                        }
                    }
                    entries.add(entry);
                }
            }
            if (vEdge != null) {
                RouteEntry entry = new RouteEntry(vEdge.getTarget().getEntry(), false, 0, 0);
                if (buyer != null) entry.setLand(true);
                entries.add(entry);
            }
            return new Route(entries);
        }

    }
}
