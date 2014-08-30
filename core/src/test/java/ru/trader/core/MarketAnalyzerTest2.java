package ru.trader.core;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import ru.trader.TestUtil;
import ru.trader.graph.PathRoute;
import ru.trader.store.Store;
import java.io.InputStream;
import java.util.Collection;

public class MarketAnalyzerTest2 extends Assert {
    private static MarketAnalyzer analyzer;
    private static Market market;

    @Before
    public void setUp() throws Exception {
        InputStream is = getClass().getResourceAsStream("/world.xml");
        market = Store.loadFromFile(is);
        analyzer = new MarketAnalyzer(market);
    }

    @Test
    public void testRoutes() throws Exception {
        // Balance: 6000000, cargo: 440, tank: 40, distance: 13.4, jumps: 6
        // Ithaca (Palladium to LHS 3262) -> Morgor -> LHS 3006 -> LHS 3262 (Consumer Technology to Ithaca) -> LHS 3006 -> Morgor -> Ithaca
        // Profit: 981200, avg: 490600, distance: 67.5, lands: 2
        Vendor ithaca = market.get().stream().filter((v)->v.getName().equals("Ithaca")).findFirst().get();
        Vendor morgor = market.get().stream().filter((v)->v.getName().equals("Morgor")).findFirst().get();
        Vendor lhs3006 = market.get().stream().filter((v)->v.getName().equals("LHS 3006")).findFirst().get();
        Vendor lhs3262 = market.get().stream().filter((v)->v.getName().equals("LHS 3262")).findFirst().get();
        analyzer.setCargo(440);analyzer.setTank(40);analyzer.setMaxDistance(13.4);analyzer.setJumps(6);
        Collection<PathRoute> paths = analyzer.getPaths(ithaca, ithaca, 6000000);
        PathRoute expect = PathRoute.toPathRoute(ithaca, morgor, lhs3006, lhs3262, lhs3006, morgor, ithaca);
        PathRoute actual = paths.stream().filter((p)->p.equals(expect)).findFirst().get().getRoot();
        TestUtil.assertCollectionContain(paths, expect);
        assertEquals(981200, actual.getProfit(), 0.00001);
        assertEquals(72.42, actual.getDistance(), 0.01);
        assertEquals(2, actual.getLandsCount());
        assertEquals(490600, actual.getProfit()/actual.getLandsCount() , 0.00001);
    }
}
