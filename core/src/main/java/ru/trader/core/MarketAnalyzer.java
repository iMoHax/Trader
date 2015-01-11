package ru.trader.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.graph.*;

import java.util.*;

public class MarketAnalyzer {
    private final static Logger LOG = LoggerFactory.getLogger(MarketAnalyzer.class);

    private final Market market;
    private MarketAnalyzerCallBack callback;
    private MarketFilter filter;
    private double tank;
    private double maxDistance;
    private int segmentSize;
    private int limit;
    private int jumps;
    private int cargo;

    private final static Comparator<Order> orderComparator = (o1, o2) -> o2.compareTo(o1);

    public MarketAnalyzer(Market market) {
        this(market, new MarketAnalyzerCallBack());
    }

    public MarketAnalyzer(Market market, MarketAnalyzerCallBack callback) {
        this.market = market;
        this.callback = callback;
        this.limit = 100;
        this.segmentSize = 0;
    }

    public void setCallback(MarketAnalyzerCallBack callback) {
        this.callback = callback;
    }

    public void setFilter(MarketFilter filter) {
        this.filter = filter;
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

    public Collection<Order> getTop(double balance){
        LOG.debug("Get top {}", limit);
        Collection<Place> places = getPlaces();
        List<Order> top = new ArrayList<>(limit);
        callback.setCount(places.size());
        for (Place place : places) {
            if (callback.isCancel()) break;
            LOG.trace("Check place {}", place);
            Collection<Order> orders = getOrders(place, balance, top.isEmpty() ? 0 : top.get(top.size()-1).getProfit());
            TopList.addAllToTop(top, orders, limit, orderComparator);
            callback.inc();
        }
        callback.onEnd();
        return top;
    }

    public Collection<Order> getOrders(Vendor vendor, double balance) {
        Collection<Place> places = getPlaces();
        Graph<Place> graph = new Graph<Place>(vendor.getPlace(), places, tank, maxDistance, true, jumps, Path::new, callback.onStartGraph());
        return getOrders(graph, Collections.singleton(vendor), balance, 0);
    }

    public Collection<Order> getOrders(Place place, double balance) {
        return getOrders(place, balance, 0);
    }

    private Collection<Order> getOrders(Place place, double balance, double lowProfit) {
        Collection<Place> places = getPlaces();
        Graph<Place> graph = new Graph<>(place, places, tank, maxDistance, true, jumps, Path::new, callback.onStartGraph());
        return getOrders(graph, place.get(), balance, lowProfit);
    }

    private Collection<Order> getOrders(Graph<Place> graph, Collection<Vendor> sellers, double balance, double lowProfit) {
        List<Order> res = new ArrayList<>(20);
        callback.setCount(sellers.size());
        for (Vendor vendor : sellers) {
            if (callback.isCancel()) break;
            if (isFiltered(vendor)){
                LOG.trace("Is filtered, skip");
                callback.inc();
                continue;
            }
            for (Offer sell : vendor.getAllSellOffers()) {
                if (callback.isCancel()) break;
                LOG.trace("Sell offer {}", sell);
                if (sell.getCount() == 0) continue;
                long count = Order.getMaxCount(sell, balance, cargo);
                LOG.trace("count = {}", count);
                if (count == 0) continue;
                Iterator<Offer> buyers = market.getStatBuy(sell.getItem()).getOffers().descendingIterator();
                while (buyers.hasNext()){
                    if (callback.isCancel()) break;
                    Offer buy = buyers.next();
                    if (isFiltered(buy.getVendor())){
                        LOG.trace("Is filtered, skip");
                        continue;
                    }
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
            callback.inc();
        }
        res.sort(orderComparator);
        return res;
    }

    private Collection<Order> getOrders(Collection<Vendor> sellers, Collection<Vendor> buyers, double balance, double lowProfit) {
        List<Order> res = new ArrayList<>();
        callback.setCount(sellers.size());
        for (Vendor seller : sellers) {
            if (callback.isCancel()) break;
            if (isFiltered(seller)){
                LOG.trace("Is filtered, skip");
                callback.inc();
                continue;
            }
            for (Offer sell : seller.getAllSellOffers()) {
                if (callback.isCancel()) break;
                if (sell.getCount() == 0) continue;
                long count = Order.getMaxCount(sell, balance, cargo);
                LOG.trace("Sell offer {}, count = {}", sell, count);
                if (count == 0) continue;
                for (Vendor buyer : buyers) {
                    if (callback.isCancel()) break;
                    if (isFiltered(buyer)){
                        LOG.trace("Is filtered, skip");
                        continue;
                    }
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
            callback.inc();
        }
        res.sort(orderComparator);
        return res;
    }

    public Collection<Order> getOrders(Vendor from, Vendor to, double balance) {
        Graph<Place> graph = new Graph<Place>(from.getPlace(), getPlaces(), tank, maxDistance, true, jumps, Path::new);
        if (!graph.isAccessible(to.getPlace())){
            LOG.trace("Is inaccessible buyer");
            return Collections.emptyList();
        }
        return getOrders(Collections.singleton(from), Collections.singleton(to), balance, 0);
    }

    public Collection<Order> getOrders(Place from, Place to, double balance) {
        Graph<Place> graph = new Graph<Place>(from, getPlaces(), tank, maxDistance, true, jumps, Path::new);
        if (!graph.isAccessible(to)){
            LOG.trace("Is inaccessible buyer");
            return Collections.emptyList();
        }
        return getOrders(from.get(), to.get(), balance, 0);
    }


    public Collection<Order> getOrders(Vendor from, Place to, double balance) {
        Graph<Place> graph = new Graph<Place>(from.getPlace(), getPlaces(), tank, maxDistance, true, jumps, Path::new);
        if (!graph.isAccessible(to)){
            LOG.trace("Is inaccessible buyer");
            return Collections.emptyList();
        }
        return getOrders(Collections.singleton(from), to.get(), balance, 0);
    }

    public Collection<Order> getOrders(Place from, Vendor to, double balance) {
        Graph<Place> graph = new Graph<Place>(from, getPlaces(), tank, maxDistance, true, jumps, Path::new);
        if (!graph.isAccessible(to.getPlace())){
            LOG.trace("Is inaccessible buyer");
            return Collections.emptyList();
        }
        return getOrders(from.get(), Collections.singleton(to), balance, 0);
    }


    public Collection<Path<Place>> getPaths(Place from, Place to){
        Graph<Place> graph = new Graph<Place>(from, getPlaces(), tank, maxDistance, true, jumps, Path::new);
        return graph.getPathsTo(to);
    }

    public Path<Place> getPath(Place from, Place to){
        Graph<Place> graph = new Graph<Place>(from, getPlaces(), tank, maxDistance, true, jumps, Path::new);
        return graph.getFastPathTo(to);
    }

    public PathRoute getPath(Vendor from, Vendor to){
        RouteGraph graph = new RouteGraph(from, getVendors(), tank, maxDistance, true, jumps);
        return (PathRoute)graph.getFastPathTo(to);
    }

    public Collection<PathRoute> getPaths(Vendor from, double balance){
        callback.setCount(1);
        RouteSearcher searcher = new RouteSearcher(maxDistance, tank, segmentSize, callback.onStartSearch());
        Collection<Vendor> vendors = getVendors();
        Collection<PathRoute> res = searcher.getPaths(from, vendors, jumps, balance, cargo, limit);
        callback.inc();
        callback.onEndSearch();
        return res;
    }

    public Collection<PathRoute> getPaths(Place from, double balance){
        List<PathRoute> top = new ArrayList<>(limit);
        Collection<Vendor> vendors = getVendors();
        callback.setCount(vendors.size());
        RouteSearcher searcher = new RouteSearcher(maxDistance, tank, segmentSize, callback.onStartSearch());
        for (Vendor vendor : from.get()) {
            if (callback.isCancel()) break;
            if (isFiltered(vendor)){
                LOG.trace("Is filtered, skip");
                callback.inc();
                continue;
            }
            Collection<PathRoute> paths = searcher.getPaths(vendor, vendors, jumps, balance, cargo, limit);
            TopList.addAllToTop(top, paths, limit, RouteGraph.byProfitComparator);
            callback.inc();
        }
        callback.onEndSearch();
        return top;
    }

    public Collection<PathRoute> getPaths(Vendor from, Vendor to, double balance){
        callback.setCount(1);
        RouteSearcher searcher = new RouteSearcher(maxDistance, tank, segmentSize, callback.onStartSearch());
        Collection<PathRoute> res = searcher.getPaths(from, to, getVendors(), jumps, balance, cargo, limit);
        callback.inc();
        callback.onEndSearch();
        return res;
    }

    public Collection<PathRoute> getPaths(Place from, Place to, double balance){
        List<PathRoute> top = new ArrayList<>(limit);
        Collection<Vendor> vendors = getVendors();
        Collection<Vendor> fVendors = from.get();
        Collection<Vendor> toVendors = to.get();
        int count = (int) Math.ceil(limit / fVendors.size());
        callback.setCount(fVendors.size() * toVendors.size());
        RouteSearcher searcher = new RouteSearcher(maxDistance, tank, segmentSize, callback.onStartSearch());
        for (Vendor fromVendor : fVendors) {
            if (callback.isCancel()) break;
            if (isFiltered(fromVendor)){
                LOG.trace("Is filtered, skip");
                callback.inc();
                continue;
            }
            for (Vendor toVendor : toVendors) {
                if (callback.isCancel()) break;
                if (isFiltered(toVendor)){
                    LOG.trace("Is filtered, skip");
                    callback.inc();
                    continue;
                }
                Collection<PathRoute> paths = searcher.getPaths(fromVendor, toVendor, vendors, jumps, balance, cargo, count);
                TopList.addAllToTop(top, paths, limit, RouteGraph.byProfitComparator);
                callback.inc();
            }
        }
        callback.onEndSearch();
        return top;
    }

    public Collection<PathRoute> getPaths(Vendor from, Place to, double balance){
        List<PathRoute> top = new ArrayList<>(limit);
        Collection<Vendor> vendors = getVendors();
        Collection<Vendor> toVendors = to.get();
        int count = (int) Math.ceil(limit / toVendors.size());
        callback.setCount(toVendors.size());
        RouteSearcher searcher = new RouteSearcher(maxDistance, tank, segmentSize, callback.onStartSearch());
        for (Vendor toVendor : toVendors) {
            if (callback.isCancel()) break;
            if (isFiltered(toVendor)){
                LOG.trace("Is filtered, skip");
                callback.inc();
                continue;
            }
            Collection<PathRoute> paths = searcher.getPaths(from, toVendor, vendors, jumps, balance, cargo, count);
            TopList.addAllToTop(top, paths, limit, RouteGraph.byProfitComparator);
            callback.inc();
        }
        callback.onEndSearch();
        return top;
    }

    public Collection<PathRoute> getPaths(Place from, Vendor to, double balance){
        List<PathRoute> top = new ArrayList<>(limit);
        Collection<Vendor> vendors = getVendors();
        Collection<Vendor> fVendors = from.get();
        int count = (int) Math.ceil(limit / fVendors.size());
        callback.setCount(fVendors.size());
        RouteSearcher searcher = new RouteSearcher(maxDistance, tank, segmentSize, callback.onStartSearch());
        for (Vendor fromVendor : fVendors) {
            if (callback.isCancel()) break;
            if (isFiltered(fromVendor)){
                LOG.trace("Is filtered, skip");
                callback.inc();
                continue;
            }
            Collection<PathRoute> paths = searcher.getPaths(fromVendor, to, vendors, jumps, balance, cargo, count);
            TopList.addAllToTop(top, paths, limit, RouteGraph.byProfitComparator);
            callback.inc();
        }
        callback.onEndSearch();
        return top;
    }

    public Collection<PathRoute> getTopPaths(double balance){
        List<PathRoute> top = new ArrayList<>(limit);
        Collection<Vendor> vendors = getVendors();
        callback.setCount(vendors.size());
        RouteSearcher searcher = new RouteSearcher(maxDistance, tank, segmentSize, callback.onStartSearch());
        for (Vendor vendor : vendors) {
            if (callback.isCancel()) break;
            Collection<PathRoute> paths = searcher.getPaths(vendor, vendor, vendors, jumps, balance, cargo, 3);
            TopList.addAllToTop(top, paths, limit, RouteGraph.byProfitComparator);
            callback.inc();
        }
        callback.onEndSearch();
        return top;
    }

    private Collection<Place> getPlaces(){
        if (filter != null){
            return filter.filtered(market.get());
        } else {
            return market.get();
        }
    }

    private Collection<Vendor> getVendors(){
        if (filter != null){
            Collection<Vendor> vendors = new PlacesWrapper(getPlaces());
            return filter.filteredVendors(vendors);
        } else {
            return market.getVendors();
        }
    }

    public MarketFilter getFilter() {
        return filter;
    }

    private boolean isFiltered(Vendor vendor){
        return filter != null && (filter.isFiltered(vendor.getPlace()) || filter.isFiltered(vendor));
    }
}
