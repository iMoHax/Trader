package ru.trader.analysis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.analysis.graph.*;
import ru.trader.core.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class RouteSearcher {
    private final static Logger LOG = LoggerFactory.getLogger(RouteSearcher.class);
    private final Scorer scorer;
    private final AnalysisCallBack callback;

    public RouteSearcher(Scorer scorer) {
        this(scorer, new AnalysisCallBack());
    }

    public RouteSearcher(Scorer scorer, AnalysisCallBack callback) {
        this.scorer = scorer;
        this.callback = callback;
    }

    public Scorer getScorer() {
        return scorer;
    }

    private <T extends Connectable<T>> Edge<T> createEdge(T source, T target){
        ConnectibleEdge<T> edge = new ConnectibleEdge<>(new Vertex<>(source, 0), new Vertex<>(target, 0));
        Profile profile = getScorer().getProfile();
        Ship ship = profile.getShip();
        double fuelCost =  profile.withRefill() ? ship.getFuelCost(ship.getTank(), source.getDistance(target)) : 0;
        edge.setFuelCost(fuelCost);
        return edge;
    }

    public List<Edge<Place>> getPath(Place from, Place to, Collection<Place> places){
        if (from.equals(to)){
            return Collections.singletonList(createEdge(from, to));
        }
        List<List<Edge<Place>>> res = search(from, to, places, 1, null);
        return res.isEmpty() ? Collections.emptyList() : res.get(0);
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
        ConnectibleGraph<Place> graph = new ConnectibleGraph<>(profile, callback);
        LOG.trace("Build connectible graph");
        places = filtered(source, places);
        graph.build(source, places);
        LOG.trace("Graph is builds");
        List<List<Edge<Place>>> paths = new ArrayList<>();
        Crawler<Place> crawler = specification != null ?
                new CCrawler<>(graph, new SimpleCrawlerSpecification<>(specification, paths::add), callback) :
                new CCrawler<>(graph, paths::add, callback);
        crawler.setMaxSize(profile.getJumps());
        if (profile.getPathPriority() == Profile.PATH_PRIORITY.FAST){
            crawler.findFast(target, count);
        } else {
            crawler.findMin(target, count);
        }
        return paths;
    }

    public List<Route> search(Vendor source, Vendor target, Collection<Vendor> vendors, int count, CrawlerSpecificator specificator){
        LOG.trace("Start search route  from {} to {}", source, target);
        VendorsGraph vGraph = new VendorsGraph(scorer, callback);
        LOG.trace("Build vendors graph");
        vendors = filtered(source, vendors);
        vGraph.build(source, vendors);
        LOG.trace("Graph is builds");
        RouteCollector collector = new RouteCollector();
        VendorsCrawlerSpecification specification = specificator.build(vendors, collector::add);
        Crawler<Vendor> crawler = vGraph.crawler(specification, callback);
        int lands = specification.getMinLands();
        if (specificator.isFullScan()){
            lands = Math.max(scorer.getProfile().getLands(), lands);
        }
        crawler.setMaxSize(lands);
        crawler.findMin(target, count);
        return collector.get();
    }

    public List<Route> searchLoops(Vendor source, Collection<Vendor> vendors, CrawlerSpecificator specificator){
        LOG.trace("Start search loops from {}", source);
        VendorsGraph vGraph = new VendorsGraph(scorer, callback);
        LOG.trace("Build vendors graph");
        vendors = filtered(source, vendors);
        vGraph.build(source, vendors);
        LOG.trace("Graph is builds");
        RouteCollector collector = new RouteCollector();
        specificator.setGroupCount(vendors.size());

        VendorsCrawlerSpecification specification = specificator.build(vendors, collector::add, new LoopRouteSpecification<>(true), true);
        int lands = specification.getMinLands();
        if (specificator.isFullScan()){
            lands = Math.max(scorer.getProfile().getLands(), lands);
        }
        Crawler<Vendor> crawler = vGraph.crawler(specification, callback);
        crawler.setMaxSize(lands);
        crawler.findMin(source, vendors.size());
        specification = specificator.build(vendors, collector::add, new RouteSpecificationByTarget<>(source), false);
        lands = specification.getMinLands();
        if (specificator.isFullScan()){
            lands = Math.max(scorer.getProfile().getLands(), lands);
        }
        crawler = vGraph.crawler(specification, callback);
        crawler.setMaxSize(lands);
        crawler.findMin(source, 1);
        List<Route> routes = collector.get();
        routes.sort((r1, r2) -> {
            double s1 = (r1.getProfit() - r1.getEntries().get(0).getProfit()) / r1.getTime();
            double s2 = (r2.getProfit() - r2.getEntries().get(0).getProfit()) / r2.getTime();
            return Double.compare(s2, s1);
        });
        return routes;
    }

    protected <T extends Connectable<T>> Collection<T> filtered(T from, Collection<T> set){
        final double maxDistance = scorer.getProfile().getShip().getMaxJumpRange() * scorer.getProfile().getJumps();
        return set.parallelStream().filter(v -> from.getDistance(v) <= maxDistance).collect(Collectors.toList());
    }

    private class RouteCollector {
        private List<Route> routes = new ArrayList<>();

        public void add(List<Edge<Vendor>> edges){
            Route route = toRoute(edges, scorer);
            routes.add(route);
        }

        public List<Route> get() {
            return routes;
        }
    }

    private static RouteEntry find(Vendor vendor, List<RouteEntry> entries){
        for (int i = entries.size() - 1; i >= 0; i--) {
            RouteEntry entry = entries.get(i);
            if (entry.getVendor().equals(vendor)){
                return entry;
            }
        }
        throw new IllegalStateException(String.format("Wrong route, vendor %s not found", vendor));
    }

    public static Route toRoute(List<Edge<Vendor>> edges, final Scorer scorer){
        List<RouteEntry> entries = new ArrayList<>(edges.size()+1);
        Vendor buyer = null;
        VendorsCrawler.VendorsEdge vEdge = null;
        RouteEntry prev = null;
        for (Edge<Vendor> e : edges) {
            vEdge = (VendorsCrawler.VendorsEdge) e;
            List<ConnectibleEdge<Vendor>> transitEdges = vEdge.getPath().getEntries();
            for (int k = 0; k < transitEdges.size(); k++) {
                ConnectibleEdge<Vendor> edge = transitEdges.get(k);
                Vendor vendor = edge.getSource().getEntry();
                RouteEntry entry = new RouteEntry(vendor, edge.getRefill(), edge.getFuelCost(), 0);
                if (buyer != null && vendor.equals(buyer)) {
                    entry.setLand(true);
                    buyer = null;
                }
                if (k == 0) {
                    entry.setProfit(scorer.getProfit(vEdge.getProfit(), vEdge.getFuelCost()));
                    entry.setFullTime(vEdge.getTime());
                    List<Order> orders = vEdge.getOrders();
                    if (!orders.isEmpty()) {
                        Vendor seller = orders.get(0).getSeller();
                        RouteEntry sellerEntry;
                        if (entry.getVendor().equals(seller)){
                            sellerEntry = entry;
                        } else {
                            sellerEntry = find(seller, entries);
                            entry.setLand(true);
                        }
                        buyer = orders.get(0).getBuyer();
                        sellerEntry.addAll(orders);
                    } else {
                        if (!vendor.isTransit()){
                            entry.setLand(true);
                        }
                    }
                }
                if (prev != null){
                    prev.setTime(scorer.getTime(entry, prev));
                }
                entries.add(entry);
                prev = entry;
            }
        }
        if (vEdge != null) {
            RouteEntry entry = new RouteEntry(vEdge.getTarget().getEntry(), 0, 0, 0);
            if (!entry.getVendor().isTransit()) entry.setLand(true);
            if (prev != null){
                prev.setTime(scorer.getTime(entry, prev));
            }
            entries.add(entry);
        }
        Route route = new Route(entries);
        route.setBalance(scorer.getProfile().getBalance());
        route.setCargo(scorer.getProfile().getShip().getCargo());
        return route;
    }

    public static Route toRoute(Order order, List<Edge<Place>> edges, final Scorer scorer){
        Route route = toRoute(order.getSeller(), order.getBuyer(), edges, scorer);
        if (route.isEmpty()) return route;
        route.get(0).add(order);
        route.updateStats();
        return route;
    }

    public static Route toRoute(Vendor from, Vendor to, List<Edge<Place>> edges, final Scorer scorer){
        List<RouteEntry> entries = new ArrayList<>(edges.size()+1);
        RouteEntry prev = null;
        for (int i = 0; i < edges.size(); i++) {
            ConnectibleEdge<Place> edge = (ConnectibleEdge<Place>) edges.get(i);
            Vendor vendor = i == 0 ? from : edge.getSource().getEntry().asTransit();
            RouteEntry entry = new RouteEntry(vendor, edge.getRefill(), edge.getFuelCost(), 0);
            if (i == 0) entry.setLand(!vendor.isTransit());
            if (prev != null){
                prev.setTime(scorer.getTime(entry, prev));
                prev.setFullTime(prev.getTime());
            }
            entries.add(entry);
            prev = entry;
            if (i == edges.size()-1){
                entry = new RouteEntry(to, 0, 0, 0);
                entry.setLand(!to.isTransit());
                prev.setTime(scorer.getTime(entry, prev));
                prev.setFullTime(prev.getTime());
                entries.add(entry);
            }
        }
        Route route = new Route(entries);
        route.setBalance(scorer.getProfile().getBalance());
        route.setCargo(scorer.getProfile().getShip().getCargo());
        return route;
    }
}
