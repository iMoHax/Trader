package ru.trader.analysis;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.core.*;
import ru.trader.store.simple.Store;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
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
    private Place dnDraconis;
    private Place aulin;
    private Place cmDraco;
    private Place aulis;

    @Before
    public void setUp() throws Exception {
        InputStream is = getClass().getResourceAsStream("/world.xml");
        world = Store.loadFromFile(is);
        ithaca = world.get("Ithaca");
        lhs3262 = world.get("LHS 3262");
        morgor = world.get("Morgor");
        lhs3006 = world.get("LHS 3006");
        dnDraconis = world.get("DN Draconis");
        aulin = world.get("Aulin");
        cmDraco = world.get("CM Draco");
        aulis = world.get("Aulis");

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
        Vendor aulin_st = aulin.get().iterator().next();
        Vendor cmDraco_st = cmDraco.get().iterator().next();
        Ship ship = new Ship();
        ship.setCargo(440); ship.setTank(15);
        ship.setEngine(5, 'A'); ship.setMass(466);
        Profile profile = new Profile(ship);
        profile.setBalance(6000000); profile.setJumps(6);
        profile.setRoutesCount(100); profile.setLands(3);
        Scorer scorer = new Scorer(fWorld, profile);

        LOG.info("Start test routes, 3 lands");
        RouteSearcher searcher = new RouteSearcher(scorer);

        Route route = new Route(new RouteEntry(ithaca_st, 0, 3.3789702637348586d, 0));
        route.add(new RouteEntry(morgor.asTransit(), 0, 4.137765020523591d, 0));
        route.add(new RouteEntry(lhs3006.asTransit(), 0, 4.0674474942172765d, 0));
        route.add(new RouteEntry(lhs3262_st, 15, 4.149937831634785d, 0));
        route.add(new RouteEntry(lhs3006.asTransit(), 0, 4.1292528548103d, 0));
        route.add(new RouteEntry(morgor.asTransit(), 0, 3.3050364899848566, 0));
        route.add(new RouteEntry(ithaca_st, 0, 0, 0));
        RouteFiller filler = new RouteFiller(scorer);
        filler.fill(route);

        assertEquals(978883.1, route.getProfit(), 0.1);
        assertEquals(2, route.getLands());
        assertEquals(72.42, route.getDistance(), 0.01);

        Collection<Vendor> world = fWorld.getMarkets(true).collect(Collectors.toList());
        List<Route> apaths = searcher.getRoutes(ithaca_st, ithaca_st, world);
/*        List<Route> apaths = searcher.getRoutes(ithaca_st, ithaca_st, Arrays.asList(ithaca_st, lhs3262_st,
                morgor_st, lhs3006_st, ithaca.asTransit(), lhs3262.asTransit(),
                morgor.asTransit(), lhs3006.asTransit()));
*/
        Route actual = apaths.stream().findFirst().get();
        assertEquals("Routes is different", route, actual);

        LOG.info("Start test routes, 4 lands");
        profile.setLands(4);
        route = new Route(new RouteEntry(ithaca_st, 0, 3.3789702637348586d, 0));
        route.add(new RouteEntry(morgor.asTransit(), 0, 4.137765020523591d, 0));
        route.add(new RouteEntry(lhs3006.asTransit(), 0, 4.0674474942172765d, 0));
        route.add(new RouteEntry(lhs3262_st, 15, 0.644029909978323d, 0));
        route.add(new RouteEntry(dnDraconis.asTransit(), 0, 4.437544442558194d, 0));
        route.add(new RouteEntry(cmDraco_st, 0, 4.385307711185104d, 0));
        route.add(new RouteEntry(dnDraconis.asTransit(), 0, 0.6279317619086441d, 0));
        route.add(new RouteEntry(lhs3262_st, 15, 4.149937831634785d, 0));
        route.add(new RouteEntry(lhs3006.asTransit(), 0, 4.1292528548103d, 0));
        route.add(new RouteEntry(morgor.asTransit(), 0, 3.3050364899848566, 0));
        route.add(new RouteEntry(ithaca_st, 0, 0, 0));
        filler = new RouteFiller(scorer);
        filler.fill(route);

        assertEquals(1967873.6, route.getProfit(), 0.1);
        assertEquals(4, route.getLands());
        assertEquals(109.51, route.getDistance(), 0.01);

        apaths = searcher.getRoutes(ithaca_st, ithaca_st, world);
        actual = apaths.stream().findFirst().get();
        assertEquals(route, actual);

    }

    @Test
    public void testRoutesByTime() throws Exception {
        // Balance: 6000000, cargo: 440, tank: 40, distance: 13.4, jumps: 6
        // Ithaca (Palladium to Aulis) -> Aulis (Consumer Technology to Ithaca) -> Aulis
        // Profit: 231093.9, time: 476, distance: 17.36, lands: 2
        Vendor ithaca_st = ithaca.get().iterator().next();
        Vendor aulis_st = aulis.get().iterator().next();
        Ship ship = new Ship();
        ship.setCargo(440); ship.setTank(15);
        ship.setEngine(5, 'A'); ship.setMass(466);
        Profile profile = new Profile(ship);
        profile.setBalance(6000000); profile.setJumps(6);
        profile.setRoutesCount(100); profile.setLands(3);
        Scorer scorer = new Scorer(fWorld, profile);

        LOG.info("Start search by times test");
        RouteSearcher searcher = new RouteSearcher(scorer);

        Route route = new Route(new RouteEntry(ithaca_st, 0, 1.73439683972718d, 0));
        route.add(new RouteEntry(aulis_st, 0, 1.726405672421771d, 0));
        route.add(new RouteEntry(ithaca_st, 0, 0, 0));
        RouteFiller filler = new RouteFiller(scorer);
        filler.fill(route);

        assertEquals(231093.9, route.getProfit(), 0.1);
        assertEquals(476, route.getTime(), 0.1);
        assertEquals(2, route.getLands());
        assertEquals(17.36, route.getDistance(), 0.01);

        CrawlerSpecificator specificator = new CrawlerSpecificator();
        specificator.setByTime(true);
        Collection<Vendor> world = fWorld.getMarkets(true).collect(Collectors.toList());
        List<Route> apaths = searcher.search(ithaca_st, ithaca_st, world, profile.getRoutesCount(), specificator);
        Route actual = apaths.stream().findFirst().get();
        assertEquals("Routes is different", route, actual);
    }

    @Test
    public void testLoopRoutes() throws Exception {
        // Balance: 6000000, cargo: 440, tank: 40, distance: 13.4, jumps: 6
        Ship ship = new Ship();
        ship.setCargo(440); ship.setTank(15);
        ship.setEngine(5, 'A'); ship.setMass(466);
        Profile profile = new Profile(ship);
        profile.setBalance(6000000); profile.setJumps(6);
        profile.setRoutesCount(100); profile.setLands(3);
        Scorer scorer = new Scorer(fWorld, profile);

        LOG.info("Start loop search ");
        RouteSearcher searcher = new RouteSearcher(scorer);
        Vendor ithaca_st = ithaca.get().iterator().next();
        Collection<Vendor> world = fWorld.getMarkets(true).collect(Collectors.toList());

        List<Route> apaths = searcher.getLoops(ithaca_st, world, profile.getRoutesCount());
        assertEquals(39, apaths.size());
        Collection<Vendor> vendors = new ArrayList<>(40);
        apaths.forEach(route -> {
            List<RouteEntry> entries = route.getEntries();
            Vendor v = entries.get(entries.size()-1).getVendor();
            assertFalse(vendors.contains(v));
            vendors.add(v);
/*            List<Route> p = searcher.getRoutes(v, v, world, 1);
            double profit = route.getProfit() - route.get(0).getProfit();
            long time = route.getTime() - route.get(0).getFullTime();
            double score = profit/time;
            Route oRoute = p.get(0);
            LOG.info("{} - loop", v);
            LOG.info("profit: {}, time: {},  score: {} ", profit, time, score);
            LOG.info("profit: {}, time: {},  score: {} ", oRoute.getProfit(), oRoute.getTime(), oRoute.getScore());
            assertTrue(score <= oRoute.getScore());*/
        });
        assertTrue(vendors.contains(ithaca_st));
    }

}
