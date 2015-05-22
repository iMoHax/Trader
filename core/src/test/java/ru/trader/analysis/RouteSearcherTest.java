package ru.trader.analysis;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.core.*;
import ru.trader.store.simple.Store;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

public class RouteSearcherTest extends Assert{
    private final static Logger LOG = LoggerFactory.getLogger(RouteSearcherTest.class);

    private Market world;
    private FilteredMarket fWorld;

    private Place ithaca;
    private Place lhs3262;
    private Place morgor;
    private Place lhs3006;

    @Before
    public void setUp() throws Exception {
        InputStream is = getClass().getResourceAsStream("/world.xml");
        world = Store.loadFromFile(is);
        ithaca = world.get("Ithaca");
        lhs3262 = world.get("LHS 3262");
        morgor = world.get("Morgor");
        lhs3006 = world.get("LHS 3006");

        MarketFilter filter = new MarketFilter();
        fWorld = new FilteredMarket(world, filter);
    }

    @Test
    public void testRoutes() throws Exception {
        // Balance: 6000000, cargo: 440, tank: 40, distance: 13.4, jumps: 6
        // Ithaca (Palladium to LHS 3262) -> Morgor -> LHS 3006 -> LHS 3262 (Consumer Technology to Ithaca) -> LHS 3006 -> Morgor -> Ithaca
        // Profit: 981200, avg: 490600, distance: 67.5, lands: 2
        Vendor ithaca_st = ithaca.get().iterator().next();
        Vendor lhs3262_st = lhs3262.get().iterator().next();
        Vendor morgor_st = morgor.get().iterator().next();
        Vendor lhs3006_st = lhs3006.get().iterator().next();
        Ship ship = new Ship();
        ship.setCargo(440); ship.setTank(15);
        ship.setEngine(5, 'A'); ship.setMass(466);
        Profile profile = new Profile(ship);
        profile.setBalance(6000000); profile.setJumps(6);
        profile.setRoutesCount(100);
        Scorer scorer = new Scorer(fWorld, profile);

        LOG.info("Start test routes");
        RouteSearcher searcher = new RouteSearcher(scorer);

        Route route = new Route(new RouteEntry(ithaca_st, false, 3.3789702637348586d, 0));
        route.add(new RouteEntry(morgor_st, false, 4.137765020523591d, 0));
        route.add(new RouteEntry(lhs3006_st, false, 4.0674474942172765d, 0));
        route.add(new RouteEntry(lhs3262_st, true, 4.149937831634785d, 0));
        route.add(new RouteEntry(lhs3006_st, false, 4.1292528548103d, 0));
        route.add(new RouteEntry(morgor_st, false, 3.3050364899848566, 0));
        route.add(new RouteEntry(ithaca_st, false, 3.3483447506734136, 0));
        RouteFiller filler = new RouteFiller(scorer);
        filler.fill(route);

        List<Route> apaths = searcher.getRoutes(ithaca_st, ithaca_st, fWorld.getMarkets(true).collect(Collectors.toList()));
        Route actual = apaths.stream().findFirst().get();
        //assertTrue("Routes is different",expect.isRoute(actual));

    }
/*
    @Test
    public void testRoutes2() throws Exception {
        // Balance: 6000000, cargo: 440, tank: 40, distance: 13.6, jumps: 6
        // Ithaca (Palladium to LHS 3262) -> Morgor -> LHS 3006 -> LHS 3262 (Consumer Technology to Ithaca) -> LHS 3006 -> Morgor -> Ithaca
        // Profit: 981200, avg: 490600, distance: 67.5, lands: 2
        Vendor ithaca = market.get().stream().filter((v)->v.getName().equals("Ithaca")).findFirst().get().get().iterator().next();
        Vendor lhs3262 = market.get().stream().filter((v)->v.getName().equals("LHS 3262")).findFirst().get().get().iterator().next();

        RouteSearcher searcher = new RouteSearcher(13.6, 40);
        RouteGraph graph = new RouteGraph(ithaca, market.getVendors(true), 40, 13.6, true, 6);
        graph.setCargo(440);
        graph.setBalance(6000000);

        List<Path<Vendor>> epaths = graph.getPathsTo(ithaca, 10);
        PathRoute expect = epaths.stream().map(p -> (PathRoute) p).findFirst().get();

        List<PathRoute> apaths = searcher.getPaths(ithaca, ithaca, market.getVendors(true), 6, 6000000, 440, 10);
        PathRoute actual = apaths.stream().findFirst().get();
        assertTrue("Routes is different",expect.isRoute(actual));

        graph = new RouteGraph(lhs3262, market.getVendors(true), 40, 13.6, true, 6);
        graph.setCargo(440);
        graph.setBalance(6000000);

        expect = graph.getPathsTo(lhs3262, 10).stream().map(p -> (PathRoute)p).findFirst().get();
        apaths = searcher.getPaths(lhs3262, lhs3262, market.getVendors(true), 6, 6000000, 440, 10);
        actual = apaths.stream().findFirst().get();
        assertEquals("Routes is different",expect.getAvgProfit(), actual.getAvgProfit(), 0.00001);

    }
*/
}
