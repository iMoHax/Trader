package ru.trader.core;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.TestUtil;
import ru.trader.graph.Path;

import java.util.Collection;

public class MarketAnalyzerTest extends Assert {
    private final static Logger LOG = LoggerFactory.getLogger(MarketAnalyzerTest.class);

    private static Market market = new SimpleMarket();
    private static Vendor v1;
    private static Vendor v2;
    private static Vendor v3;
    private static Vendor v4;
    private static Vendor v5;
    private static Vendor v6;
    private static Vendor v7;
    private static Vendor v8;
    private static Vendor v9;
    private static Vendor v10;
    private static Vendor v11;
    private static Item ITEM1 = new Item("ITEM1");
    private static Item ITEM2 = new Item("ITEM2");
    private static Item ITEM3 = new Item("ITEM3");

    @Before
    public void setUp() throws Exception {
        v1 = new SimpleVendor("v1_x0y0z0");
        v2 = new SimpleVendor("v2_x1y0z0");
        v3 = new SimpleVendor("v3_x0y1z0");
        v4 = new SimpleVendor("v4_x0y0z1");
        v5 = new SimpleVendor("v5_x1y1z0");

        v6 = new SimpleVendor("v6_x110y100z100");
        v7 = new SimpleVendor("v7_x115y100z100");
        v8 = new SimpleVendor("v8_x105y105z100");
        v9 = new SimpleVendor("v9_x100y115z100");
        v10 = new SimpleVendor("v10_x100y100z100");
        v11 = new SimpleVendor("v11_x105y105z105");

        v1.setX(0); v1.setY(0); v1.setZ(0);
        v2.setX(1); v2.setY(0); v2.setZ(0);
        v3.setX(0); v3.setY(1); v3.setZ(0);
        v4.setX(0); v4.setY(0); v4.setZ(1);
        v5.setX(1); v5.setY(1); v5.setZ(0);
        v6.setX(110); v6.setY(100); v6.setZ(100);
        v7.setX(115); v7.setY(100); v7.setZ(100);
        v8.setX(105); v8.setY(105); v8.setZ(100);
        v9.setX(100); v9.setY(115); v9.setZ(100);
        v10.setX(100); v10.setY(100); v10.setZ(100);
        v11.setX(105); v11.setY(105); v11.setZ(105);

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

        market.add(v6, new Offer(OFFER_TYPE.SELL, ITEM1, 100));
        market.add(v7, new Offer(OFFER_TYPE.SELL, ITEM1, 100));
        market.add(v9, new Offer(OFFER_TYPE.SELL, ITEM1, 100));
        market.add(v10, new Offer(OFFER_TYPE.SELL, ITEM1, 100));
        market.add(v6, new Offer(OFFER_TYPE.BUY, ITEM1, 50));
        market.add(v7, new Offer(OFFER_TYPE.BUY, ITEM1, 120));
        market.add(v9, new Offer(OFFER_TYPE.BUY, ITEM1, 200));
        market.add(v10, new Offer(OFFER_TYPE.BUY, ITEM1, 150));
        market.add(v9, new Offer(OFFER_TYPE.SELL, ITEM2, 100));
        market.add(v6, new Offer(OFFER_TYPE.BUY, ITEM2, 140));
        market.add(v7, new Offer(OFFER_TYPE.SELL, ITEM3, 154));
        market.add(v10, new Offer(OFFER_TYPE.BUY, ITEM3, 140));
        market.add(v11, new Offer(OFFER_TYPE.BUY, ITEM3, 500));

    }


    @Test
    public void testPaths() throws Exception {
        LOG.info("Start paths test");
        MarketAnalyzer analyzer = new MarketAnalyzer(market);
        analyzer.setJumps(5);analyzer.setMaxDistance(1);analyzer.setTank(1);

        Collection<Path<Vendor>> paths = analyzer.getPaths(v1, v2);
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

        Collection<Path<Vendor>> paths = analyzer.getPaths(v1, v2);
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

        Collection<Path<Vendor>> paths = analyzer.getPaths(v10, v6);
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

        Collection<Path<Vendor>> paths = analyzer.getPaths(v10, v6);
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
