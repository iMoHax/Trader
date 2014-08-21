package ru.trader.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.graph.Graph;
import ru.trader.graph.Path;
import ru.trader.graph.PathRoute;
import ru.trader.graph.RouteGraph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.TreeSet;

public class MarketAnalyzer {
    private final static Logger LOG = LoggerFactory.getLogger(MarketAnalyzer.class);

    private Market market;
    private RouteGraph graph;
    private double tank;
    private double maxDistance;
    private int jumps;
    private int cargo;


    public MarketAnalyzer(Market market) {
        this.market = market;
    }

    public Collection<Order> getTop(int limit, double balance){
        LOG.debug("Get top {}", limit);
        TreeSet<Order> top = new TreeSet<>();
        for (Vendor vendor : market.get()) {
            LOG.trace("Check vendor {}", vendor);
            setSource(vendor);
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
                    if (top.size() == limit){
                        LOG.trace("Min order {}", top.first());
                        if (top.first().getProfit() < order.getProfit()) {
                            LOG.trace("Add to top");
                            top.add(order);
                            top.pollFirst();
                        } else {
                            LOG.trace("Is low profit, skip");
                            break;
                        }
                    } else {
                        top.add(order);
                    }
                }
            }
        }
        return top;
    }

    public Collection<Order> getOrders(Vendor vendor, double balance) {
        Collection<Order> res = new ArrayList<>();
        setSource(vendor);
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
                res.add(order);
            }
        }
        return res;
    }

    public Collection<Order> getOrders(Vendor from, Vendor to, double balance) {
        Collection<Order> res = new ArrayList<>();
        setSource(from);
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


    private void rebuild(Vendor source){
        graph = new RouteGraph(source, market.get(), tank, maxDistance, true, jumps);
        graph.setLimit(cargo);
    }

    private void setSource(Vendor source){
        if (graph == null || !graph.getRoot().equals(source))
            rebuild(source);
    }

    public Collection<Path<Vendor>> getPaths(Vendor from, Vendor to){
        setSource(from);
        return graph.getPathsTo(to);
    }

    public PathRoute getPath(Vendor from, Vendor to){
        setSource(from);
        return (PathRoute) graph.getFastPathTo(to);
    }

    public Collection<PathRoute> getPaths(Vendor from, double balance){
        setSource(from);
        graph.setBalance(balance);
        Collection<PathRoute> res = new ArrayList<>();
        for (Vendor vendor : market.get()) {
            Collection<Path<Vendor>> paths = graph.getPathsTo(vendor, 10);
            for (Path<Vendor> path : paths) {
                res.add((PathRoute) path);
            }
        }
        return res;
    }

    public Collection<PathRoute> getPaths(Vendor from, Vendor to, double balance){
        setSource(from);
        graph.setBalance(balance);
        Collection<Path<Vendor>> paths = graph.getPathsTo(to);
        Collection<PathRoute> res = new ArrayList<>(paths.size());
        for (Path<Vendor> path : paths) {
            res.add((PathRoute) path);
        }
        return res;
    }

    public Collection<PathRoute> getTopPaths(int limit, double balance){
        TreeSet<PathRoute> top = new TreeSet<>((p1, p2) -> Double.compare(p2.getProfit()/p2.getLandsCount(), p1.getProfit()/p1.getLandsCount()));
        for (Vendor vendor : market.get()) {
            setSource(vendor);
            graph.setBalance(balance);
            Collection<Path<Vendor>> paths = graph.getPathsTo(vendor, 10);
            for (Path<Vendor> path : paths) {
                top.add((PathRoute) path);
                if (top.size() > limit) {
                    top.pollLast();
                }
            }
        }
        return top;
    }


    public void setTank(double tank) {
        this.tank = tank;
        this.graph = null;
    }

    public void setMaxDistance(double maxDistance) {
        this.maxDistance = maxDistance;
        this.graph = null;
    }

    public void setJumps(int jumps) {
        this.jumps = jumps;
        this.graph = null;
    }

    public void setCargo(int cargo) {
        if (graph != null) graph.setLimit(cargo);
        this.cargo = cargo;
    }


}
