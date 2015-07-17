package ru.trader.model;

import ru.trader.analysis.Route;
import ru.trader.analysis.RouteEntry;
import ru.trader.core.Order;

import java.util.ArrayList;
import java.util.Collection;

public class RouteModel {
    private final MarketModel market;
    private final Route _route;

    RouteModel(Route route, MarketModel market) {
        this.market = market;
        this._route = route;
    }

    public double getDistance() {
        return _route.getDistance();
    }

    public double getTotalProfit() {
        return _route.getProfit();
    }

    public int getJumps() {
        return _route.getLands();
    }

    public int getRefuels() {
        return _route.getRefills();
    }

    public Route getRoute() {
        return _route;
    }

    public int getLands() {
        return _route.getLands();
    }

    public double getAvgProfit(){
        return _route.getProfit()/_route.getLands();
    }

    public Collection<OrderModel> getOrders(){
        Collection<OrderModel> res = new ArrayList<>();
        for (RouteEntry entry : _route.getEntries()) {
            for (Order o : entry.getOrders()) {
                OrderModel order = market.getModeler().get(o);
                res.add(order);
            }
        }
        return res;
    }

    public void add(OrderModel order){
        Route path = market._getPath(order);
        if (path == null) return;
        _route.join(path);
    }

    public void add(RouteModel route){
        _route.join(route.getRoute());
    }

    public void remove(OrderModel order) {
        _route.dropTo(order.getStation().getStation());
    }

}
