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

    public Collection<Order> getOrders(Place place, double balance) {
        return getOrders(place, balance, 0);
    }

    private Collection<Order> getOrders(Place place, double balance, double lowProfit) {
        List<Order> res = new ArrayList<>(20);
        Collection<Place> places = market.get();
        RouteGraph graph = new RouteGraph(place, places, tank, maxDistance, true, jumps);
        for (Vendor vendor : place.get()) {
            for (Offer sell : vendor.getAllSellOffers()) {
                LOG.trace("Sell offer {}", sell);
                if (sell.getCount() == 0) continue;
                long count = Math.min(sell.getCount(), Math.min(cargo, (long) Math.floor(balance / sell.getPrice())));
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

    public Collection<Order> getOrders(Place from, Place to, double balance) {
        Collection<Order> res = new ArrayList<>();
        RouteGraph graph = new RouteGraph(from, market.get(), tank, maxDistance, true, jumps);
        if (!graph.isAccessible(to)){
            LOG.trace("Is inaccessible buyer");
            return res;
        }
        for (Vendor seller : from.get()) {
            for (Offer sell : seller.getAllSellOffers()) {
                if (sell.getCount() == 0) continue;
                long count = Math.min(sell.getCount(), Math.min(cargo, (long) Math.floor(balance / sell.getPrice())));
                LOG.trace("Sell offer {}, count = {}", sell, count);
                if (count == 0) continue;
                for (Vendor buyer : to.get()) {
                    Offer buy = buyer.getBuy(sell.getItem());
                    if (buy != null){
                        Order order = new Order(sell, buy, count);
                        LOG.trace("Buy offer {} profit = {}", buy, order.getProfit());
                        res.add(order);
                    }
                }
            }
        }
        return res;
    }

    public Collection<Order> getOrders(Vendor from, Vendor to, double balance) {
        Collection<Order> res = new ArrayList<>();
        RouteGraph graph = new RouteGraph(from.getPlace(), market.get(), tank, maxDistance, true, jumps);
        if (!graph.isAccessible(to.getPlace())){
            LOG.trace("Is inaccessible buyer");
            return res;
        }
        for (Offer sell : from.getAllSellOffers()) {
            if (sell.getCount() == 0) continue;
            long count = Math.min(sell.getCount(), Math.min(cargo, (long) Math.floor(balance / sell.getPrice())));
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

    public Collection<Path<Place>> getPaths(Place from, Place to){
        RouteGraph graph = new RouteGraph(from, market.get(), tank, maxDistance, true, jumps);
        return graph.getPathsTo(to);
    }

    public PathRoute getPath(Place from, Place to){
        RouteGraph graph = new RouteGraph(from, market.get(), tank, maxDistance, true, jumps);
        return (PathRoute) graph.getFastPathTo(to);
    }

    public Collection<PathRoute> getPaths(Place from, double balance){
        RouteSearcher searcher = new RouteSearcher(maxDistance, tank, segmentSize);
        return searcher.getPaths(from, market.get(), jumps, balance, cargo, limit);
    }

    public Collection<PathRoute> getPaths(Place from, Place to, double balance){
        RouteSearcher searcher = new RouteSearcher(maxDistance, tank, segmentSize);
        return searcher.getPaths(from, to, market.get(), jumps, balance, cargo, limit);
    }

    public Collection<PathRoute> getTopPaths(double balance){
        List<PathRoute> top = new ArrayList<>(limit);
        RouteSearcher searcher = new RouteSearcher(maxDistance, tank, segmentSize);
        Collection<Place> places = market.get();
        for (Place place : places) {
            Collection<PathRoute> paths = searcher.getPaths(place, place, places, jumps, balance, cargo, 3);
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
