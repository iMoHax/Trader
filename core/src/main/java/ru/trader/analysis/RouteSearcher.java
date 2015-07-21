package ru.trader.analysis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.analysis.graph.*;
import ru.trader.core.Order;
import ru.trader.core.Place;
import ru.trader.core.Profile;
import ru.trader.core.Vendor;

import java.util.*;

public class RouteSearcher {
    private final static Logger LOG = LoggerFactory.getLogger(RouteSearcher.class);
    private final Scorer scorer;

    public RouteSearcher(Scorer scorer) {
        this.scorer = scorer;
    }

    public List<Edge<Place>> getPath(Place from, Place to, Collection<Place> places){
        List<List<Edge<Place>>> res = search(from, to, places, 1, null);
        return res.isEmpty() ? null : res.get(0);
    }

    public List<List<Edge<Place>>> getPaths(Place from, Collection<Place> places){
        return search(from, from, places, scorer.getProfile().getRoutesCount(), RouteSpecificationByTargets.any(places));
    }

    public List<List<Edge<Place>>> getPaths(Place from, Place to, Collection<Place> places){
        return search(from, to, places, scorer.getProfile().getRoutesCount(), null);
    }

    private List<List<Edge<Place>>> search(Place source, Place target, Collection<Place> places, int count, RouteSpecification<Place> specification){
        Profile profile = scorer.getProfile();
        LOG.trace("Start search path from {} to {} ", source, target);
        ConnectibleGraph<Place> graph = new ConnectibleGraph<>(profile);
        LOG.trace("Build connectible graph");
        graph.build(source, places);
        LOG.trace("Graph is builds");
        List<List<Edge<Place>>> paths = new ArrayList<>();
        Crawler<Place> crawler = specification != null ?  new CCrawler<>(graph, specification, paths::add) :  new CCrawler<>(graph, paths::add);
        crawler.setMaxSize(profile.getJumps());
        if (profile.getPathPriority() == Profile.PATH_PRIORITY.FAST){
            crawler.findFast(target, count);
        } else {
            crawler.findMin(target, count);
        }
        return paths;
    }

    public List<Route> getRoutes(Collection<Vendor> fVendors, Collection<Vendor> vendors){
        return getRoutes(fVendors, vendors, vendors);
    }

    public List<Route> getRoutes(Collection<Vendor> fVendors, Collection<Vendor> toVendors, Collection<Vendor> vendors){
        List<Route> res = new LimitedQueue<>(scorer.getProfile().getRoutesCount());
        int count = (int) Math.ceil(scorer.getProfile().getRoutesCount() / fVendors.size());
        RouteSpecification<Vendor> specification = RouteSpecificationByTargets.any(toVendors);
        for (Vendor fromVendor : fVendors) {
            count = count / toVendors.size();
            Collection<Route> routes = search(fromVendor, fromVendor, vendors, count, specification);
            res.addAll(routes);
        }
        return res;
    }

    public List<Route> getRoutes(Vendor from, Collection<Vendor> vendors){
        return search(from, from, vendors, scorer.getProfile().getRoutesCount(), RouteSpecificationByTargets.any(vendors));
    }

    public List<Route> getRoutes(Vendor from, Vendor to, Collection<Vendor> vendors){
        return getRoutes(from, to, vendors, scorer.getProfile().getRoutesCount());
    }

    public List<Route> getRoutes(Vendor source, Vendor target, Collection<Vendor> vendors, int count){
        return search(source, target, vendors, count, null);
    }

    private List<Route> search(Vendor source, Vendor target, Collection<Vendor> vendors, int count, RouteSpecification<Vendor> specification){
        LOG.trace("Start search route  from {} to {}", source, target);
        VendorsGraph vGraph = new VendorsGraph(scorer);
        LOG.trace("Build vendors graph");
        vGraph.build(source, vendors);
        LOG.trace("Graph is builds");
        RouteCollector collector = new RouteCollector();
        Crawler<Vendor> crawler = specification != null ? vGraph.crawler(specification, collector::add) : vGraph.crawler(collector::add);
        crawler.setMaxSize(scorer.getProfile().getLands());
        crawler.findMin(target, count);
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
    }

    public static Route toRoute(List<Edge<Vendor>> edges){
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

    public static Route toRoute(Order order, List<Edge<Place>> edges){
        Route route = toRoute(order.getSeller(), order.getBuyer(), edges);
        if (route.isEmpty()) return route;
        route.get(0).add(order);
        route.updateStats();
        return route;
    }

    public static Route toRoute(Vendor from, Vendor to, List<Edge<Place>> edges){
        List<RouteEntry> entries = new ArrayList<>(edges.size()+1);
        for (int i = 0; i < edges.size(); i++) {
            ConnectibleEdge<Place> edge = (ConnectibleEdge<Place>) edges.get(i);
            Vendor vendor = i == 0 ? from : edge.getSource().getEntry().asTransit();
            RouteEntry entry = new RouteEntry(vendor, edge.isRefill(), edge.getFuelCost(), 0);
            entries.add(entry);
            if (i == edges.size()-1){
                entry = new RouteEntry(to, false, 0, 0);
                entry.setLand(true);
                entries.add(entry);
            }
        }
        return new Route(entries);
    }
}
