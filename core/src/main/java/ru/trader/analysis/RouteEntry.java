package ru.trader.analysis;

import ru.trader.core.Order;
import ru.trader.core.Vendor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RouteEntry {
    private final Vendor vendor;
    private final double fuel;
    private final List<Order> orders;
    private boolean land;
    private boolean refill;
    private double score;

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

    void setScore(double score) {
        this.score = score;
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RouteEntry)) return false;

        RouteEntry that = (RouteEntry) o;
        if (land != that.land) return false;
        if (refill != that.refill) return false;
        if (Double.compare(that.fuel, fuel) != 0) return false;
        if (orders.size() != that.orders.size()) return false;
        return vendor.equals(that.vendor);
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = vendor.hashCode();
        temp = Double.doubleToLongBits(fuel);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (land ? 1 : 0);
        result = 31 * result + (refill ? 1 : 0);
        temp = Double.doubleToLongBits(score);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return vendor + (isRefill() ? " (R)":"");
    }
}
