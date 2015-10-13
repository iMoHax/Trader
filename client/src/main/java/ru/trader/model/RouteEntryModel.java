package ru.trader.model;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import ru.trader.analysis.RouteEntry;
import ru.trader.core.Order;
import ru.trader.model.support.BindingsHelper;

import java.util.Collection;
import java.util.List;

public class RouteEntryModel {
    private final StationModel station;
    private final RouteEntry entry;
    private final DoubleProperty profit;
    private final ObservableList<OrderModel> orders;
    private final ObservableList<OrderModel> sellOrders;
    private final ObservableList<MissionModel> missions;

    RouteEntryModel(RouteEntry entry, MarketModel market) {
        this.entry = entry;
        station = market.getModeler().get(entry.getVendor());
        List<Order> orderList = entry.getOrders();
        orders = BindingsHelper.observableList(orderList, market.getModeler()::get);
        sellOrders = FXCollections.observableArrayList();
        missions = FXCollections.observableArrayList();
        profit = new SimpleDoubleProperty();
        profit.bind(BindingsHelper.group(Double::sum, OrderModel::profitProperty, orders));
    }

    void addSellOrder(OrderModel order){
        sellOrders.add(order);
    }

    void add(MissionModel mission){
        missions.add(mission);
    }

    void addAll(Collection<MissionModel> missions){
        this.missions.addAll(missions);
    }

    void remove(MissionModel mission){
        missions.remove(mission);
    }

    public StationModel getStation() {
        return station;
    }

    public double getProfit() {
        return profit.get();
    }

    public ReadOnlyDoubleProperty profitProperty() {
        return profit;
    }

    public double getFuel(){
        return entry.getFuel();
    }

    public long getTime(){
        return entry.getTime();
    }

    public long getFullTime(){
        return entry.getFullTime();
    }

    public double getRefill(){
        return entry.getRefill();
    }

    public boolean isTransit(){
        return entry.isTransit();
    }

    public boolean isBuy(){
        return !orders.isEmpty();
    }

    public boolean isSell(){
        return !sellOrders.isEmpty();
    }

    public boolean isMissionTarget(){
        return !missions.isEmpty();
    }

    public ObservableList<OrderModel> orders() {
        return orders;
    }

    public ObservableList<OrderModel> sellOrders() {
        return sellOrders;
    }

    public ObservableList<MissionModel> missions() {
        return missions;
    }

    void refresh(MarketModel market){
        orders.clear();
        orders.addAll(BindingsHelper.observableList(entry.getOrders(), market.getModeler()::get));
    }
}
