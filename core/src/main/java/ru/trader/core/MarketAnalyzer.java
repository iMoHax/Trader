package ru.trader.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.graph.*;

import java.util.*;

public class MarketAnalyzer {
    private final static Logger LOG = LoggerFactory.getLogger(MarketAnalyzer.class);

    private Market market;
    private double tank;
    private double maxDistance;
    private int segmentSize;
    private int limit;
    private int jumps;
    private int cargo;

    private final static Comparator<Order> orderComparator = (o1, o2) -> o2.compareTo(o1);

    public MarketAnalyzer(Market market) {
        this.market = market;
        this.limit = 100;
        this.segmentSize = 0;
    }

    public Collection<Order> getTop(double balance){
        LOG.debug("Get top {}", limit);
        Iterable<Place> places = market.get();
        List<Order> top = new ArrayList<>(limit);
        for (Place place : places) {
             LOG.trace("Check place {}", place);
             Collection<Order> orders = getOrders(place, balance, top.isEmpty() ? 0 : top.get(top.size()-1).getProfit());
             TopList.addAllToTop(top, orders, limit, orderComparator);
        }
        return top;
    }

    public Collection<Order> getOrders(Vendor vendor, double balance) {
        Collection<Place> places = market.get();
        Graph<Place> graph = new Graph<Place>(vendor.getPlace(), places, tank, maxDistance, true, jumps, Path::new);
        return getOrders(graph, Collections.singleton(vendor), balance, 0);
    }

    public Collection<Order> getOrders(Place place, double balance) {
        return getOrders(place, balance, 0);
    }

    private Collection<Order> getOrders(Place place, double balance, double lowProfit) {
        Collection<Place> places = market.get();
        Graph<Place> graph = new Graph<>(place, places, tank, maxDistance, true, jumps, Path::new);
        return getOrders(graph, place.get(), balance, lowProfit);
    }

    private Collection<Order> getOrders(Graph<Place> graph, Collection<Vendor> sellers, double balance, double lowProfit) {
        List<Order> res = new ArrayList<>(20);
        for (Vendor vendor : sellers) {
            for (Offer sell : vendor.getAllSellOffers()) {
                LOG.trace("Sell offer {}", sell);
                if (sell.getCount() == 0) continue;
                long count = Order.getMaxCount(sell, balance, cargo);
                LOG.trace("count = {}", count);
                if (count == 0) continue;
                Iterator<Offer> buyers = market.getStatBuy(sell.getItem()).getOffers().descendingIterator();
                while (buyers.hasNext()){
                    Offer buy = buyers.next();
                    if (!graph.isAccessible(buy.getVendor().getPlace())){
                        LOG.trace("Is inaccessible buyer, skip");
                        continue;
                    }
                    Order order = new Order(sell, buy, count);
                    LOG.trace("Buy offer {} profit = {}", buy, order.getProfit());
                    if (order.getProfit() <= 0 && order.getCount() > 0) break;
                    if (order.getProfit() < lowProfit && order.getCount() == count) {
                        LOG.trace("Is low profit, skip");
                        break;
                    }
                    res.add(order);
                }
            }
        }
        res.sort(orderComparator);
        return res;
    }

    private Collection<Order> getOrders(Collection<Vendor> sellers, Collection<Vendor> buyers, double balance, double lowProfit) {
        List<Order> res = new ArrayList<>();
        for (Vendor seller : sellers) {
            for (Offer sell : seller.getAllSellOffers()) {
                if (sell.getCount() == 0) continue;
                long count = Order.getMaxCount(sell, balance, cargo);
                LOG.trace("Sell offer {}, count = {}", sell, count);
                if (count == 0) continue;
                for (Vendor buyer : buyers) {
                    Offer buy = buyer.getBuy(sell.getItem());
                    if (buy != null){
                        Order order = new Order(sell, buy, count);
                        LOG.trace("Buy offer {} profit = {}", buy, order.getProfit());
                        if (order.getProfit() < lowProfit) {
                            LOG.trace("Is low profit, skip");
                            continue;
                        }
                        res.add(order);
                    }
                }
            }
        }
        res.sort(orderComparator);
        return res;
    }

    public Collection<Order> getOrders(Vendor from, Vendor to, double balance) {
        Graph<Place> graph = new Graph<Place>(from.getPlace(), market.get(), tank, maxDistance, true, jumps, Path::new);
        if (!graph.isAccessible(to.getPlace())){
            LOG.trace("Is inaccessible buyer");
            return Collections.emptyList();
        }
        return getOrders(Collections.singleton(from), Collections.singleton(to), balance, 0);
    }

    public Collection<Order> getOrders(Place from, Place to, double balance) {
        Graph<Place> graph = new Graph<Place>(from, market.get(), tank, maxDistance, true, jumps, Path::new);
        if (!graph.isAccessible(to)){
            LOG.trace("Is inaccessible buyer");
            return Collections.emptyList();
        }
        return getOrders(from.get(), to.get(), balance, 0);
    }


    public Collection<Order> getOrders(Vendor from, Place to, double balance) {
        Graph<Place> graph = new Graph<Place>(from.getPlace(), market.get(), tank, maxDistance, true, jumps, Path::new);
        if (!graph.isAccessible(to)){
            LOG.trace("Is inaccessible buyer");
            return Collections.emptyList();
        }
        return getOrders(Collections.singleton(from), to.get(), balance, 0);
    }

    public Collection<Order> getOrders(Place from, Vendor to, double balance) {
        Graph<Place> graph = new Graph<Place>(from, market.get(), tank, maxDistance, true, jumps, Path::new);
        if (!graph.isAccessible(to.getPlace())){
            LOG.trace("Is inaccessible buyer");
            return Collections.emptyList();
        }
        return getOrders(from.get(), Collections.singleton(to), balance, 0);
    }


    public Collection<Path<Place>> getPaths(Place from, Place to){
        Graph<Place> graph = new Graph<Place>(from, market.get(), tank, maxDistance, true, jumps, Path::new);
        return graph.getPathsTo(to);
    }

    public Path<Place> getPath(Place from, Place to){
        Graph<Place> graph = new Graph<Place>(from, market.get(), tank, maxDistance, true, jumps, Path::new);
        return graph.getFastPathTo(to);
    }

    public PathRoute getPath(Vendor from, Vendor to){
        RouteGraph graph = new RouteGraph(from, market.getVendors(), tank, maxDistance, true, jumps);
        return (PathRoute)graph.getFastPathTo(to);
    }

    public Collection<PathRoute> getPaths(Vendor from, double balance){
        RouteSearcher searcher = new RouteSearcher(maxDistance, tank, segmentSize);
        Collection<Vendor> vendors = market.getVendors();
        return searcher.getPaths(from, vendors, jumps, balance, cargo, limit);
    }

    public Collection<PathRoute> getPaths(Place from, double balance){
        RouteSearcher searcher = new RouteSearcher(maxDistance, tank, segmentSize);
        Collection<Vendor> vendors = market.getVendors();
        for (Vendor vendor : from.get()) {
            Collection<PathRoute> paths = searcher.getPaths(vendor, vendors, jumps, balance, cargo, limit);
            if (paths.size()>0){
                return paths;
            }
        }
        return Collections.emptyList();
    }

    public Collection<PathRoute> getPaths(Vendor from, Vendor to, double balance){
        RouteSearcher searcher = new RouteSearcher(maxDistance, tank, segmentSize);
        return searcher.getPaths(from, to, market.getVendors(), jumps, balance, cargo, limit);
    }

    public Collection<PathRoute> getPaths(Place from, Place to, double balance){
        RouteSearcher searcher = new RouteSearcher(maxDistance, tank, segmentSize);
        Collection<Vendor> vendors = market.getVendors();
        Collection<Vendor> fVendors = from.get();
        Collection<Vendor> toVendors = to.get();
        for (Vendor fromVendor : fVendors) {
            for (Vendor toVendor : toVendors) {
                Collection<PathRoute> paths = searcher.getPaths(fromVendor, toVendor, vendors, jumps, balance, cargo, limit);
                if (paths.size()>0){
                    return paths;
                }
            }
        }
        return Collections.emptyList();
    }

    public Collection<PathRoute> getPaths(Vendor from, Place to, double balance){
        List<PathRoute> top = new ArrayList<>(limit);
        RouteSearcher searcher = new RouteSearcher(maxDistance, tank, segmentSize);
        Collection<Vendor> vendors = market.getVendors();
        Collection<Vendor> toVendors = to.get();
        for (Vendor toVendor : toVendors) {
            Collection<PathRoute> paths = searcher.getPaths(from, toVendor, vendors, jumps, balance, cargo, limit);
            TopList.addAllToTop(top, paths, limit, RouteGraph.byProfitComparator);
        }
        return top;
    }

    public Collection<PathRoute> getPaths(Place from, Vendor to, double balance){
        RouteSearcher searcher = new RouteSearcher(maxDistance, tank, segmentSize);
        Collection<Vendor> vendors = market.getVendors();
        Collection<Vendor> fVendors = from.get();
        for (Vendor fromVendor : fVendors) {
            Collection<PathRoute> paths = searcher.getPaths(fromVendor, to, vendors, jumps, balance, cargo, limit);
            if (paths.size()>0){
                return paths;
            }
        }
        return Collections.emptyList();
    }

    public Collection<PathRoute> getTopPaths(double balance){
        List<PathRoute> top = new ArrayList<>(limit);
        RouteSearcher searcher = new RouteSearcher(maxDistance, tank, segmentSize);
        Collection<Vendor> vendors = new PlacesWrapper(market.get());
        for (Vendor vendor : vendors) {
            Collection<PathRoute> paths = searcher.getPaths(vendor, vendor, vendors, jumps, balance, cargo, 3);
            TopList.addAllToTop(top, paths, limit, RouteGraph.byProfitComparator);
        }
        return top;
    }


    public void setTank(double tank) {
        this.tank = tank;
    }

    public void setMaxDistance(double maxDistance) {
        this.maxDistance = maxDistance;
    }

    public void setJumps(int jumps) {
        this.jumps = jumps;
    }

    public void setCargo(int cargo) {
        this.cargo = cargo;
    }

    public void setSegmentSize(int segmentSize) {
        this.segmentSize = segmentSize;
    }

    public void setPathsCount(int count) {
        this.limit = count;
    }
}
