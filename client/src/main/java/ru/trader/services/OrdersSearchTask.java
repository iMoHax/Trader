package ru.trader.services;

import ru.trader.core.Order;
import ru.trader.core.Place;
import ru.trader.core.Vendor;
import ru.trader.model.MarketModel;

import java.util.Collection;

public class OrdersSearchTask extends AnalyzerTask<Collection<Order>>{
    private final Place from;
    private final Vendor stationFrom;
    private final Place to;
    private final Vendor stationTo;
    private final double balance;

    public OrdersSearchTask(MarketModel market, Place from, Vendor stationFrom, Place to, Vendor stationTo, double balance) {
        super(market);
        this.from = from;
        this.stationFrom = stationFrom;
        this.to = to;
        this.stationTo = stationTo;
        this.balance = balance;
    }

    @Override
    protected Collection<Order> call() throws Exception {
        Collection<Order> orders;
        if (stationFrom != null){
            if (stationTo != null){
                orders = analyzer.getOrders(stationFrom, stationTo, balance);
            } else {
                if (to != null){
                    orders = analyzer.getOrders(stationFrom, to, balance);
                } else {
                    orders = analyzer.getOrders(stationFrom, balance);
                }
            }
        } else {
            if (stationTo != null){
                orders = analyzer.getOrders(from, stationTo, balance);
            } else {
                if (to != null){
                    orders = analyzer.getOrders(from, to, balance);
                } else {
                    if (from != null){
                        orders = analyzer.getOrders(from, balance);
                    } else {
                        orders = analyzer.getTop(balance);
                    }
                }
            }
        }
        return orders;
    }
}
