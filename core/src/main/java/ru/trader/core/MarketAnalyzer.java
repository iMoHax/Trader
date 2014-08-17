package ru.trader.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.graph.Graph;
import ru.trader.graph.Path;
import ru.trader.graph.PathRoute;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.TreeSet;

public class MarketAnalyzer {
    private final static Logger LOG = LoggerFactory.getLogger(MarketAnalyzer.class);

    private Market market;
    private Graph<Vendor> graph;
    private double tank;
    private double maxDistance;
    private int jumps;
    private long cargo;


    public MarketAnalyzer(Market market) {
        this.market = market;
    }

    public Collection<Order> getTop(int limit, double balance){
        LOG.debug("Get top {}", limit);
        TreeSet<Order> top = new TreeSet<>();
        for (Vendor vendor : market.get()) {
            LOG.trace("Check vendor {}", vendor);
            for (Offer sell : vendor.getAllSellOffers()) {
                long count = Math.min(cargo, (long) Math.floor(balance / sell.getPrice()));
                LOG.trace("Sell offer {}, count = {}", sell, count);
                if (count == 0) continue;
                Iterator<Offer> buyers = market.getStatBuy(sell.getItem()).getOffers().descendingIterator();
                while (buyers.hasNext()){
                    Offer buy = buyers.next();
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

    private void rebuild(Vendor source){
        graph = new Graph<>(source, market.get(), tank, maxDistance, true, jumps, PathRoute::new);
    }

    private void setSource(Vendor source){
        if (graph == null || !graph.getRoot().equals(source))
            rebuild(source);
    }

    public Collection<Path<Vendor>> getPaths(Vendor from, Vendor to){
        setSource(from);
        return graph.getPathsTo(to, true);
    }

    public Collection<PathRoute> getPaths(Vendor from, Vendor to, double balance){
        Collection<Path<Vendor>> paths = getPaths(from, to);
        Collection<PathRoute> res = new ArrayList<>(paths.size());
        for (Path<Vendor> path : paths) {
            PathRoute p = (PathRoute) path;
            p.sort(balance, cargo);
            res.add(p);
        }
        return res;
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

    public void setCargo(long cargo) {
        this.cargo = cargo;
    }

    public long getCargo() {
        return cargo;
    }
}
