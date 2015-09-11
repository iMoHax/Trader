package ru.trader.model;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import ru.trader.analysis.Route;
import ru.trader.analysis.RouteEntry;
import ru.trader.analysis.RouteFiller;
import ru.trader.controllers.MainController;
import ru.trader.core.Offer;
import ru.trader.core.Order;
import ru.trader.model.support.BindingsHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class RouteModel {
    private final MarketModel market;
    private final Route _route;
    private final DoubleProperty profit;
    private final DoubleProperty profitByTime;
    private final List<RouteEntryModel> entries;

    RouteModel(Route route, MarketModel market) {
        this.market = market;
        this._route = route;
        entries = _route.getEntries().stream().map(e -> new RouteEntryModel(e, market)).collect(Collectors.toList());
        profit = new SimpleDoubleProperty();
        profit.bind(BindingsHelper.group(Double::sum, RouteEntryModel::profitProperty, entries));
        profitByTime = new SimpleDoubleProperty();
        profitByTime.bind(profit.divide(_route.getTime()));
        fillSellOrders();
    }

    private void fillSellOrders(){
        for (int i = 0; i < entries.size(); i++) {
            RouteEntryModel entry = entries.get(i);
            for (OrderModel order : entry.orders()) {
                for (int j = i+1; j < entries.size(); j++) {
                    RouteEntryModel buyEntry = entries.get(j);
                    if (buyEntry.getStation().equals(order.getBuyer())){
                        buyEntry.addSellOrder(order);
                        break;
                    }
                }
            }
        }
    }

    public RouteEntryModel get(int index){
        return entries.get(index);
    }

    public double getDistance() {
        return _route.getDistance();
    }

    public int getJumps() {
        return entries.size();
    }

    public int getRefuels() {
        return _route.getRefills();
    }

    public long getTime(){
        return _route.getTime();
    }

    public Route getRoute() {
        return _route;
    }

    public int getLands() {
        return _route.getLands();
    }

    public double getProfit() {
        return profit.get();
    }

    public ReadOnlyDoubleProperty profitProperty() {
        return profit;
    }

    public double getProfitByTime(){
        return profitByTime.get();
    }

    public ReadOnlyDoubleProperty profitByTimeProperty(){
        return profitByTime;
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

    public RouteModel add(OrderModel order){
        Route path = market._getPath(order);
        if (path == null) return this;
        _route.join(path);
        return new RouteModel(_route, market);
    }

    public RouteModel add(RouteModel route){
        _route.join(route.getRoute());
        return new RouteModel(_route, market);
    }

    public RouteModel remove(OrderModel order) {
        _route.dropTo(order.getStation().getStation());
        return new RouteModel(_route, market);
    }

    public void add(MissionModel mission){
        long cargo = MainController.getProfile().getShipCargo();
        Offer offer = mission.toOffer();
        if (offer != null){
            RouteFiller.addOrders(_route, 0, offer, cargo);
            for (RouteEntryModel entry : entries) {
                entry.sellOrders().clear();
                entry.refresh(market);
            }
            fillSellOrders();
        }
    }

    public void addAll(Collection<MissionModel> missions){
        long cargo = MainController.getProfile().getShipCargo();
        for (MissionModel mission : missions) {
            Offer offer = mission.toOffer();
            if (offer != null){
                RouteFiller.addOrders(_route, 0, offer, cargo);
            }
        }
        for (RouteEntryModel entry : entries) {
            entry.sellOrders().clear();
            entry.refresh(market);
        }
        fillSellOrders();
    }

}
