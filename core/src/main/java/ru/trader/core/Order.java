package ru.trader.core;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class Order implements Comparable<Order> {
    private Offer sell;
    private Offer buy;
    private double profit;
    private long count;

    public Order(Offer sell, Offer buy) {
        this.sell = sell;
        this.buy = buy;
        this.profit = Double.NaN;
    }

    public Order(Offer sell, Offer buy, long count) {
        this.sell = sell;
        this.buy = buy;
        this.count = getMaxCount(sell, buy, count);
        this.profit = (buy.getPrice() - sell.getPrice()) * count;
    }

    public Offer getSell() {
        return sell;
    }

    public Offer getBuy() {
        return buy;
    }

    public void setCount(long count){
        this.count = getMaxCount(sell, buy, count);
        this.profit = (buy.getPrice() - sell.getPrice()) * count;
    }

    public double getProfit(){
        return profit;
    }

    public long getCount() {
        return count;
    }

    public double getDistance() {
        return sell.getVendor().getPlace().getDistance(buy.getVendor().getPlace()) + buy.getVendor().getDistance() * 3.17e-8;
    }

    public boolean isBuyer(Place buyer) {
        return buy.getVendor().getPlace().equals(buyer);
    }

    public boolean isBuyer(Vendor buyer) {
        return buy.getVendor().equals(buyer);
    }

    public Vendor getBuyer(){
        return buy.getVendor();
    }

    @Override
    public int compareTo(@NotNull Order order) {
        Objects.requireNonNull(order, "Not compare with null");
        if (this == order) return 0;
        int cmp = Double.compare(profit, order.profit);
        if (cmp!=0) return cmp;
        cmp = Double.compare(sell.getPrice(), order.sell.getPrice());
        if (cmp!=0) return cmp;
        cmp = Double.compare(order.buy.getPrice(), buy.getPrice());
        if (cmp!=0) return cmp;
        cmp = sell.compareTo(order.sell);
        if (cmp!=0) return cmp;
        return buy.compareTo(order.buy);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Order order = (Order) o;

        return Double.compare(order.profit, profit) == 0 && buy.equals(order.buy) && sell.equals(order.sell);

    }

    public boolean equalsIgnoreCount(Order order) {
        return buy.equals(order.buy) && sell.equals(order.sell);
    }

    @Override
    public int hashCode() {
        int result;
        result = sell.hashCode();
        result = 31 * result + buy.hashCode();
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Order{");
        sb.append("sell=").append(sell);
        sb.append(", buy=").append(buy);
        sb.append(", profit=").append(profit);
        sb.append(", count=").append(count);
        sb.append('}');
        return sb.toString();
    }

    public void setMax(double balance, long limit) {
        this.count = getMaxCount(sell, buy, balance, limit);
        this.profit = (buy.getPrice() - sell.getPrice()) * count;
    }

    public static long getMaxCount(Offer sell, double balance, long limit){
        return getMaxCount(sell, null, balance, limit);
    }

    public static long getMaxCount(Offer sell, Offer buy, long limit){
        return getMaxCount(sell, buy, Double.POSITIVE_INFINITY, limit);
    }

    public static long getMaxCount(Offer sell, Offer buy, double balance, long limit){
        long supply = sell.getCount();
        if (supply == 0) return 0;
        if (supply == -1) supply = Long.MAX_VALUE;
        long demand = buy != null ? buy.getCount() : -1;
        if (demand <= 0) demand = Long.MAX_VALUE;
        if (Double.isInfinite(balance)) return Math.min(limit, Math.min(supply, demand));
        return (long) Math.min(limit, Math.min(Math.min(supply, demand), Math.floor(balance/sell.getPrice())));
    }
}
