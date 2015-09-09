package ru.trader.services;

import ru.trader.analysis.CrawlerSpecificator;
import ru.trader.analysis.Route;
import ru.trader.core.Place;
import ru.trader.core.Profile;
import ru.trader.core.Vendor;
import ru.trader.model.MarketModel;

import java.util.Collection;

public class RoutesSearchTask extends AnalyzerTask<Collection<Route>>{
    private final Place from;
    private final Vendor stationFrom;
    private final Place to;
    private final Vendor stationTo;
    private final CrawlerSpecificator specificator;

    public RoutesSearchTask(MarketModel market, Place from, Vendor stationFrom, Place to, Vendor stationTo, Profile profile, CrawlerSpecificator specificator) {
        super(market, profile);
        this.from = from;
        this.stationFrom = stationFrom;
        this.to = to;
        this.stationTo = stationTo;
        this.specificator = specificator;
        if (stationTo != null){
            specificator.target(stationTo);
        }
    }

    @Override
    protected Collection<Route> call() throws Exception {
        Collection<Route> routes;

        if (stationFrom != null) {
            if (stationTo != null) {
                routes = analyzer.getRoutes(stationFrom, stationTo, specificator);
            } else {
                if (to != null) {
                    routes = analyzer.getRoutes(stationFrom, to, specificator);
                } else {
                    routes = analyzer.getRoutes(stationFrom, specificator);
                }
            }
        } else {
            if (stationTo != null) {
                routes = analyzer.getRoutes(from, specificator);
            } else {
                if (to != null) {
                    routes = analyzer.getRoutes(from, to, specificator);
                } else {
                    if (from != null){
                        routes = analyzer.getRoutes(from, specificator);
                    } else {
                        routes = analyzer.getTopRoutes(100);
                    }
                }
            }
        }
        return routes;
    }
}

