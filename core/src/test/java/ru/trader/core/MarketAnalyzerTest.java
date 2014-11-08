package ru.trader.core;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.TestUtil;
import ru.trader.graph.Path;
import ru.trader.store.simple.*;

import java.util.Collection;

public class MarketAnalyzerTest extends Assert {
    private final static Logger LOG = LoggerFactory.getLogger(MarketAnalyzerTest.class);

    private static Market market = new SimpleMarket();
    private static Place v1;
    private static Place v2;
    private static Place v3;
    private static Place v4;
    private static Place v5;
    private static Place v6;
    private static Place v7;
    private static Place v8;
    private static Place v9;
    private static Place v10;
    private static Place v11;
    private static Item ITEM1 = new SimpleItem("ITEM1");
    private static Item ITEM2 = new SimpleItem("ITEM2");
    private static Item ITEM3 = new SimpleItem("ITEM3");

    private void add(Place place, Offer offer){
        if (place.isEmpty()){
            place.add(new SimpleVendor());
        }
        Vendor vendor = place.get().iterator().next();
        vendor.add(offer);
    }

    @Before
    public void setUp() throws Exception {
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

    }


    @Test
    public void testPaths() throws Exception {
        LOG.info("Start paths test");
        MarketAnalyzer analyzer = new MarketAnalyzer(market);
        analyzer.setJumps(5);analyzer.setMaxDistance(1);analyzer.setTank(1);

        Collection<Path<Place>> paths = analyzer.getPaths(v1, v2);
        TestUtil.assertCollectionEquals(paths, Path.toPath(v1, v2));

        paths = analyzer.getPaths(v1, v3);
        TestUtil.assertCollectionEquals(paths, Path.toPath(v1, v3));

        paths = analyzer.getPaths(v1, v4);
        TestUtil.assertCollectionEquals(paths, Path.toPath(v1, v4));

        paths = analyzer.getPaths(v1, v5);
        assertTrue(paths.isEmpty());

        paths = analyzer.getPaths(v2, v5);
        TestUtil.assertCollectionEquals(paths, Path.toPath(v2, v5));

        paths = analyzer.getPaths(v4, v3);
        assertTrue(paths.isEmpty());
    }

    @Test
    public void testPathsWithStock() throws Exception {
        LOG.info("Start paths with stock test");
        MarketAnalyzer analyzer = new MarketAnalyzer(market);
        analyzer.setJumps(5);analyzer.setMaxDistance(1);analyzer.setTank(2);

        Collection<Path<Place>> paths = analyzer.getPaths(v1, v2);
        TestUtil.assertCollectionContainAll(paths, Path.toPath(v1, v2));

        paths = analyzer.getPaths(v1, v3);
        TestUtil.assertCollectionContainAll(paths, Path.toPath(v1, v3));

        paths = analyzer.getPaths(v1, v4);
        TestUtil.assertCollectionContainAll(paths, Path.toPath(v1, v4));

        paths = analyzer.getPaths(v1, v5);
        TestUtil.assertCollectionContainAll(paths, Path.toPath(v1, v2, v5), Path.toPath(v1, v3, v5));

        paths = analyzer.getPaths(v2, v5);
        TestUtil.assertCollectionContainAll(paths, Path.toPath(v2, v5));

        paths = analyzer.getPaths(v4, v3);
        TestUtil.assertCollectionContainAll(paths, Path.toPath(v4, v1, v3));
    }


    @Test
    public void testPathsWithStockAndRefill() throws Exception {
        LOG.info("Start paths with stock and refill test");
        MarketAnalyzer analyzer = new MarketAnalyzer(market);
        analyzer.setJumps(2);analyzer.setMaxDistance(10);analyzer.setTank(15);

        Collection<Path<Place>> paths = analyzer.getPaths(v10, v6);
        TestUtil.assertCollectionContainAll(paths, Path.toPath(v10, v6), Path.toPath(v10, v11, v6),
                Path.toPath(v10, v8, v6));

        paths = analyzer.getPaths(v1, v3);
        TestUtil.assertCollectionContainAll(paths, Path.toPath(v1, v3), Path.toPath(v1, v2, v3),
                Path.toPath(v1, v4, v3), Path.toPath(v1, v5, v3)
        );
    }

    @Test
    public void testPathsWithStockAndRefill2() throws Exception {
        LOG.info("Start paths with stock and refill test 2");
        MarketAnalyzer analyzer = new MarketAnalyzer(market);
        analyzer.setJumps(3);analyzer.setMaxDistance(10);analyzer.setTank(15);

        Collection<Path<Place>> paths = analyzer.getPaths(v10, v6);
        TestUtil.assertCollectionContainAll(paths, Path.toPath(v10, v6), Path.toPath(v10, v11, v6), Path.toPath(v10, v11, v10, v6),
                Path.toPath(v10, v8, v6), Path.toPath(v10, v8, v10, v6), Path.toPath(v10, v8, v11, v6));

        paths = analyzer.getPaths(v10, v7);
        TestUtil.assertCollectionContainAll(paths, Path.toPath(v10, v6, v7), Path.toPath(v10, v11, v6, v7),
                Path.toPath(v10, v8, v6, v7)
        );

        paths = analyzer.getPaths(v10, v8);
        TestUtil.assertCollectionContainAll(paths, Path.toPath(v10, v8), Path.toPath(v10, v11, v8),
                Path.toPath(v10, v11, v6, v8), Path.toPath(v10, v6, v8), Path.toPath(v10, v6, v11, v8),
                Path.toPath(v10, v11, v10, v8), Path.toPath(v10, v6, v10, v8));

        paths = analyzer.getPaths(v10, v9);
        assertTrue(paths.isEmpty());

        paths = analyzer.getPaths(v10, v10);
        TestUtil.assertCollectionContainAll(paths, Path.toPath(v10, v11, v10), Path.toPath(v10, v6, v10),
                Path.toPath(v10, v11, v6, v10), Path.toPath(v10, v6, v11, v10),
                Path.toPath(v10, v8, v10), Path.toPath(v10, v8, v11, v10),
                Path.toPath(v10, v8, v6, v10), Path.toPath(v10, v8, v6, v10));
    }



    @After
    public void tearDown() throws Exception {
        market = new SimpleMarket();

    }
}
