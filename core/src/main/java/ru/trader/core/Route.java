package ru.trader.core;

import ru.trader.graph.Path;

import java.util.LinkedList;

public class Route {
    private double profit;
    private double distance;
    private final LinkedList<Order> orders = new LinkedList<>();

    public Route() {
        profit = 0;
        distance = 0;
    }

    public void add(Order order){
        orders.add(order);
        profit += order.getProfit();
        distance += order.getDistance();
    }



}
