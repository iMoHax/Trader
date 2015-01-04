package ru.trader.core;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import ru.trader.TestUtil;
import ru.trader.graph.PathRoute;
import ru.trader.store.simple.Store;
import java.io.InputStream;
import java.util.Collection;
import java.util.Optional;

public class MarketAnalyzerTest2 extends Assert {

    @Test
    public void testRoutes() throws Exception {
        InputStream is = getClass().getResourceAsStream("/world.xml");
        Market market = Store.loadFromFile(is);
        MarketAnalyzer analyzer = new MarketAnalyzer(market);
        // Balance: 6000000, cargo: 440, tank: 40, distance: 13.4, jumps: 6
        // Ithaca (Palladium to LHS 3262) -> Morgor -> LHS 3006 -> LHS 3262 (Consumer Technology to Ithaca) -> LHS 3006 -> Morgor -> Ithaca
        // Profit: 981200, avg: 490600, distance: 67.5, lands: 2
        Vendor ithaca = market.get().stream().filter((v)->v.getName().equals("Ithaca")).findFirst().get().get().iterator().next();
        Vendor morgor = market.get().stream().filter((v)->v.getName().equals("Morgor")).findFirst().get().get().iterator().next();
        Vendor lhs3006 = market.get().stream().filter((v)->v.getName().equals("LHS 3006")).findFirst().get().get().iterator().next();
        Vendor lhs3262 = market.get().stream().filter((v)->v.getName().equals("LHS 3262")).findFirst().get().get().iterator().next();
        analyzer.setCargo(440);analyzer.setTank(40);analyzer.setMaxDistance(13.4);analyzer.setJumps(6);
        Collection<PathRoute> paths = analyzer.getPaths(ithaca, ithaca, 6000000);
        PathRoute expect = PathRoute.toPathRoute(ithaca, morgor, lhs3006, lhs3262, lhs3006, morgor, ithaca);
        Optional<PathRoute> path = paths.stream().filter((p)->p.equals(expect)).findFirst();
        assertTrue(path.isPresent());
        PathRoute actual = path.get().getRoot();
        TestUtil.assertCollectionContain(paths, expect);
        assertEquals(981200, actual.getProfit(), 0.00001);
        assertEquals(72.42, actual.getDistance(), 0.01);
        assertEquals(2, actual.getLandsCount());
        assertEquals(490600, actual.getAvgProfit() , 0.00001);
    }

    //test best avg profit
    @Test
    public void testRoutes2() throws Exception {
        InputStream is = getClass().getResourceAsStream("/test2.xml");
        Market market = Store.loadFromFile(is);
        MarketAnalyzer analyzer = new MarketAnalyzer(market);
        // Balance: 6000000, cargo: 104 tank: 150, distance: 12.6, jumps: 4
        // LHS 21 (Resonatic Separator to Sui Xing) -> Bonde -> Sui Xing (Palladium to LHS 21) -> Bonde -> LHS 21
        // Profit: 981200, avg: 490600, distance: 67.5, lands: 2
        Place lhs21 = market.get().stream().filter((v)->v.getName().equals("LHS 21")).findFirst().get();
        Place suiXing = market.get().stream().filter((v)->v.getName().equals("Sui Xing")).findFirst().get();
        analyzer.setCargo(104);analyzer.setTank(150);analyzer.setMaxDistance(12.6);analyzer.setJumps(4);analyzer.setPathsCount(100);
        Collection<PathRoute> paths = analyzer.getPaths(lhs21, lhs21, 6000000);
        Optional<PathRoute> path = paths.stream().findFirst();
        assertTrue(path.isPresent());
        PathRoute actual = path.get().getRoot();

        assertEquals(199056, actual.getProfit(), 0.00001);
        assertEquals(28.72, actual.getDistance(), 0.01);
        assertEquals(2, actual.getLandsCount());
        assertEquals(99528, actual.getAvgProfit() , 0.00001);

        Place aPlace = actual.get().getPlace();
        assertEquals(lhs21, aPlace);
        actual = actual.getNext().getNext();
        aPlace = actual.get().getPlace();
        assertEquals(suiXing, aPlace);
    }

}
