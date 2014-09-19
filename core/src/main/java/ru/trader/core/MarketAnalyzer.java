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
        Collection<Vendor> vendors = market.get();
        List<Order> top = new ArrayList<>(limit);
        for (Vendor vendor : vendors) {
            LOG.trace("Check vendor {}", vendor);
            Collection<Order> orders = getOrders(vendor, balance, top.isEmpty() ? 0 : top.get(top.size()-1).getProfit());
            TopList.addAllToTop(top, orders, limit, orderComparator);
        }
        return top;
    }

    public Collection<Order> getOrders(Vendor vendor, double balance) {
        return getOrders(vendor, balance, 0);
    }

    private Collection<Order> getOrders(Vendor vendor, double balance, double lowProfit) {
        List<Order> res = new ArrayList<>(20);
        Collection<Vendor> vendors = market.get();
        RouteGraph graph = new RouteGraph(vendor, vendors, tank, maxDistance, true, jumps);
        for (Offer sell : vendor.getAllSellOffers()) {
            long count = Math.min(cargo, (long) Math.floor(balance / sell.getPrice()));
            LOG.trace("Sell offer {}, count = {}", sell, count);
            if (count == 0) continue;
            Iterator<Offer> buyers = market.getStatBuy(sell.getItem()).getOffers().descendingIterator();
            while (buyers.hasNext()){
                Offer buy = buyers.next();
                if (!graph.isAccessible(buy.getVendor())){
                    LOG.trace("Is inaccessible buyer, skip");
                    continue;
                }
                Order order = new Order(sell, buy, count);
                LOG.trace("Buy offer {} profit = {}", buy, order.getProfit());
                if (order.getProfit() <= 0 ) break;
                if (order.getProfit() < lowProfit) {
                    LOG.trace("Is low profit, skip");
                    break;
                }
                res.add(order);
            }
        }
        res.sort(orderComparator);
        return res;
    }

    public Collection<Order> getOrders(Vendor from, Vendor to, double balance) {
        Collection<Order> res = new ArrayList<>();
        RouteGraph graph = new RouteGraph(from, market.get(), tank, maxDistance, true, jumps);
        if (!graph.isAccessible(to)){
            LOG.trace("Is inaccessible buyer");
            return res;
        }
        for (Offer sell : from.getAllSellOffers()) {
            long count = Math.min(cargo, (long) Math.floor(balance / sell.getPrice()));
            LOG.trace("Sell offer {}, count = {}", sell, count);
            if (count == 0) continue;

            Offer buy = to.getBuy(sell.getItem());
            if (buy != null){
                Order order = new Order(sell, buy, count);
                LOG.trace("Buy offer {} profit = {}", buy, order.getProfit());
                res.add(order);
            }
        }
        return res;
    }

    public Collection<Path<Vendor>> getPaths(Vendor from, Vendor to){
        RouteGraph graph = new RouteGraph(from, market.get(), tank, maxDistance, true, jumps);
        return graph.getPathsTo(to);
    }

    public PathRoute getPath(Vendor from, Vendor to){
        RouteGraph graph = new RouteGraph(from, market.get(), tank, maxDistance, true, jumps);
        return (PathRoute) graph.getFastPathTo(to);
    }

    public Collection<PathRoute> getPaths(Vendor from, double balance){
        RouteSearcher searcher = new RouteSearcher(maxDistance, tank, segmentSize);
        return searcher.getPaths(from, market.get(), jumps, balance, cargo, limit);
    }

    public Collection<PathRoute> getPaths(Vendor from, Vendor to, double balance){
        RouteSearcher searcher = new RouteSearcher(maxDistance, tank, segmentSize);
        return searcher.getPaths(from, to, market.get(), jumps, balance, cargo, limit);
    }

    public Collection<PathRoute> getTopPaths(double balance){
        List<PathRoute> top = new ArrayList<>(limit);
        RouteSearcher searcher = new RouteSearcher(maxDistance, tank, segmentSize);
        Collection<Vendor> vendors = market.get();
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
