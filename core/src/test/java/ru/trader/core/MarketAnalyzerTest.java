package ru.trader.core;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.TestUtil;
import ru.trader.analysis.FilteredMarket;
import ru.trader.analysis.graph.Edge;
import ru.trader.analysis.graph.PPath;
import ru.trader.store.simple.*;

import java.util.Collection;
import java.util.List;

public class MarketAnalyzerTest extends Assert {
    private final static Logger LOG = LoggerFactory.getLogger(MarketAnalyzerTest.class);

    private FilteredMarket market;
    private Place v1;
    private Place v2;
    private Place v3;
    private Place v4;
    private Place v5;
    private Place v6;
    private Place v7;
    private Place v8;
    private Place v9;
    private Place v10;
    private Place v11;
    private Item ITEM1 = new SimpleItem("ITEM1");
    private Item ITEM2 = new SimpleItem("ITEM2");
    private Item ITEM3 = new SimpleItem("ITEM3");

    private void add(Place place, Offer offer){
        if (place.isEmpty()){
            place.add(new SimpleVendor());
        }
        Vendor vendor = place.get().iterator().next();
        vendor.add(offer);
    }

    @Before
    public void setUp() throws Exception {
        Market market = new SimpleMarket();

        v1 = new SimplePlace("v1_x0y0z0",0,0,0);
        v2 = new SimplePlace("v2_x1y0z0",1,0,0);
        v3 = new SimplePlace("v3_x0y1z0",0,1,0);
        v4 = new SimplePlace("v4_x0y0z1",0,0,1);
        v5 = new SimplePlace("v5_x1y1z0",1,1,0);

        v6 = new SimplePlace("v6_x110y100z100",110,100,100);
        v7 = new SimplePlace("v7_x115y100z100",115,100,100);
        v8 = new SimplePlace("v8_x105y105z100",105,105,100);
        v9 = new SimplePlace("v9_x100y115z100",100,115,100);
        v10 = new SimplePlace("v10_x100y100z100",100,100,100);
        v11 = new SimplePlace("v11_x105y105z105",105,105,105);

        market.add(v1);
        market.add(v2);
        market.add(v3);
        market.add(v4);
        market.add(v5);
        market.add(v6);
        market.add(v7);
        market.add(v8);
        market.add(v9);
        market.add(v10);
        market.add(v11);

        add(v6, new SimpleOffer(OFFER_TYPE.SELL, ITEM1, 100, 1));
        add(v7, new SimpleOffer(OFFER_TYPE.SELL, ITEM1, 100, 1));
        add(v9, new SimpleOffer(OFFER_TYPE.SELL, ITEM1, 100, 1));
        add(v10, new SimpleOffer(OFFER_TYPE.SELL, ITEM1, 100, 1));
        add(v6, new SimpleOffer(OFFER_TYPE.BUY, ITEM1, 50, 1));
        add(v7, new SimpleOffer(OFFER_TYPE.BUY, ITEM1, 120, 1));
        add(v9, new SimpleOffer(OFFER_TYPE.BUY, ITEM1, 200, 1));
        add(v10, new SimpleOffer(OFFER_TYPE.BUY, ITEM1, 150, 1));
        add(v9, new SimpleOffer(OFFER_TYPE.SELL, ITEM2, 100, 1));
        add(v6, new SimpleOffer(OFFER_TYPE.BUY, ITEM2, 140, 1));
        add(v7, new SimpleOffer(OFFER_TYPE.SELL, ITEM3, 154, 1));
        add(v10, new SimpleOffer(OFFER_TYPE.BUY, ITEM3, 140, 1));
        add(v11, new SimpleOffer(OFFER_TYPE.BUY, ITEM3, 500, 1));

        MarketFilter filter = new MarketFilter();
        this.market = new FilteredMarket(market, filter);
    }

    @Test
    public void testPaths() throws Exception {
        LOG.info("Start paths test");
        // maxDist - 1, fulltank - 1
        Ship ship = new Ship();
        ship.setMass(340);ship.setTank(0.6);
        Profile profile = new Profile(ship);
        profile.setJumps(5);
        MarketAnalyzer analyzer = new MarketAnalyzer(market, profile);

        Collection<List<Edge<Place>>> paths = analyzer.getPaths(v1, v2);
        TestUtil.assertPaths(paths, PPath.of(v1, v2));

        paths = analyzer.getPaths(v1, v3);
        TestUtil.assertPaths(paths, PPath.of(v1, v3));

        paths = analyzer.getPaths(v1, v4);
        TestUtil.assertPaths(paths, PPath.of(v1, v4));

        paths = analyzer.getPaths(v1, v5);
        assertTrue(paths.isEmpty());

        paths = analyzer.getPaths(v2, v5);
        TestUtil.assertPaths(paths, PPath.of(v2, v5));

        paths = analyzer.getPaths(v4, v3);
        assertTrue(paths.isEmpty());
    }

    @Test
    public void testPathsWithStock() throws Exception {
        LOG.info("Start paths with stock test");
        // jumpRange - 1, fulltank - 2
        Ship ship = new Ship();
        ship.setMass(340);ship.setTank(1.17);
        Profile profile = new Profile(ship);
        profile.setJumps(5);
        MarketAnalyzer analyzer = new MarketAnalyzer(market, profile);

        Collection<List<Edge<Place>>> paths = analyzer.getPaths(v1, v2);
        TestUtil.assertPaths(paths, PPath.of(v1, v2));

        paths = analyzer.getPaths(v1, v3);
        TestUtil.assertPaths(paths, PPath.of(v1, v3));

        paths = analyzer.getPaths(v1, v4);
        TestUtil.assertPaths(paths, PPath.of(v1, v4));

        paths = analyzer.getPaths(v1, v5);
        TestUtil.assertPaths(paths, PPath.of(v1, v2, v5), PPath.of(v1, v3, v5));

        paths = analyzer.getPaths(v2, v5);
        TestUtil.assertPaths(paths, PPath.of(v2, v5));

        paths = analyzer.getPaths(v4, v3);
        TestUtil.assertPaths(paths, PPath.of(v4, v1, v3));
    }


    @Test
    public void testPathsWithStockAndRefill() throws Exception {
        LOG.info("Start paths with stock and refill test");
        // jumpRange - 10, fulltank - 15
        Ship ship = new Ship();
        ship.setMass(30);ship.setTank(0.7);
        Profile profile = new Profile(ship);
        profile.setJumps(2);
        MarketAnalyzer analyzer = new MarketAnalyzer(market, profile);

        Collection<List<Edge<Place>>> paths = analyzer.getPaths(v10, v6);
        TestUtil.assertPaths(paths, PPath.of(v10, v6), PPath.of(v10, v11, v6),
                PPath.of(v10, v8, v6));

        paths = analyzer.getPaths(v1, v3);
        TestUtil.assertPaths(paths, PPath.of(v1, v3), PPath.of(v1, v2, v3),
                PPath.of(v1, v4, v3), PPath.of(v1, v5, v3)
        );
    }

    @Test
    public void testPathsWithStockAndRefill2() throws Exception {
        LOG.info("Start paths with stock and refill test 2");
        // jumpRange - 10, fulltank - 15
        Ship ship = new Ship();
        ship.setMass(30);ship.setTank(0.7);
        Profile profile = new Profile(ship);
        profile.setJumps(3);
        MarketAnalyzer analyzer = new MarketAnalyzer(market, profile);

        Collection<List<Edge<Place>>> paths = analyzer.getPaths(v10, v6);
        TestUtil.assertPaths(paths, PPath.of(v10, v6), PPath.of(v10, v8, v6), PPath.of(v10, v11, v6),
                PPath.of(v10, v8, v11, v6), PPath.of(v10, v8, v10, v6), PPath.of(v10, v11, v10, v6),
                PPath.of(v10, v6, v7, v6), PPath.of(v10, v6, v8, v6), PPath.of(v10, v6, v11, v6),
                PPath.of(v10, v6, v10, v6));

        paths = analyzer.getPaths(v10, v7);
        TestUtil.assertPaths(paths, PPath.of(v10, v6, v7), PPath.of(v10, v11, v6, v7),
                PPath.of(v10, v8, v6, v7)
        );

        paths = analyzer.getPaths(v10, v8);
        TestUtil.assertPaths(paths, PPath.of(v10, v8), PPath.of(v10, v11, v8), PPath.of(v10, v6, v8),
                PPath.of(v10, v8, v11, v8), PPath.of(v10, v8, v10, v8), PPath.of(v10, v8, v6, v8),
                PPath.of(v10, v11, v10, v8), PPath.of(v10, v11, v6, v8), PPath.of(v10, v6, v11, v8),
                PPath.of(v10, v6, v10, v8));

        paths = analyzer.getPaths(v10, v9);
        assertTrue(paths.isEmpty());

        paths = analyzer.getPaths(v10, v10);
        TestUtil.assertPaths(paths, PPath.of(v10, v11, v10), PPath.of(v10, v6, v10),
                PPath.of(v10, v11, v6, v10), PPath.of(v10, v6, v11, v10),
                PPath.of(v10, v8, v10), PPath.of(v10, v8, v11, v10),
                PPath.of(v10, v8, v6, v10), PPath.of(v10, v8, v6, v10));
    }



    @After
    public void tearDown() throws Exception {
        market = null;

    }
}
