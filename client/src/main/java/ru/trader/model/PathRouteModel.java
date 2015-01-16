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

    private final PathRoute _path;

    PathRouteModel(PathRoute path, MarketModel market) {
        this.market = market;
        this._path = path;
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
        return _path;
    }

    public int getLands() {
        return lands;
    }

    public double getAvgProfit(){
        return totalProfit/lands;
    }

    public Collection<OrderModel> getOrders(){
        Collection<OrderModel> res = new ArrayList<>(lands);
        PathRoute path = _path.getRoot();
        Order cargo = null;
        while (path.hasNext()){
            path = path.getNext();
            if (cargo == null && path.getBest()!=null){
                cargo = path.getBest();
                OrderModel order = market.getModeler().get(cargo);
                order.setPath(path);
                res.add(order);
            }
            if (cargo!=null && cargo.isBuyer(path.get())){
                cargo = null;
            }
        }
        return res;
    }

    PathRoute _add(PathRoute route){
        return _path.add(route, true);
    }

    public PathRouteModel add(OrderModel order){
        PathRoute path = market._getPath(order.getStation(), order.getBuyer());
        if (path == null) return this;
        path.getRoot().getNext().setOrder(new Order(order.getOffer().getOffer(), order.getBuyOffer().getOffer(), order.getCount()));
        PathRoute head = _path.getEnd();
        PathRouteModel res = new PathRouteModel(_add(path), market);
        order.setPath(head);
        return res;
    }

    public PathRouteModel add(PathRouteModel route){
        return new PathRouteModel(_add(route.getPath()), market);
    }

    public PathRouteModel remove(OrderModel order) {
        return new PathRouteModel(_path.dropTo(order.getStation().getStation()), market);
    }

    public void recompute(double balance, long cargo) {
        _path.refresh();
        _path.sort(balance, cargo);
    }

}
