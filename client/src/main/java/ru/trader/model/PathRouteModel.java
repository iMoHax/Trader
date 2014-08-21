package ru.trader.model;

import ru.trader.core.Order;
import ru.trader.graph.PathRoute;

import java.util.ArrayList;
import java.util.Collection;

public class PathRouteModel {
    private final MarketModel market;
    private final double distance;
    private final double totalProfit;
    private final int jumps;
    private final int refuels;
    private final int lands;

    private final PathRoute path;

    PathRouteModel(PathRoute path, MarketModel market) {
        this.market = market;
        this.path = path;
        PathRoute p = path.getRoot();
        totalProfit = p.getProfit();
        lands = p.getLandsCount();
        double d = 0; int j = 0, r = 0;
        while (p.hasNext()){
            p = p.getNext();
            d += p.getDistance();
            j++;
            if (p.isRefill()) r++;
        }
        distance = d;
        jumps = j;
        refuels = r;
    }

    public double getDistance() {
        return distance;
    }

    public double getTotalProfit() {
        return totalProfit;
    }

    public int getJumps() {
        return jumps;
    }

    public int getRefuels() {
        return refuels;
    }

    public PathRoute getPath() {
        return path;
    }

    public int getLands() {
        return lands;
    }

    public double getAvgProfit(){
        return totalProfit/lands;
    }

    public Collection<OrderModel> getOrders(){
        Collection<OrderModel> res = new ArrayList<>(lands);
        PathRoute p = path.getRoot();
        Order cargo = null;
        while (p.hasNext()){
            p = p.getNext();
            if (cargo == null && p.getBest()!=null){
                cargo = p.getBest();
                OrderModel order = market.asModel(cargo);
                order.setPath(p);
                res.add(order);
            }
            if (cargo!=null && cargo.isBuyer(p.get())){
                cargo = null;
            }
        }
        return res;
    }

    public void add(OrderModel order){
        PathRoute p = market.getPath(order.getVendor(), order.getBuyer());
        if (p == null) return;
        p.getRoot().getNext().setOrder(new Order(order.getOffer().getOffer(), order.getBuyOffer().getOffer(), order.getCount()));
        PathRoute head = path.getEnd();
        add(p);
        order.setPath(head);
    }

    public void add(PathRoute route){
        path.getEnd().add(route, true);
    }

    public PathRouteModel remove(OrderModel order) {
        return new PathRouteModel(path.dropTo(order.getVendor().getVendor()), market);
    }
}
