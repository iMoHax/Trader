package ru.trader.analysis;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.TestUtil;
import ru.trader.core.*;
import ru.trader.store.simple.SimpleMarket;
import ru.trader.store.simple.SimpleOffer;

import java.util.List;

public class RouteFillerTest extends Assert {
    private final static Logger LOG = LoggerFactory.getLogger(RouteFillerTest.class);

    private Market market;
    private Item ITEM1;
    private Item ITEM2;
    private Item ITEM3;
    private Item ITEM4;
    private Vendor v1;
    private Vendor v2;
    private Vendor v3;
    private Vendor v4;
    private Vendor v5;

    private RouteFiller getFillerInstance(double balance, int cargo, boolean skipLandingTime, Market market){
        Ship ship = new Ship();
        ship.setCargo(cargo);
        Profile profile = new Profile(ship);
        profile.setBalance(balance);
        if (skipLandingTime){
            profile.setLandingTime(0);
        }
        Scorer scorer = new Scorer(new FilteredMarket(market, new MarketFilter()), profile);
        return new RouteFiller(scorer);
    }

    private Route initTest1(){
        LOG.info("Init test 1");
        market = new SimpleMarket();
        ITEM1 = market.addItem("ITEM1", null);
        ITEM2 = market.addItem("ITEM2", null);
        ITEM3 = market.addItem("ITEM3", null);
        v1 = market.addPlace("p1",0,0,0).addVendor("v1");
        v2 =  market.addPlace("p2",0,0,0).addVendor("v2");
        v1.addOffer(OFFER_TYPE.SELL, ITEM1, 100, -1);
        v1.addOffer(OFFER_TYPE.SELL, ITEM2, 200, -1);
        v1.addOffer(OFFER_TYPE.SELL, ITEM3, 300, -1);
        v2.addOffer(OFFER_TYPE.BUY, ITEM1, 300, -1);
        v2.addOffer(OFFER_TYPE.BUY, ITEM2, 350, -1);
        v2.addOffer(OFFER_TYPE.BUY, ITEM3, 400, -1);

        Route route = new Route(new RouteEntry(v1, 0, 0,0));
        route.add(new RouteEntry(v2, 0, 0,0));

        return route;
    }


    @Test
    public void testRoute1() throws Exception {
        LOG.info("Start route test 1");
        Route route = initTest1();
        RouteFiller filler = getFillerInstance(10000, 5, true, market);
        filler.fill(route);

        assertEquals(10000, route.getBalance(), 0.0001);
        assertEquals(1000, route.getProfit(), 0.0001);
        assertEquals(1, route.getLands());

        List<RouteEntry> entries = route.getEntries();

        RouteEntry entry = entries.get(0);
        Order order1 = new Order(v1.getSell(ITEM1), v2.getBuy(ITEM1), 5);
        assertEquals(1000, entry.getProfit(), 0.0001);
        TestUtil.assertCollectionEquals(entry.getOrders(), order1);
    }

    private Route initTest2(){
        LOG.info("Init test 2");
        market = new SimpleMarket();
        ITEM1 = market.addItem("ITEM1", null);
        ITEM2 = market.addItem("ITEM2", null);
        ITEM3 = market.addItem("ITEM3", null);
        v1 = market.addPlace("p1",0,0,0).addVendor("v1");
        v2 = market.addPlace("p2",0,0,0).addVendor("v2");
        v3 = market.addPlace("p3",0,0,0).addVendor("v3");
        v1.addOffer(OFFER_TYPE.SELL, ITEM1, 100, -1);
        v1.addOffer(OFFER_TYPE.SELL, ITEM3, 300, -1);
        v2.addOffer(OFFER_TYPE.SELL, ITEM2, 200, -1);
        v3.addOffer(OFFER_TYPE.BUY, ITEM1, 300, -1);
        v3.addOffer(OFFER_TYPE.BUY, ITEM2, 350, -1);
        v3.addOffer(OFFER_TYPE.BUY, ITEM3, 400, -1);

        Route route = new Route(new RouteEntry(v1, 0, 0,0));
        route.add(new RouteEntry(v2, 0, 0,0));
        route.add(new RouteEntry(v3, 0, 0,0));

        return route;
    }

    @Test
    public void testPathRoute2() throws Exception {
        LOG.info("Start route test 2");
        Route route = initTest2();
        RouteFiller filler = getFillerInstance(10000, 5, true, market);
        filler.fill(route);

        assertEquals(1000, route.getProfit(), 0.0001);
        assertEquals(1, route.getLands());

        List<RouteEntry> entries = route.getEntries();
        Order order1 = new Order(v1.getSell(ITEM1), v3.getBuy(ITEM1), 5);

        RouteEntry entry = entries.get(0);
        assertEquals(1000, entry.getProfit(), 0.0001);
        TestUtil.assertCollectionEquals(entry.getOrders(), order1);

        entry = entries.get(1);
        assertEquals(0, entry.getProfit(), 0.0001);
        assertTrue(entry.getOrders().isEmpty());
    }

    private Route initTest3(){
        LOG.info("Init test 3");
        market = new SimpleMarket();
        ITEM1 = market.addItem("ITEM1", null);
        ITEM2 = market.addItem("ITEM2", null);
        ITEM3 = market.addItem("ITEM3", null);
        v1 = market.addPlace("p1",0,0,0).addVendor("v1");
        v2 = market.addPlace("p2",0,0,0).addVendor("v2");
        v3 = market.addPlace("p3",0,0,0).addVendor("v3");
        v4 = market.addPlace("p4",0,0,0).addVendor("v4");
        v1.addOffer(OFFER_TYPE.SELL, ITEM1, 100, -1);
        v1.addOffer(OFFER_TYPE.SELL, ITEM2, 200, -1);
        v1.addOffer(OFFER_TYPE.SELL, ITEM3, 300, -1);
        v2.addOffer(OFFER_TYPE.SELL, ITEM1, 150, -1);
        v2.addOffer(OFFER_TYPE.SELL, ITEM3, 320, -1);
        v3.addOffer(OFFER_TYPE.SELL, ITEM3, 390, -1);
        v2.addOffer(OFFER_TYPE.BUY, ITEM2, 225, -1);
        v3.addOffer(OFFER_TYPE.BUY, ITEM1, 200, -1);
        v4.addOffer(OFFER_TYPE.BUY, ITEM3, 450, -1);

        Route route = new Route(new RouteEntry(v1, 0, 0,0));
        route.add(new RouteEntry(v2, 0, 0,0));
        route.add(new RouteEntry(v3, 0, 0,0));
        route.add(new RouteEntry(v4, 0, 0,0));

        return route;
    }

    @Test
    public void testPathRoute3() throws Exception {
        LOG.info("Start route test 3");
        Route route = initTest3();
        RouteFiller filler = getFillerInstance(10000, 5, true, market);
        filler.fill(route);

        assertEquals(800, route.getProfit(), 0.0001);
        assertEquals(2, route.getLands());

        List<RouteEntry> entries = route.getEntries();
        Order order1 = new Order(v1.getSell(ITEM1), v3.getBuy(ITEM1), 5);
        Order order7 = new Order(v3.getSell(ITEM3), v4.getBuy(ITEM3), 5);

        RouteEntry entry = entries.get(0);
        assertEquals(500, entry.getProfit(), 0.0001);
        TestUtil.assertCollectionEquals(entry.getOrders(), order1);

        entry = entries.get(1);
        assertEquals(0, entry.getProfit(), 0.0001);
        assertTrue(entry.getOrders().isEmpty());

        entry = entries.get(2);
        assertEquals(300, entry.getProfit(), 0.0001);
        TestUtil.assertCollectionEquals(entry.getOrders(), order7);
    }

    @Test
    public void testPathRoute3byLands() throws Exception {
        LOG.info("Start route test 3 by lands");
        Route route = initTest3();
        RouteFiller filler = getFillerInstance(10000, 5, false, market);
        filler.fill(route);

        assertEquals(750, route.getProfit(), 0.0001);
        assertEquals(1, route.getLands());

        List<RouteEntry> entries = route.getEntries();
        Order order3 = new Order(v1.getSell(ITEM3), v4.getBuy(ITEM3), 5);

        RouteEntry entry = entries.get(0);
        assertEquals(750, entry.getProfit(), 0.0001);
        TestUtil.assertCollectionEquals(entry.getOrders(), order3);

        entry = entries.get(1);
        assertEquals(0, entry.getProfit(), 0.0001);
        assertTrue(entry.getOrders().isEmpty());

        entry = entries.get(2);
        assertEquals(0, entry.getProfit(), 0.0001);
        assertTrue(entry.getOrders().isEmpty());
    }

    private Route initTest4(){
        LOG.info("Init test 4");
        market = new SimpleMarket();
        ITEM1 = market.addItem("ITEM1", null);
        ITEM2 = market.addItem("ITEM2", null);
        ITEM3 = market.addItem("ITEM3", null);
        v1 = market.addPlace("p1",0,0,0).addVendor("v1");
        v2 = market.addPlace("p2",0,0,0).addVendor("v2");
        v3 = market.addPlace("p3",0,0,0).addVendor("v3");
        v4 = market.addPlace("p4",0,0,0).addVendor("v4");
        v5 = market.addPlace("p5",0,0,0).addVendor("v5");
        v1.addOffer(OFFER_TYPE.SELL, ITEM1, 410, -1);
        v1.addOffer(OFFER_TYPE.SELL, ITEM2, 200, -1);
        v1.addOffer(OFFER_TYPE.SELL, ITEM3, 300, -1);
        v2.addOffer(OFFER_TYPE.SELL, ITEM2, 270, -1);
        v4.addOffer(OFFER_TYPE.SELL, ITEM1, 300, -1);
        v2.addOffer(OFFER_TYPE.BUY, ITEM1, 470, -1);
        v3.addOffer(OFFER_TYPE.BUY, ITEM2, 300, -1);
        v4.addOffer(OFFER_TYPE.BUY, ITEM3, 370, -1);
        v5.addOffer(OFFER_TYPE.BUY, ITEM1, 400, -1);

        Route route = new Route(new RouteEntry(v1, 0, 0,0));
        route.add(new RouteEntry(v2, 0, 0,0));
        route.add(new RouteEntry(v3, 0, 0,0));
        route.add(new RouteEntry(v4, 0, 0,0));
        route.add(new RouteEntry(v5, 0, 0,0));

        return route;
    }

    @Test
    public void testPathRoute4() throws Exception {
        LOG.info("Start route test 4");
        Route route = initTest4();
        RouteFiller filler = getFillerInstance(10000, 5, true, market);
        filler.fill(route);

        assertEquals(1000, route.getProfit(), 0.0001);
        assertEquals(3, route.getLands());

        List<RouteEntry> entries = route.getEntries();
        Order order3 = new Order(v1.getSell(ITEM2), v3.getBuy(ITEM2), 5);
        Order order6 = new Order(v4.getSell(ITEM1), v5.getBuy(ITEM1), 5);

        RouteEntry entry = entries.get(0);
        assertEquals(500, entry.getProfit(), 0.0001);
        TestUtil.assertCollectionEquals(entry.getOrders(), order3);

        entry = entries.get(1);
        assertEquals(0, entry.getProfit(), 0.0001);
        assertTrue(entry.getOrders().isEmpty());

        entry = entries.get(2);
        assertEquals(0, entry.getProfit(), 0.0001);
        assertTrue(entry.getOrders().isEmpty());

        entry = entries.get(3);
        assertEquals(500, entry.getProfit(), 0.0001);
        TestUtil.assertCollectionEquals(entry.getOrders(), order6);
    }

    private Route initTest5(){
        LOG.info("Init test 5");
        market = new SimpleMarket();
        ITEM1 = market.addItem("ITEM1", null);
        ITEM2 = market.addItem("ITEM2", null);
        ITEM3 = market.addItem("ITEM3", null);
        ITEM4 = market.addItem("ITEM4", null);
        v1 = market.addPlace("p1",0,0,0).addVendor("v1");
        v2 = market.addPlace("p2",0,0,0).addVendor("v2");
        v3 = market.addPlace("p3",0,0,0).addVendor("v3");
        v4 = market.addPlace("p4",0,0,0).addVendor("v4");
        v1.addOffer(OFFER_TYPE.SELL, ITEM1, 100, 5);
        v1.addOffer(OFFER_TYPE.SELL, ITEM2, 200, 5);
        v1.addOffer(OFFER_TYPE.SELL, ITEM3, 300, 5);
        v1.addOffer(OFFER_TYPE.SELL, ITEM4, 40, -1);
        v2.addOffer(OFFER_TYPE.SELL, ITEM1, 150, 5);
        v2.addOffer(OFFER_TYPE.SELL, ITEM3, 320, 5);
        v3.addOffer(OFFER_TYPE.SELL, ITEM3, 390, 5);
        v2.addOffer(OFFER_TYPE.BUY, ITEM2, 300, -1);
        v2.addOffer(OFFER_TYPE.BUY, ITEM4, 50, -1);
        v3.addOffer(OFFER_TYPE.BUY, ITEM1, 200, -1);
        v4.addOffer(OFFER_TYPE.BUY, ITEM3, 450, -1);

        Route route = new Route(new RouteEntry(v1, 0, 0,0));
        route.add(new RouteEntry(v2, 0, 0,0));
        route.add(new RouteEntry(v3, 0, 0,0));
        route.add(new RouteEntry(v4, 0, 0,0));

        return route;
    }


    @Test
    public void testPathRoute5() throws Exception {
        LOG.info("Start route test 5");
        Route route = initTest5();
        RouteFiller filler = getFillerInstance(500, 5, true, market);
        filler.fill(route);

        assertEquals(620, route.getProfit(), 0.0001);
        assertEquals(2, route.getLands());

        List<RouteEntry> entries = route.getEntries();
        Order order1 = new Order(v1.getSell(ITEM1), v3.getBuy(ITEM1), 5);
        Order order7 = new Order(v3.getSell(ITEM3), v4.getBuy(ITEM3), 2);

        RouteEntry entry = entries.get(0);
        assertEquals(500, entry.getProfit(), 0.0001);
        TestUtil.assertCollectionEquals(entry.getOrders(), order1);

        entry = entries.get(1);
        assertEquals(0, entry.getProfit(), 0.0001);
        assertTrue(entry.getOrders().isEmpty());

        entry = entries.get(2);
        assertEquals(120, entry.getProfit(), 0.0001);
        TestUtil.assertCollectionEquals(entry.getOrders(), order7);
    }

    @Test
    public void testPathRoute5B() throws Exception {
        LOG.info("Start route test 5B");
        Route route = initTest5();
        RouteFiller filler = getFillerInstance(700, 7, true, market);
        filler.fill(route);

        assertEquals(750, route.getProfit(), 0.0001);
        assertEquals(3, route.getLands());

        List<RouteEntry> entries = route.getEntries();
        Order order2 = new Order(v1.getSell(ITEM2), v2.getBuy(ITEM2), 3);
        Order order3 = new Order(v1.getSell(ITEM4), v2.getBuy(ITEM4), 2);
        Order order4 = new Order(v2.getSell(ITEM1), v3.getBuy(ITEM1), 5);
        Order order7 = new Order(v3.getSell(ITEM3), v4.getBuy(ITEM3), 3);

        RouteEntry entry = entries.get(0);
        assertEquals(320, entry.getProfit(), 0.0001);
        TestUtil.assertCollectionEquals(entry.getOrders(), order2, order3);

        entry = entries.get(1);
        assertEquals(250, entry.getProfit(), 0.0001);
        TestUtil.assertCollectionEquals(entry.getOrders(), order4);

        entry = entries.get(2);
        assertEquals(180, entry.getProfit(), 0.0001);
        TestUtil.assertCollectionEquals(entry.getOrders(), order7);
    }

    private Route initTest6A(){
        LOG.info("Init test 5");
        market = new SimpleMarket();
        ITEM1 = market.addItem("ITEM1", null);
        ITEM2 = market.addItem("ITEM2", null);
        ITEM3 = market.addItem("ITEM3", null);
        ITEM4 = market.addItem("ITEM4", null);
        v1 = market.addPlace("p1",0,0,0).addVendor("v1");
        v2 = market.addPlace("p2",0,1,0).addVendor("v2");
        v1.addOffer(OFFER_TYPE.SELL, ITEM1, 100, -1);
        v1.addOffer(OFFER_TYPE.SELL, ITEM2, 200, -1);
        v1.addOffer(OFFER_TYPE.SELL, ITEM3, 300, -1);
        v2.addOffer(OFFER_TYPE.SELL, ITEM1, 150, -1);
        v2.addOffer(OFFER_TYPE.SELL, ITEM3, 320, -1);

        v2.addOffer(OFFER_TYPE.BUY, ITEM2, 225, -1);

        Route route = new Route(new RouteEntry(v1, 0, 0,0));
        route.add(new RouteEntry(v2, 0, 0,0));


        return route;
    }

    private Route initTest6B(){
        LOG.info("Init test 6B");
        v3 = market.addPlace("p3",0,1,1).addVendor("v3");
        v4 = market.addPlace("p4",1,1,1).addVendor("v4");

        v3.addOffer(OFFER_TYPE.SELL, ITEM3, 390, -1);

        v3.addOffer(OFFER_TYPE.BUY, ITEM1, 200, -1);
        v4.addOffer(OFFER_TYPE.BUY, ITEM3, 450, -1);

        Route route = new Route(new RouteEntry(v2, 0, 0,0));
        route.add(new RouteEntry(v3, 0, 0,0));
        route.add(new RouteEntry(v4, 0, 0,0));

        return route;
    }


    @Test
    public void testJoinRoute() throws Exception {
        LOG.info("Start join route test");
        Route route = initTest6A();
        Route routeB = initTest6B();
        RouteFiller filler = getFillerInstance(500, 5, true, market);
        filler.fill(route);
        filler.fill(routeB);

        route.join(routeB);
        filler.fill(route);

        assertEquals(620, route.getProfit(), 0.0001);
        assertEquals(2, route.getLands());
        assertEquals(3, route.getDistance(), 0.0001);

        List<RouteEntry> entries = route.getEntries();
        Order order1 = new Order(v1.getSell(ITEM1), v3.getBuy(ITEM1), 5);
        Order order2 = new Order(v1.getSell(ITEM2), v2.getBuy(ITEM2), 2);
        Order order3 = new Order(v1.getSell(ITEM3), v4.getBuy(ITEM3), 1);
        Order order4 = new Order(v2.getSell(ITEM1), v3.getBuy(ITEM1), 3);
        Order order5 = new Order(v2.getSell(ITEM3), v4.getBuy(ITEM3), 1);
        Order order7 = new Order(v3.getSell(ITEM3), v4.getBuy(ITEM3), 2);

        RouteEntry entry = entries.get(0);
        assertEquals(500, entry.getProfit(), 0.0001);
        TestUtil.assertCollectionEquals(entry.getOrders(), order1);

        entry = entries.get(1);
        assertEquals(0, entry.getProfit(), 0.0001);
        assertTrue(entry.getOrders().isEmpty());

        entry = entries.get(2);
        assertEquals(120, entry.getProfit(), 0.0001);
        TestUtil.assertCollectionEquals(entry.getOrders(), order7);

    }

    @Test
    public void testJoinRouteNoSort() throws Exception {
        LOG.info("Start join route test");
        Route route = initTest6A();
        Route routeB = initTest6B();
        RouteFiller filler = getFillerInstance(500, 5, true, market);
        filler.fill(route);
        filler = getFillerInstance(550, 5, true, market);
        filler.fill(routeB);

        route.join(routeB);

        assertEquals(260, route.getProfit(), 0.0001);
        assertEquals(3, route.getLands());
        assertEquals(3, route.getDistance(), 0.0001);

        List<RouteEntry> entries = route.getEntries();
        Order order1 = new Order(v1.getSell(ITEM1), v3.getBuy(ITEM1), 5);
        Order order2 = new Order(v1.getSell(ITEM2), v2.getBuy(ITEM2), 2);
        Order order3 = new Order(v1.getSell(ITEM3), v4.getBuy(ITEM3), 1);
        Order order4 = new Order(v2.getSell(ITEM1), v3.getBuy(ITEM1), 3);
        Order order5 = new Order(v2.getSell(ITEM3), v4.getBuy(ITEM3), 1);
        Order order7 = new Order(v3.getSell(ITEM3), v4.getBuy(ITEM3), 1);


        RouteEntry entry = entries.get(0);
        assertEquals(50, entry.getProfit(), 0.0001);
        TestUtil.assertCollectionEquals(entry.getOrders(), order2);

        entry = entries.get(1);
        assertEquals(150, entry.getProfit(), 0.0001);
        TestUtil.assertCollectionEquals(entry.getOrders(), order4);

        entry = entries.get(2);
        assertEquals(60, entry.getProfit(), 0.0001);
        TestUtil.assertCollectionEquals(entry.getOrders(), order7);

    }

    @Test
    public void testLostProfits() throws Exception {
        LOG.info("Start lost profit test");
        Route route = initTest5();
        int cargo = 7;
        RouteFiller filler = getFillerInstance(700, cargo, true, market);
        filler.fill(route);

        assertEquals(750, route.getProfit(), 0.0001);
        assertEquals(3, route.getLands());

        /* v1 3x100 + 2x20 -> v2 5x50 -> v3 3x60 -> v4 */
        double[] profits = RouteFiller.getLostProfits(route, 0, v4, cargo, cargo);
        assertArrayEquals(new double[]{750, 430, 180, 0}, profits, 0.001);

        profits = RouteFiller.getLostProfits(route, 0, v1, cargo, cargo);
        assertArrayEquals(new double[]{0, 430, 180, 0}, profits, 0.001);

        profits = RouteFiller.getLostProfits(route, 1, v2, cargo, cargo);
        assertArrayEquals(new double[]{320, 0, 500, 320}, profits, 0.001);

        profits = RouteFiller.getLostProfits(route, 2, v3, cargo, cargo);
        assertArrayEquals(new double[]{570, 250, 0, 570}, profits, 0.001);

        profits = RouteFiller.getLostProfits(route, 0, v3, cargo, cargo);
        assertArrayEquals(new double[]{570, 250, 0, 570}, profits, 0.001);

        profits = RouteFiller.getLostProfits(route, 0, v1, 2, cargo);
        assertArrayEquals(new double[]{0, 0, 0, 0}, profits, 0.001);

        /* v1 -2x20 -> v2 -2x50 -> v3 -> v4 */
        profits = RouteFiller.getLostProfits(route, 0, v4, 4, cargo);
        assertArrayEquals(new double[]{120, 100, 0, 0}, profits, 0.001);

        /* v1 -1x100 - 2x20 -> v2 -3x50 -> v3 -1x60 -> v4 */
        profits = RouteFiller.getLostProfits(route, 0, v4, 5, cargo);
        assertArrayEquals(new double[]{330, 210, 60, 0}, profits, 0.001);
    }

    private Route initTest5c(){
        LOG.info("Init test 5c");
        market = new SimpleMarket();
        ITEM1 = market.addItem("ITEM1", null);
        ITEM2 = market.addItem("ITEM2", null);
        ITEM3 = market.addItem("ITEM3", null);
        ITEM4 = market.addItem("ITEM4", null);
        v1 = market.addPlace("p1",0,0,0).addVendor("v1");
        v2 = market.addPlace("p2",0,0,0).addVendor("v2");
        v3 = market.addPlace("p3",0,0,0).addVendor("v3");
        v4 = market.addPlace("p4",0,0,0).addVendor("v4");
        v1.addOffer(OFFER_TYPE.SELL, ITEM1, 100, 5);
        v1.addOffer(OFFER_TYPE.SELL, ITEM2, 200, 5);
        v1.addOffer(OFFER_TYPE.SELL, ITEM3, 300, 5);
        v1.addOffer(OFFER_TYPE.SELL, ITEM4, 40, -1);
        v2.addOffer(OFFER_TYPE.SELL, ITEM1, 150, 5);
        v2.addOffer(OFFER_TYPE.SELL, ITEM3, 320, 5);
        v3.addOffer(OFFER_TYPE.SELL, ITEM3, 390, 5);
        v4.addOffer(OFFER_TYPE.SELL, ITEM3, 200, 5);

        v1.addOffer(OFFER_TYPE.BUY, ITEM3, 250, -1);
        v2.addOffer(OFFER_TYPE.BUY, ITEM2, 300, -1);
        v2.addOffer(OFFER_TYPE.BUY, ITEM4, 50, -1);
        v3.addOffer(OFFER_TYPE.BUY, ITEM1, 200, -1);
        v4.addOffer(OFFER_TYPE.BUY, ITEM3, 450, -1);

        Route route = new Route(new RouteEntry(v1, 0, 0,0));
        route.add(new RouteEntry(v2, 0, 0,0));
        route.add(new RouteEntry(v3, 0, 0,0));
        route.add(new RouteEntry(v4, 0, 0,0));
        route.add(new RouteEntry(v3, 0, 0,0));
        route.add(new RouteEntry(v1, 0, 0,0));

        return route;
    }

    @Test
    public void testProfitsByLoop() throws Exception {
        LOG.info("Start profit by loop test");
        Route route = initTest5c();
        int cargo = 7;
        RouteFiller filler = getFillerInstance(700, cargo, true, market);
        filler.fill(route);

        assertEquals(1000, route.getProfit(), 0.0001);
        assertEquals(4, route.getLands());

        /* v1 3x100 + 2x20 -> v2 5x50 -> v3 3x60 -> v4 5x50 -> v3 transit -> v1 */
        double[] profits = RouteFiller.getLostProfits(route, 0, v4, cargo, cargo);
        assertArrayEquals(new double[]{750, 430, 180, 0, 750}, profits, 0.001);

        profits = RouteFiller.getLostProfits(route, 0, v1, cargo, cargo);
        assertArrayEquals(new double[]{0, 680, 430, 250, 0}, profits, 0.001);

        profits = RouteFiller.getLostProfits(route, 1, v2, cargo, cargo);
        assertArrayEquals(new double[]{320, 0, 750, 570, 320}, profits, 0.001);

        profits = RouteFiller.getLostProfits(route, 2, v3, cargo, cargo);
        assertArrayEquals(new double[]{570, 250, 0, 820, 570}, profits, 0.001);

        profits = RouteFiller.getLostProfits(route, 0, v3, cargo, cargo);
        assertArrayEquals(new double[]{570, 250, 0, 820, 570}, profits, 0.001);

        profits = RouteFiller.getLostProfits(route, 0, v1, 2, cargo);
        assertArrayEquals(new double[]{0, 0, 0, 0, 0}, profits, 0.001);

    }

    private Route initTest5d(){
        LOG.info("Init test 5d");
        market = new SimpleMarket();
        ITEM1 = market.addItem("ITEM1", null);
        ITEM2 = market.addItem("ITEM2", null);
        ITEM3 = market.addItem("ITEM3", null);
        ITEM4 = market.addItem("ITEM4", null);
        v1 = market.addPlace("p1",0,0,0).addVendor("v1");
        v2 = market.addPlace("p2",0,0,0).addVendor("v2");
        v3 = market.addPlace("p3",0,0,0).addVendor("v3");
        v4 = market.addPlace("p4",0,0,0).addVendor("v4");
        v1.addOffer(OFFER_TYPE.SELL, ITEM1, 100, 5);
        v1.addOffer(OFFER_TYPE.SELL, ITEM2, 200, 5);
        v1.addOffer(OFFER_TYPE.SELL, ITEM3, 300, 5);
        v1.addOffer(OFFER_TYPE.SELL, ITEM4, 40, -1);
        v2.addOffer(OFFER_TYPE.SELL, ITEM1, 150, 5);
        v2.addOffer(OFFER_TYPE.SELL, ITEM3, 320, 5);
        v3.addOffer(OFFER_TYPE.SELL, ITEM3, 390, 5);
        v4.addOffer(OFFER_TYPE.SELL, ITEM1, 180, 5);

        v1.addOffer(OFFER_TYPE.BUY, ITEM3, 400, -1);
        v2.addOffer(OFFER_TYPE.BUY, ITEM2, 300, -1);
        v2.addOffer(OFFER_TYPE.BUY, ITEM4, 50, -1);
        v3.addOffer(OFFER_TYPE.BUY, ITEM1, 200, -1);
        v4.addOffer(OFFER_TYPE.BUY, ITEM3, 450, -1);

        Route route = new Route(new RouteEntry(v1, 0, 0,0));
        route.add(new RouteEntry(v2, 0, 0,0));
        route.add(new RouteEntry(v3, 0, 0,0));
        route.add(new RouteEntry(v4, 0, 0,0));
        route.add(new RouteEntry(v3, 0, 0,0));
        route.add(new RouteEntry(v1, 0, 0,0));

        return route;
    }

    @Test
    public void testProfitsByLoop2() throws Exception {
        LOG.info("Start profit by loop2 test");
        Route route = initTest5d();
        int cargo = 7;
        RouteFiller filler = getFillerInstance(700, cargo, true, market);
        filler.fill(route);

        assertEquals(880, route.getProfit(), 0.0001);
        assertEquals(5, route.getLands());

        /* v1 3x100 + 2x20 -> v2 5x50 -> v3 3x60 -> v4 5x20 -> v3 3x10 -> v1 */
        double[] profits = RouteFiller.getLostProfits(route, 0, v4, cargo, cargo);
        assertArrayEquals(new double[]{750, 430, 180, 0, 780}, profits, 0.001);

        profits = RouteFiller.getLostProfits(route, 0, v1, cargo, cargo);
        assertArrayEquals(new double[]{0, 560, 310, 130, 30}, profits, 0.001);

        profits = RouteFiller.getLostProfits(route, 1, v2, cargo, cargo);
        assertArrayEquals(new double[]{320, 0, 630, 450, 350}, profits, 0.001);

        profits = RouteFiller.getLostProfits(route, 2, v3, cargo, cargo);
        assertArrayEquals(new double[]{570, 250, 0, 100, 0}, profits, 0.001);

        profits = RouteFiller.getLostProfits(route, 0, v3, cargo, cargo);
        assertArrayEquals(new double[]{570, 250, 0, 100, 0}, profits, 0.001);

        profits = RouteFiller.getLostProfits(route, 0, v1, 2, cargo);
        assertArrayEquals(new double[]{0, 0, 0, 0, 0}, profits, 0.001);

    }

    @Test
    public void testAddOffer() throws Exception {
        LOG.info("Start add offer test");
        Route route = initTest5d();
        int cargo = 7;
        RouteFiller filler = getFillerInstance(700, cargo, true, market);
        filler.fill(route);

        assertEquals(880, route.getProfit(), 0.0001);
        assertEquals(5, route.getLands());

        /* v1 3x100 + 2x20 -> v2 5x50 -> v3 3x60 -> v4 5x20 -> v3 3x10 -> v1 */
        Offer offer = SimpleOffer.fakeBuy(v2, ITEM3, 210, 3);
        RouteFiller.addOrders(route, 1, offer, cargo);

        Order order1 = new Order(v1.getSell(ITEM2), v2.getBuy(ITEM2), 3);
        Order order2 = new Order(v1.getSell(ITEM4), v2.getBuy(ITEM4), 1);
        Order order3 = new Order(v2.getSell(ITEM1), v3.getBuy(ITEM1), 5);
        Order order4 = new Order(v3.getSell(ITEM3), v4.getBuy(ITEM3), 3);
        Order order5 = new Order(v4.getSell(ITEM1), v3.getBuy(ITEM1), 5);
        Order order6 = new Order(v3.getSell(ITEM3), v1.getBuy(ITEM3), 3);
        Order order7 = new Order(v1.getSell(ITEM3), offer, 2);
        Order order8 = new Order(v3.getSell(ITEM3), offer, 1);

        TestUtil.assertCollectionEquals(route.get(0).getOrders(), order1, order2, order7);
        TestUtil.assertCollectionEquals(route.get(1).getOrders(), order3);
        TestUtil.assertCollectionEquals(route.get(2).getOrders(), order4);
        TestUtil.assertCollectionEquals(route.get(3).getOrders(), order5);
        TestUtil.assertCollectionEquals(route.get(4).getOrders(), order6, order8);

        order2 = new Order(v1.getSell(ITEM4), v2.getBuy(ITEM4), 2);

        RouteFiller.removeOrders(route, offer);
        TestUtil.assertCollectionEquals(route.get(0).getOrders(), order1, order2);
        TestUtil.assertCollectionEquals(route.get(1).getOrders(), order3);
        TestUtil.assertCollectionEquals(route.get(2).getOrders(), order4);
        TestUtil.assertCollectionEquals(route.get(3).getOrders(), order5);
        TestUtil.assertCollectionEquals(route.get(4).getOrders(), order6);
    }

}
