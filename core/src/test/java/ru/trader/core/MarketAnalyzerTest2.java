package ru.trader.core;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import ru.trader.TestUtil;
import ru.trader.analysis.*;
import ru.trader.store.simple.Store;
import java.io.InputStream;
import java.util.Collection;
import java.util.Optional;

public class MarketAnalyzerTest2 extends Assert {

    @Test
    public void testRoutes() throws Exception {
        InputStream is = getClass().getResourceAsStream("/world.xml");
        Market market = Store.loadFromFile(is);
        MarketFilter filter = new MarketFilter();
        FilteredMarket fWorld = new FilteredMarket(market, filter);
        // Balance: 6000000, cargo: 440, tank: 40, distance: 13.4, jumps: 6
        // Ithaca (Palladium to LHS 3262) -> Morgor -> LHS 3006 -> LHS 3262 (Consumer Technology to Ithaca) -> LHS 3006 -> Morgor -> Ithaca
        // Profit: 981200, avg: 490600, distance: 67.5, lands: 2
        Ship ship = new Ship();
        ship.setCargo(440); ship.setTank(15);
        ship.setEngine(5, 'A'); ship.setMass(466);
        Profile profile = new Profile(ship);
        profile.setBalance(6000000); profile.setJumps(6);
        profile.setRoutesCount(100);
        MarketAnalyzer analyzer = new MarketAnalyzer(fWorld, profile);
        Vendor ithaca = market.get("Ithaca").get().iterator().next();
        Vendor morgor = market.get("Morgor").asTransit();
        Vendor lhs3006 = market.get("LHS 3006").asTransit();
        Vendor lhs3262 = market.get("LHS 3262").get().iterator().next();
        Collection<Route> paths = analyzer.getRoutes(ithaca, ithaca);
        Route expect = new Route(new RouteEntry(ithaca, false, 3.3789702637348586d, 0));
        expect.add(new RouteEntry(morgor, false, 4.137765020523591d, 0));
        expect.add(new RouteEntry(lhs3006, false, 4.0674474942172765d, 0));
        expect.add(new RouteEntry(lhs3262, true, 4.149937831634785d, 0));
        expect.add(new RouteEntry(lhs3006, false, 4.1292528548103d, 0));
        expect.add(new RouteEntry(morgor, false, 3.3050364899848566, 0));
        expect.add(new RouteEntry(ithaca, false, 0, 0));
        RouteFiller filler = new RouteFiller(new Scorer(fWorld, profile));
        filler.fill(expect);

        Optional<Route> path = paths.stream().findFirst();
        assertTrue(path.isPresent());
        Route actual = path.get();
        assertEquals(expect, actual);
        assertEquals(981200, actual.getProfit(), 0.00001);
        assertEquals(72.42, actual.getDistance(), 0.01);
        assertEquals(2, actual.getLands());
    }

    //test best avg profit
    @Test
    public void testRoutes2() throws Exception {
        InputStream is = getClass().getResourceAsStream("/test2.xml");
        Market market = Store.loadFromFile(is);
        MarketFilter filter = new MarketFilter();
        FilteredMarket fWorld = new FilteredMarket(market, filter);
        // Balance: 6000000, cargo: 104 tank: 150, distance: 12.6, jumps: 4
        // LHS 21 -> Bonde -> LHS 21
        // Profit: 114816, distance: 8.16, lands: 2
        Ship ship = new Ship();
        ship.setCargo(104); ship.setTank(150);
        ship.setEngine(5, 'A'); ship.setMass(865);
        Profile profile = new Profile(ship);
        profile.setBalance(6000000); profile.setJumps(4);
        profile.setRoutesCount(100);
        MarketAnalyzer analyzer = new MarketAnalyzer(fWorld, profile);
        Place lhs21 = market.get("LHS 21");
        Place bonde = market.get("Bonde");
        Place suiXing = market.get("Sui Xing");
        Collection<Route> paths = analyzer.getRoutes(lhs21, lhs21);
        Optional<Route> path = paths.stream().findFirst();
        assertTrue(path.isPresent());
        Route actual = path.get();
        assertEquals(114816, actual.getProfit(), 0.00001);
        assertEquals(8.16, actual.getDistance(), 0.01);
        assertEquals(2, actual.getLands());

        Place aPlace = actual.get(0).getVendor().getPlace();
        assertEquals(lhs21, aPlace);
        aPlace = actual.get(1).getVendor().getPlace();
        assertEquals(bonde, aPlace);

        // If distance to station has small mult
        // LHS 21 (Resonatic Separator to Sui Xing) -> Bonde -> Sui Xing (Palladium to LHS 21) -> Bonde -> LHS 21
        // Profit: 199056, distance: 28.72, lands: 2

        profile.setDistanceMult(0.1);
        paths = analyzer.getRoutes(lhs21, lhs21);
        path = paths.stream().findFirst();
        assertTrue(path.isPresent());
        actual = path.get();

        assertEquals(199056, actual.getProfit(), 0.00001);
        assertEquals(28.72, actual.getDistance(), 0.01);
        assertEquals(2, actual.getLands());

        aPlace = actual.get(0).getVendor().getPlace();
        assertEquals(lhs21, aPlace);
        aPlace = actual.get(2).getVendor().getPlace();
        assertEquals(suiXing, aPlace);
    }

}
