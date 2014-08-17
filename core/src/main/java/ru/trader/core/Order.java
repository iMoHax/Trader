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
        this.count = count;
        this.profit = (buy.getPrice() - sell.getPrice()) * count;
    }

    public Offer getSell() {
        return sell;
    }

    public Offer getBuy() {
        return buy;
    }

    public void setCount(long count){
        this.count = count;
        this.profit = (buy.getPrice() - sell.getPrice()) * count;
    }

    public double getProfit(){
        return profit;
    }

    public long getCount() {
        return count;
    }

    public double getDistance() {
        return sell.getVendor().getDistance(buy.getVendor());
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
        setCount((long) Math.min(balance/sell.getPrice(), limit));
    }
}
