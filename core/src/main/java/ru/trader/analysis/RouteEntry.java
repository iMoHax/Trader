package ru.trader.analysis;

import ru.trader.core.Order;
import ru.trader.core.Vendor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RouteEntry {
    private final Vendor vendor;
    private final double fuel;
    private final double score;
    private final List<Order> orders;
    private boolean land;
    private boolean refill;


    public RouteEntry(Vendor vendor, boolean refill, double fuel, double score) {
        orders = new ArrayList<>();
        this.vendor = vendor;
        this.refill = refill;
        this.fuel = fuel;
        this.score = score;
    }

    public Vendor getVendor() {
        return vendor;
    }

    public boolean is(Vendor vendor){
        return vendor.equals(this.vendor);
    }

    public boolean isRefill() {
        return refill;
    }

    void setRefill(boolean refill) {
        this.refill = refill;
    }

    public double getFuel() {
        return fuel;
    }

    public double getScore() {
        return score;
    }

    public void add(Order order){
        orders.add(order);
    }

    public void addAll(Collection<Order> orders){
        this.orders.addAll(orders);
    }

    public List<Order> getOrders() {
        return orders;
    }

    public void clearOrders(){
        orders.clear();
    }

    public double getProfit(){
        return orders.stream().mapToDouble(Order::getProfit).sum();
    }

    public boolean isLand(){
        return land || refill || !orders.isEmpty();
    }

    public void setLand(boolean land) {
        this.land = land;
    }

    public boolean isTransit(){
        return !isLand();
    }

    @Override
    public String toString() {
        return vendor + (isRefill() ? " (R)":"");
    }
}
