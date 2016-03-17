package ru.trader.analysis;

import ru.trader.core.Order;
import ru.trader.core.Vendor;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class RouteEntry {
    private final Vendor vendor;
    private final double fuel;
    private final List<OrderWrapper> orders;
    private boolean land;
    private double refill;
    private double profit;
    private long time;
    private long fulltime;
    private long reserved;

    public RouteEntry(Vendor vendor, double refill, double fuel, double profit) {
        orders = new ArrayList<>();
        this.vendor = vendor;
        this.refill = refill;
        this.fuel = fuel;
        this.profit = profit;
        reserved = 0;
    }

    protected RouteEntry(RouteEntry entry){
        this.vendor = entry.vendor;
        this.fuel = entry.fuel;
        this.land = entry.land;
        this.refill = entry.refill;
        this.profit = entry.profit;
        this.time = entry.time;
        this.fulltime = entry.fulltime;
        this.reserved = entry.reserved;
        this.orders = new ArrayList<>(entry.orders.size());
        entry.orders.forEach(o -> this.orders.add(new OrderWrapper(o)));
    }

    public Vendor getVendor() {
        return vendor;
    }

    public boolean is(Vendor vendor){
        return vendor.equals(this.vendor);
    }

    public boolean isRefill() {
        return refill > 0;
    }

    public double getRefill(){
        return refill;
    }

    void setRefill(double refill) {
        this.refill = refill;
    }

    public double getFuel() {
        return fuel;
    }

    public double getProfit() {
        return profit;
    }

    void setProfit(double profit) {
        this.profit = profit;
    }

    public long getTime() {
        return time;
    }

    void setTime(long time) {
        this.time = time;
    }

    public long getFullTime() {
        return fulltime;
    }

    void setFullTime(long fullTime) {
        this.fulltime = fullTime;
    }

    void add(Order order){
        orders.add(fixedWrap(order));
        profit += order.getProfit();
    }

    void remove(Order order){
        if (orders.removeIf(o -> o.fixed && o.equals(order))){
            profit -= order.getProfit();
        }
    }

    void clear(){
        double p = orders.stream().filter(o -> o.fixed).mapToDouble(Order::getProfit).sum();
        if (orders.removeIf(o -> o.fixed)){
            profit -= p;
        }
    }

    void addAll(Collection<Order> orders){
        orders.forEach(this::add);
    }

    void addOrder(Order order){
        orders.add(wrap(order));
    }

    void removeOrder(Order order){
        if (order instanceof OrderWrapper) {
            orders.remove(order);
        } else {
            orders.removeIf(order::equals);
        }
    }

    public List<Order> getOrders() {
        return new AbstractList<Order>() {
            @Override
            public Order get(int index) {
                return orders.get(index);
            }

            @Override
            public int size() {
                return orders.size();
            }
        };
    }

    public List<Order> getFixedOrders(){
        return orders.stream().filter(o -> o.fixed).collect(Collectors.toList());
    }

    void reserve(final long count, final long cargo){
        long empty = cargo - getCargo();
        long need = count - empty;
        if (need > 0){
            List<Order> fixedOrders = getFixedOrders();
            fixedOrders.sort((o1, o2) -> Double.compare(o1.getProfitByTonne(), o2.getProfitByTonne()));
            for (Order order : fixedOrders) {
                long newCount = order.getCount() - need;
                if (newCount < 0){
                    newCount = 0;
                }
                need -= order.getCount() - newCount;
                order.setCount(newCount);
                if (need <= 0) break;
            }
        }
        reserved += count;
    }

    void fill(long count){
        List<Order> fixedOrders = getFixedOrders();
        fixedOrders.sort((o1, o2) -> Double.compare(o2.getProfitByTonne(), o1.getProfitByTonne()));
        for (Order order : fixedOrders) {
            long newCount = Math.min(((OrderWrapper)order).max, order.getCount() + count);
            count -= order.getCount() - newCount;
            order.setCount(newCount);
            if (count <= 0) break;
        }
        reserved -= count;
        if (reserved < 0){
            reserved = 0;
        }
    }

    public long getCargo(){
        return orders.stream().filter(o -> o.fixed).mapToLong(Order::getCount).sum() + reserved;
    }

    void clearOrders(){
        orders.clear();
    }

    public double getProfitByOrders(){
        return orders.stream().mapToDouble(Order::getProfit).sum();
    }

    public boolean isLand(){
        return land || isRefill() || !orders.isEmpty();
    }

    void setLand(boolean land) {
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
        if (time != that.time) return false;
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
        result = 31 * result + (isRefill() ? 1 : 0);
        temp = Double.doubleToLongBits(profit);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return vendor + (isRefill() ? " (R)":"");
    }

    private OrderWrapper wrap(Order order){
        return new OrderWrapper(order, false);
    }

    private OrderWrapper fixedWrap(Order order){
        return new OrderWrapper(order, true);
    }

    private class OrderWrapper extends Order {
        private final boolean fixed;
        private final long max;

        private OrderWrapper(Order order, boolean fixed) {
            super(order.getSell(), order.getBuy(), order.getCount());
            this.fixed = fixed;
            this.max = order.getCount();
        }

        private OrderWrapper(OrderWrapper orderWrapper){
            super(orderWrapper);
            this.fixed = orderWrapper.fixed;
            this.max = orderWrapper.max;
        }

        public void reset(){
            setCount(max);
        }
    }

    public static RouteEntry clone(RouteEntry entry){
        return entry != null ? new RouteEntry(entry) : null;
    }
}
