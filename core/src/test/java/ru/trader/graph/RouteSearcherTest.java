package ru.trader.graph;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import ru.trader.core.Market;
import ru.trader.core.Vendor;
import ru.trader.store.simple.Store;

import java.io.InputStream;
import java.util.List;

public class RouteSearcherTest extends Assert {
    private static Market market;

    @Before
    public void setUp() throws Exception {
        InputStream is = getClass().getResourceAsStream("/world.xml");
        market = Store.loadFromFile(is);
    }

    @Test
    public void testRoutes() throws Exception {
        // Balance: 6000000, cargo: 440, tank: 40, distance: 13.4, jumps: 6
        // Ithaca (Palladium to LHS 3262) -> Morgor -> LHS 3006 -> LHS 3262 (Consumer Technology to Ithaca) -> LHS 3006 -> Morgor -> Ithaca
        // Profit: 981200, avg: 490600, distance: 67.5, lands: 2
        Vendor ithaca = market.get().stream().filter((v)->v.getName().equals("Ithaca")).findFirst().get();

        RouteSearcher searcher = new RouteSearcher(13.4, 40);
        RouteGraph graph = new RouteGraph(ithaca, market.get(), 40, 13.4, true, 6);
        graph.setCargo(440);
        graph.setBalance(6000000);


        List<Path<Vendor>> epaths = graph.getPathsTo(ithaca, 10);
        PathRoute expect = epaths.stream().map(p -> (PathRoute) p).findFirst().get();

        List<PathRoute> apaths = searcher.getPaths(ithaca, ithaca, market.get(), 6, 6000000, 440, 10);
        PathRoute actual = apaths.stream().findFirst().get();
        assertTrue("Routes is different",expect.isRoute(actual));

    }

    @Test
    public void testRoutes2() throws Exception {
        // Balance: 6000000, cargo: 440, tank: 40, distance: 13.6, jumps: 6
        // Ithaca (Palladium to LHS 3262) -> Morgor -> LHS 3006 -> LHS 3262 (Consumer Technology to Ithaca) -> LHS 3006 -> Morgor -> Ithaca
        // Profit: 981200, avg: 490600, distance: 67.5, lands: 2
        Vendor ithaca = market.get().stream().filter((v)->v.getName().equals("Ithaca")).findFirst().get();
        Vendor lhs3262 = market.get().stream().filter((v)->v.getName().equals("LHS 3262")).findFirst().get();

        RouteSearcher searcher = new RouteSearcher(13.6, 40);
        RouteGraph graph = new RouteGraph(ithaca, market.get(), 40, 13.6, true, 6);
        graph.setCargo(440);
        graph.setBalance(6000000);

        List<Path<Vendor>> epaths = graph.getPathsTo(ithaca, 10);
        PathRoute expect = epaths.stream().map(p -> (PathRoute) p).findFirst().get();

        List<PathRoute> apaths = searcher.getPaths(ithaca, ithaca, market.get(), 6, 6000000, 440, 10);
        PathRoute actual = apaths.stream().findFirst().get();
        assertTrue("Routes is different",expect.isRoute(actual));

        graph = new RouteGraph(lhs3262, market.get(), 40, 13.6, true, 6);
        graph.setCargo(440);
        graph.setBalance(6000000);

        expect = graph.getPathsTo(lhs3262, 10).stream().map(p -> (PathRoute)p).findFirst().get();
        apaths = searcher.getPaths(lhs3262, lhs3262, market.get(), 6, 6000000, 440, 10);
        actual = apaths.stream().findFirst().get();
        assertEquals("Routes is different",expect.getAvgProfit(), actual.getAvgProfit(), 0.00001);

    }


}
