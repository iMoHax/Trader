package ru.trader.services;

import ru.trader.core.Order;
import ru.trader.core.Place;
import ru.trader.core.Profile;
import ru.trader.core.Vendor;
import ru.trader.model.MarketModel;

import java.util.Collection;

public class OrdersSearchTask extends AnalyzerTask<Collection<Order>>{
    private final Place from;
    private final Vendor stationFrom;
    private final Place to;
    private final Vendor stationTo;

    public OrdersSearchTask(MarketModel market, Place from, Vendor stationFrom, Place to, Vendor stationTo, Profile profile) {
        super(market, profile);
        this.from = from;
        this.stationFrom = stationFrom;
        this.to = to;
        this.stationTo = stationTo;
    }

    @Override
    protected Collection<Order> call() throws Exception {
        Collection<Order> orders;
        if (stationFrom != null){
            if (stationTo != null){
                orders = analyzer.getOrders(stationFrom, stationTo);
            } else {
                if (to != null){
                    orders = analyzer.getOrders(stationFrom, to);
                } else {
                    orders = analyzer.getOrders(stationFrom);
                }
            }
        } else {
            if (stationTo != null){
                orders = analyzer.getOrders(from, stationTo);
            } else {
                if (to != null){
                    orders = analyzer.getOrders(from, to);
                } else {
                    if (from != null){
                        orders = analyzer.getOrders(from);
                    } else {
                        orders = analyzer.getTop(100);
                    }
                }
            }
        }
        return orders;
    }
}
