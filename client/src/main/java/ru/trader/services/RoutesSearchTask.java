package ru.trader.services;

import ru.trader.analysis.Route;
import ru.trader.core.Place;
import ru.trader.core.Vendor;
import ru.trader.model.MarketModel;

import java.util.Collection;

public class RoutesSearchTask extends AnalyzerTask<Collection<Route>>{
    private final Place from;
    private final Vendor stationFrom;
    private final Place to;
    private final Vendor stationTo;

    public RoutesSearchTask(MarketModel market, Place from, Vendor stationFrom, Place to, Vendor stationTo, double balance) {
        super(market);
        this.from = from;
        this.stationFrom = stationFrom;
        this.to = to;
        this.stationTo = stationTo;
        market.getAnalyzer().getProfile().setBalance(balance);
    }

    @Override
    protected Collection<Route> call() throws Exception {
        Collection<Route> routes;

        if (stationFrom != null) {
            if (stationTo != null) {
                routes = analyzer.getRoutes(stationFrom, stationTo);
            } else {
                if (to != null) {
                    routes = analyzer.getRoutes(stationFrom, to);
                } else {
                    routes = analyzer.getLoops(stationFrom, 100);
                }
            }
        } else {
            if (stationTo != null) {
                routes = analyzer.getRoutes(from, stationTo);
            } else {
                if (to != null) {
                    routes = analyzer.getRoutes(from, to);
                } else {
                    if (from != null){
                        routes = analyzer.getRoutes(from);
                    } else {
                        routes = analyzer.getTopRoutes(100);
                    }
                }
            }
        }
        return routes;
    }
}

