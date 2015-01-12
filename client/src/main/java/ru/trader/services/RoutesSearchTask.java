package ru.trader.services;

import ru.trader.core.Place;
import ru.trader.core.Vendor;
import ru.trader.graph.PathRoute;
import ru.trader.model.MarketModel;

import java.util.Collection;

public class RoutesSearchTask extends AnalyzerTask<Collection<PathRoute>>{
    private final Place from;
    private final Vendor stationFrom;
    private final Place to;
    private final Vendor stationTo;
    private final double balance;

    public RoutesSearchTask(MarketModel market, Place from, Vendor stationFrom, Place to, Vendor stationTo, double balance) {
        super(market);
        this.from = from;
        this.stationFrom = stationFrom;
        this.to = to;
        this.stationTo = stationTo;
        this.balance = balance;
    }

    @Override
    protected Collection<PathRoute> call() throws Exception {
        Collection<PathRoute> routes;

        if (stationFrom != null) {
            if (stationTo != null) {
                routes = analyzer.getPaths(stationFrom, stationTo, balance);
            } else {
                if (to != null) {
                    routes = analyzer.getPaths(stationFrom, to, balance);
                } else {
                    routes = analyzer.getPaths(stationFrom, balance);
                }
            }
        } else {
            if (stationTo != null) {
                routes = analyzer.getPaths(from, stationTo, balance);
            } else {
                if (to != null) {
                    routes = analyzer.getPaths(from, to, balance);
                } else {
                    if (from != null){
                        routes = analyzer.getPaths(from, balance);
                    } else {
                        routes = analyzer.getTopPaths(balance);
                    }
                }
            }
        }
        return routes;
    }
}

