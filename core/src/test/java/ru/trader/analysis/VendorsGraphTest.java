package ru.trader.analysis;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.analysis.graph.Crawler;
import ru.trader.analysis.graph.SimpleCollector;
import ru.trader.analysis.graph.Vertex;
import ru.trader.core.*;
import ru.trader.store.simple.Store;

import java.io.InputStream;
import java.util.stream.Collectors;


public class VendorsGraphTest extends Assert {
    private final static Logger LOG = LoggerFactory.getLogger(VendorsGraphTest.class);

    private Market world;
    private FilteredMarket fWorld;

    private Place breksta;
    private Place bhadaba;
    private Place lhs1541;
    private Place itza;

    @Before
    public void setUp() throws Exception {
        InputStream is = getClass().getResourceAsStream("/test3.xml");
        world = Store.loadFromFile(is);
        breksta = world.get("Breksta");
        bhadaba = world.get("Bhadaba");
        lhs1541 = world.get("LHS 1541");
        itza = world.get("Itza");

        MarketFilter filter = new MarketFilter();
        fWorld = new FilteredMarket(world, filter);
    }

    @Test
    public void testBuild() throws Exception {
        Vendor grantTerminal = breksta.get("Grant Terminal");
        Vendor perezMarket = breksta.get("Perez market");
        Vendor kandelRing = bhadaba.get("Kandel Ring");
        Vendor robertsHub = bhadaba.get("Roberts Hub");
        Vendor cabreraDock = lhs1541.get("Cabrera Dock");
        Vendor hallerPort = lhs1541.get("Haller Port");
        Vendor luikenPort = itza.get("Luiken Port");
        Ship ship = new Ship();
        ship.setCargo(24); ship.setEngine(2,'A');
        Profile profile = new Profile(ship);
        LOG.info("Start build test");
        profile.setBalance(100000); profile.setJumps(6);
        Scorer scorer = new Scorer(fWorld, profile);
        LOG.info("Build vendors graph");
        VendorsGraph vGraph = new VendorsGraph(scorer);
        vGraph.build(cabreraDock, fWorld.getMarkets(true).collect(Collectors.toList()));

        SimpleCollector<Vendor> paths = new SimpleCollector<>();
        Crawler<Vendor> crawler = vGraph.crawler(paths::add);

        crawler.findMin(cabreraDock, 100);
        assertEquals(100, paths.get().size());
        paths.clear();

        Vertex<Vendor> x = vGraph.getRoot();
        assertNotNull(x);
    }

    @After
    public void tearDown() throws Exception {
        world = null;
        fWorld = null;
    }
}
