package ru.trader.graph;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.TestUtil;
import ru.trader.core.*;
import ru.trader.store.simple.SimpleItem;
import ru.trader.store.simple.SimpleOffer;
import ru.trader.store.simple.SimpleVendor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class PathRouteTest extends Assert {
    private final static Logger LOG = LoggerFactory.getLogger(PathRouteTest.class);

    private final static Item ITEM1 = new SimpleItem("ITEM1");
    private final static Item ITEM2 = new SimpleItem("ITEM2");
    private final static Item ITEM3 = new SimpleItem("ITEM3");
    private static Vendor v1;
    private static Vendor v2;
    private static Vendor v3;
    private static Vendor v4;
    private static Vendor v5;

    private PathRoute initTest1(){
        LOG.info("Init test 1");
        v1 = new SimpleVendor("v1",0,0,0);
        v2 = new SimpleVendor("v2",0,0,0);

        v1.add(new SimpleOffer(OFFER_TYPE.SELL, ITEM1, 100, -1));
        v1.add(new SimpleOffer(OFFER_TYPE.SELL, ITEM2, 200, -1));
        v1.add(new SimpleOffer(OFFER_TYPE.SELL, ITEM3, 300, -1));
        v2.add(new SimpleOffer(OFFER_TYPE.BUY, ITEM1, 300, -1));
        v2.add(new SimpleOffer(OFFER_TYPE.BUY, ITEM2, 350, -1));
        v2.add(new SimpleOffer(OFFER_TYPE.BUY, ITEM3, 400, -1));

        PathRoute res = new PathRoute(new Vertex<>(v1));
        res = (PathRoute) res.connectTo(new Vertex<>(v2), false);
        res.finish();
        res.sort(10000, 5);
        return res.getRoot();
    }


    @Test
    public void testPathRoute1() throws Exception {
        LOG.info("Start path route test 1");
        PathRoute path = initTest1();

        assertEquals(1000, path.getProfit(), 0.0001);
        assertEquals(1, path.getLandsCount());

        path = path.getNext();
        Collection<Order> orders = path.getOrders();

        Order order1 = new Order(v1.getSell(ITEM1), v2.getBuy(ITEM1), 5);
        Order order2 = new Order(v1.getSell(ITEM2), v2.getBuy(ITEM2), 5);
        Order order3 = new Order(v1.getSell(ITEM3), v2.getBuy(ITEM3), 5);

        assertEquals(10000, path.getBalance(), 0.0001);
        assertEquals(1000, path.getProfit(), 0.0001);
        TestUtil.assertCollectionEquals(orders, order1, order2, order3, PathRoute.TRANSIT);
    }

    private PathRoute initTest2(){
        LOG.info("Init test 2");
        v1 = new SimpleVendor("v1",0,0,0);
        v2 = new SimpleVendor("v2",0,0,0);
        v3 = new SimpleVendor("v3",0,0,0);

        v1.add(new SimpleOffer(OFFER_TYPE.SELL, ITEM1, 100, -1));
        v1.add(new SimpleOffer(OFFER_TYPE.SELL, ITEM3, 300, -1));
        v2.add(new SimpleOffer(OFFER_TYPE.SELL, ITEM2, 200, -1));
        v3.add(new SimpleOffer(OFFER_TYPE.BUY, ITEM1, 300, -1));
        v3.add(new SimpleOffer(OFFER_TYPE.BUY, ITEM2, 350, -1));
        v3.add(new SimpleOffer(OFFER_TYPE.BUY, ITEM3, 400, -1));

        PathRoute res = new PathRoute(new Vertex<>(v1));
        res = (PathRoute) res.connectTo(new Vertex<>(v2), false);
        res = (PathRoute) res.connectTo(new Vertex<>(v3), false);
        res.finish();
        res.sort(10000, 5);
        return res.getRoot();
    }

    @Test
    public void testPathRoute2() throws Exception {
        LOG.info("Start path route test 2");
        PathRoute path = initTest2();
        assertEquals(1000, path.getProfit(), 0.0001);
        assertEquals(1, path.getLandsCount());

        path = path.getNext();
        Collection<Order> orders = path.getOrders();

        Order order1 = new Order(v1.getSell(ITEM1), v3.getBuy(ITEM1), 5);
        Order order2 = new Order(v2.getSell(ITEM2), v3.getBuy(ITEM2), 5);
        Order order3 = new Order(v1.getSell(ITEM3), v3.getBuy(ITEM3), 5);

        assertEquals(10000, path.getBalance(), 0.0001);
        assertEquals(1000, path.getProfit(), 0.0001);
        TestUtil.assertCollectionEquals(orders, order1,  PathRoute.TRANSIT, order3);

        path = path.getNext();
        orders = path.getOrders();

        assertEquals(10000, path.getBalance(), 0.0001);
        assertEquals(750, path.getProfit(), 0.0001);
        TestUtil.assertCollectionEquals(orders, order2, PathRoute.TRANSIT);
    }

    private PathRoute initTest3(){
        LOG.info("Init test 3");
        v1 = new SimpleVendor("v1",0,0,0);
        v2 = new SimpleVendor("v2",0,0,0);
        v3 = new SimpleVendor("v3",0,0,0);
        v4 = new SimpleVendor("v4",0,0,0);

        v1.add(new SimpleOffer(OFFER_TYPE.SELL, ITEM1, 100, -1));
        v1.add(new SimpleOffer(OFFER_TYPE.SELL, ITEM2, 200, -1));
        v1.add(new SimpleOffer(OFFER_TYPE.SELL, ITEM3, 300, -1));
        v2.add(new SimpleOffer(OFFER_TYPE.SELL, ITEM1, 150, -1));
        v2.add(new SimpleOffer(OFFER_TYPE.SELL, ITEM3, 320, -1));
        v3.add(new SimpleOffer(OFFER_TYPE.SELL, ITEM3, 390, -1));

        v2.add(new SimpleOffer(OFFER_TYPE.BUY, ITEM2, 225, -1));
        v3.add(new SimpleOffer(OFFER_TYPE.BUY, ITEM1, 200, -1));
        v4.add(new SimpleOffer(OFFER_TYPE.BUY, ITEM3, 450, -1));

        PathRoute res = new PathRoute(new Vertex<>(v1));
        res = (PathRoute) res.connectTo(new Vertex<>(v2), false);
        res = (PathRoute) res.connectTo(new Vertex<>(v3), false);
        res = (PathRoute) res.connectTo(new Vertex<>(v4), false);
        res.finish();
        res.sort(10000, 5);
        return res.getRoot();
    }

    @Test
    public void testPathRoute3() throws Exception {
        LOG.info("Start path route test 3");
        PathRoute path = initTest3();
        assertEquals(800, path.getProfit(), 0.0001);
        assertEquals(2, path.getLandsCount());

        path = path.getNext();
        Collection<Order> orders = path.getOrders();

        Order order1 = new Order(v1.getSell(ITEM1), v3.getBuy(ITEM1), 5);
        Order order2 = new Order(v1.getSell(ITEM2), v2.getBuy(ITEM2), 5);
        Order order3 = new Order(v1.getSell(ITEM3), v4.getBuy(ITEM3), 5);
        Order order4 = new Order(v2.getSell(ITEM1), v3.getBuy(ITEM1), 5);
        Order order5 = new Order(v2.getSell(ITEM3), v4.getBuy(ITEM3), 5);
        Order order7 = new Order(v3.getSell(ITEM3), v4.getBuy(ITEM3), 5);

        assertEquals(10000, path.getBalance(), 0.0001);
        assertEquals(800, path.getProfit(), 0.0001);
        TestUtil.assertCollectionEquals(orders, order1, order2, order3, PathRoute.TRANSIT);

        path = path.getNext();
        orders = path.getOrders();

        assertEquals(10125, path.getBalance(), 0.0001);
        assertEquals(650, path.getProfit(), 0.0001);
        TestUtil.assertCollectionEquals(orders, order5, order4, PathRoute.TRANSIT);

        path = path.getNext();
        orders = path.getOrders();

        assertEquals(10500, path.getBalance(), 0.0001);
        assertEquals(300, path.getProfit(), 0.0001);
        TestUtil.assertCollectionEquals(orders, order7, PathRoute.TRANSIT);
    }

    private PathRoute initTest4(){
        LOG.info("Init test 4");
        v1 = new SimpleVendor("v1",0,0,0);
        v2 = new SimpleVendor("v2",0,0,0);
        v3 = new SimpleVendor("v3",0,0,0);
        v4 = new SimpleVendor("v4",0,0,0);
        v5 = new SimpleVendor("v5",0,0,0);

        v1.add(new SimpleOffer(OFFER_TYPE.SELL, ITEM1, 410, -1));
        v1.add(new SimpleOffer(OFFER_TYPE.SELL, ITEM2, 200, -1));
        v1.add(new SimpleOffer(OFFER_TYPE.SELL, ITEM3, 300, -1));
        v2.add(new SimpleOffer(OFFER_TYPE.SELL, ITEM2, 270, -1));
        v4.add(new SimpleOffer(OFFER_TYPE.SELL, ITEM1, 300, -1));

        v2.add(new SimpleOffer(OFFER_TYPE.BUY, ITEM1, 470, -1));
        v3.add(new SimpleOffer(OFFER_TYPE.BUY, ITEM2, 300, -1));
        v4.add(new SimpleOffer(OFFER_TYPE.BUY, ITEM3, 370, -1));
        v5.add(new SimpleOffer(OFFER_TYPE.BUY, ITEM1, 400, -1));

        PathRoute res = new PathRoute(new Vertex<>(v1));
        res = (PathRoute) res.connectTo(new Vertex<>(v2), false);
        res = (PathRoute) res.connectTo(new Vertex<>(v3), false);
        res = (PathRoute) res.connectTo(new Vertex<>(v4), false);
        res = (PathRoute) res.connectTo(new Vertex<>(v5), false);
        res.finish();
        res.sort(10000, 5);
        return res.getRoot();
    }

    @Test
    public void testPathRoute4() throws Exception {
        LOG.info("Start path route test 4");
        PathRoute path = initTest4();
        assertEquals(1000, path.getProfit(), 0.0001);
        assertEquals(3, path.getLandsCount());

        path = path.getNext();
        Collection<Order> orders = path.getOrders();

        Order order1 = new Order(v1.getSell(ITEM1), v2.getBuy(ITEM1), 5);
        Order order2 = new Order(v1.getSell(ITEM1), v5.getBuy(ITEM1), 5);
        Order order3 = new Order(v1.getSell(ITEM2), v3.getBuy(ITEM2), 5);
        Order order4 = new Order(v1.getSell(ITEM3), v4.getBuy(ITEM3), 5);
        Order order5 = new Order(v2.getSell(ITEM2), v3.getBuy(ITEM2), 5);
        Order order6 = new Order(v4.getSell(ITEM1), v5.getBuy(ITEM1), 5);


        assertEquals(10000, path.getBalance(), 0.0001);
        assertEquals(1000, path.getProfit(), 0.0001);
        TestUtil.assertCollectionEquals(orders, order3, order1, order4, PathRoute.TRANSIT);

        path = path.getNext();
        orders = path.getOrders();

        assertEquals(10300, path.getBalance(), 0.0001);
        assertEquals(650, path.getProfit(), 0.0001);
        TestUtil.assertCollectionEquals(orders, order5, PathRoute.TRANSIT);

        path = path.getNext();
        orders = path.getOrders();

        assertEquals(10500, path.getBalance(), 0.0001);
        assertEquals(500, path.getProfit(), 0.0001);
        TestUtil.assertCollectionEquals(orders, PathRoute.TRANSIT);

        path = path.getNext();
        orders = path.getOrders();

        assertEquals(10500, path.getBalance(), 0.0001);
        assertEquals(500, path.getProfit(), 0.0001);
        TestUtil.assertCollectionEquals(orders, order6, PathRoute.TRANSIT);
    }

    private PathRoute initTest5(){
        LOG.info("Init test 5");
        v1 = new SimpleVendor("v1",0,0,0);
        v2 = new SimpleVendor("v2",0,0,0);
        v3 = new SimpleVendor("v3",0,0,0);
        v4 = new SimpleVendor("v4",0,0,0);

        v1.add(new SimpleOffer(OFFER_TYPE.SELL, ITEM1, 100, -1));
        v1.add(new SimpleOffer(OFFER_TYPE.SELL, ITEM2, 200, -1));
        v1.add(new SimpleOffer(OFFER_TYPE.SELL, ITEM3, 300, -1));
        v2.add(new SimpleOffer(OFFER_TYPE.SELL, ITEM1, 150, -1));
        v2.add(new SimpleOffer(OFFER_TYPE.SELL, ITEM3, 320, -1));
        v3.add(new SimpleOffer(OFFER_TYPE.SELL, ITEM3, 390, -1));

        v2.add(new SimpleOffer(OFFER_TYPE.BUY, ITEM2, 225, -1));
        v3.add(new SimpleOffer(OFFER_TYPE.BUY, ITEM1, 200, -1));
        v4.add(new SimpleOffer(OFFER_TYPE.BUY, ITEM3, 450, -1));

        PathRoute res = new PathRoute(new Vertex<>(v1));
        res = (PathRoute) res.connectTo(new Vertex<>(v2), false);
        res = (PathRoute) res.connectTo(new Vertex<>(v3), false);
        res = (PathRoute) res.connectTo(new Vertex<>(v4), false);
        res.finish();
        res.sort(500, 5);
        return res.getRoot();
    }


    @Test
    public void testPathRoute5() throws Exception {
        LOG.info("Start path route test 5");
        PathRoute path = initTest5();
        assertEquals(620, path.getProfit(), 0.0001);
        assertEquals(2, path.getLandsCount());

        path = path.getNext();
        Collection<Order> orders = path.getOrders();

        Order order1 = new Order(v1.getSell(ITEM1), v3.getBuy(ITEM1), 5);
        Order order2 = new Order(v1.getSell(ITEM2), v2.getBuy(ITEM2), 2);
        Order order3 = new Order(v1.getSell(ITEM3), v4.getBuy(ITEM3), 1);
        Order order4 = new Order(v2.getSell(ITEM1), v3.getBuy(ITEM1), 3);
        Order order5 = new Order(v2.getSell(ITEM3), v4.getBuy(ITEM3), 1);
        Order order7 = new Order(v3.getSell(ITEM3), v4.getBuy(ITEM3), 2);

        assertEquals(500, path.getBalance(), 0.0001);
        assertEquals(620, path.getProfit(), 0.0001);
        TestUtil.assertCollectionEquals(orders, order1, order2, PathRoute.TRANSIT, order3);

        path = path.getNext();
        orders = path.getOrders();

        assertEquals(550, path.getBalance(), 0.0001);
        assertEquals(270, path.getProfit(), 0.0001);
        TestUtil.assertCollectionEquals(orders, order4, order5, PathRoute.TRANSIT);

        path = path.getNext();
        orders = path.getOrders();

        assertEquals(1000, path.getBalance(), 0.0001);
        assertEquals(120, path.getProfit(), 0.0001);
        TestUtil.assertCollectionEquals(orders, order7, PathRoute.TRANSIT);

    }

    private PathRoute initTest6A(){
        LOG.info("Init test 6A");
        v1 = new SimpleVendor("v1",0,0,0);
        v2 = new SimpleVendor("v2",0,1,0);

        v1.add(new SimpleOffer(OFFER_TYPE.SELL, ITEM1, 100, -1));
        v1.add(new SimpleOffer(OFFER_TYPE.SELL, ITEM2, 200, -1));
        v1.add(new SimpleOffer(OFFER_TYPE.SELL, ITEM3, 300, -1));
        v2.add(new SimpleOffer(OFFER_TYPE.SELL, ITEM1, 150, -1));
        v2.add(new SimpleOffer(OFFER_TYPE.SELL, ITEM3, 320, -1));

        v2.add(new SimpleOffer(OFFER_TYPE.BUY, ITEM2, 225, -1));

        PathRoute res = new PathRoute(new Vertex<>(v1));
        res = (PathRoute) res.connectTo(new Vertex<>(v2), false);
        res.finish();
        res.sort(500, 5);
        return res.getRoot();
    }

    private PathRoute initTest6B(double balance){
        LOG.info("Init test 6B");
        v3 = new SimpleVendor("v3",0,1,1);
        v4 = new SimpleVendor("v4",1,1,1);

        v3.add(new SimpleOffer(OFFER_TYPE.SELL, ITEM3, 390, -1));

        v3.add(new SimpleOffer(OFFER_TYPE.BUY, ITEM1, 200, -1));
        v4.add(new SimpleOffer(OFFER_TYPE.BUY, ITEM3, 450, -1));

        PathRoute res = new PathRoute(new Vertex<>(v2));
        res = (PathRoute) res.connectTo(new Vertex<>(v3), false);
        res = (PathRoute) res.connectTo(new Vertex<>(v4), false);
        res.finish();
        res.sort(balance, 5);
        return res.getRoot();
    }


    @Test
    public void testAddPathRoute() throws Exception {
        LOG.info("Start add path route test");
        PathRoute path = initTest6A();
        PathRoute pathB = initTest6B(500);

        path.getEnd().add(pathB, false);
        path.sort(500, 5);
        path = path.getRoot();

        assertEquals(620, path.getProfit(), 0.0001);
        assertEquals(2, path.getLandsCount());
        assertEquals(3, path.getDistance(), 0.0001);

        path = path.getNext();
        Collection<Order> orders = path.getOrders();

        Order order1 = new Order(v1.getSell(ITEM1), v3.getBuy(ITEM1), 5);
        Order order2 = new Order(v1.getSell(ITEM2), v2.getBuy(ITEM2), 2);
        Order order3 = new Order(v1.getSell(ITEM3), v4.getBuy(ITEM3), 1);
        Order order4 = new Order(v2.getSell(ITEM1), v3.getBuy(ITEM1), 3);
        Order order5 = new Order(v2.getSell(ITEM3), v4.getBuy(ITEM3), 1);
        Order order7 = new Order(v3.getSell(ITEM3), v4.getBuy(ITEM3), 2);

        assertEquals(500, path.getBalance(), 0.0001);
        assertEquals(620, path.getProfit(), 0.0001);
        TestUtil.assertCollectionEquals(orders, order1, order2, PathRoute.TRANSIT, order3);

        path = path.getNext();
        orders = path.getOrders();

        assertEquals(550, path.getBalance(), 0.0001);
        assertEquals(270, path.getProfit(), 0.0001);
        TestUtil.assertCollectionEquals(orders, order4, order5, PathRoute.TRANSIT);

        path = path.getNext();
        orders = path.getOrders();

        assertEquals(1000, path.getBalance(), 0.0001);
        assertEquals(120, path.getProfit(), 0.0001);
        TestUtil.assertCollectionEquals(orders, order7, PathRoute.TRANSIT);

    }

    @Test
    public void testAddPathRouteNoSort() throws Exception {
        LOG.info("Start add path route test");
        PathRoute path = initTest6A();
        PathRoute pathB = initTest6B(550);

        path.getEnd().add(pathB, true);
        path = path.getRoot();

        assertEquals(260, path.getProfit(), 0.0001);
        assertEquals(3, path.getLandsCount());
        assertEquals(3, path.getDistance(), 0.0001);

        path = path.getNext();
        Collection<Order> orders = path.getOrders();

        Order order1 = new Order(v1.getSell(ITEM1), v3.getBuy(ITEM1), 5);
        Order order2 = new Order(v1.getSell(ITEM2), v2.getBuy(ITEM2), 2);
        Order order3 = new Order(v1.getSell(ITEM3), v4.getBuy(ITEM3), 1);
        Order order4 = new Order(v2.getSell(ITEM1), v3.getBuy(ITEM1), 3);
        Order order5 = new Order(v2.getSell(ITEM3), v4.getBuy(ITEM3), 1);
        Order order7 = new Order(v3.getSell(ITEM3), v4.getBuy(ITEM3), 1);

        assertEquals(500, path.getBalance(), 0.0001);
        assertEquals(260, path.getProfit(), 0.0001);
        TestUtil.assertCollectionEquals(orders, order2, PathRoute.TRANSIT);

        path = path.getNext();
        orders = path.getOrders();

        assertEquals(550, path.getBalance(), 0.0001);
        assertEquals(210, path.getProfit(), 0.0001);
        TestUtil.assertCollectionEquals(orders, order4, order5, PathRoute.TRANSIT);

        path = path.getNext();
        orders = path.getOrders();

        assertEquals(700, path.getBalance(), 0.0001);
        assertEquals(60, path.getProfit(), 0.0001);
        TestUtil.assertCollectionEquals(orders, order7, PathRoute.TRANSIT);

    }

    @Test
    public void testEntries() throws Exception {
        LOG.info("Start test get entries");
        v1 = new SimpleVendor("v1",0,0,0);
        v2 = new SimpleVendor("v2",0,0,0);
        v3 = new SimpleVendor("v3",0,0,0);
        v4 = new SimpleVendor("v4",0,0,0);

        PathRoute path = new PathRoute(new Vertex<>(v1));
        path = (PathRoute) path.connectTo(new Vertex<>(v2), false);
        path = (PathRoute) path.connectTo(new Vertex<>(v3), false);
        path.finish();
        TestUtil.assertCollectionContainAll(path.getEntries(), v1, v2, v3);
    }

    @Test
    public void testContains() throws Exception {
        LOG.info("Start test get entries");
        v1 = new SimpleVendor("v1",0,0,0);
        v2 = new SimpleVendor("v2",0,0,0);
        v3 = new SimpleVendor("v3",0,0,0);
        v4 = new SimpleVendor("v4",0,0,0);

        PathRoute path = new PathRoute(new Vertex<>(v1));
        path = (PathRoute) path.connectTo(new Vertex<>(v2), false);
        path = (PathRoute) path.connectTo(new Vertex<>(v3), false);
        path.finish();
        Collection<Vendor> vendors = new ArrayList<>();
        Collections.addAll(vendors, v1, v2, v3);
        assertTrue(path.contains(vendors));
        vendors.clear();
        Collections.addAll(vendors, v2);
        assertTrue(path.contains(vendors));
        vendors.clear();
        Collections.addAll(vendors, v4);
        assertFalse(path.contains(vendors));
        vendors.clear();
        Collections.addAll(vendors, v3, v2, v4, v1);
        assertFalse(path.contains(vendors));
        vendors.clear();
        Collections.addAll(vendors, v1, v2, v3, v4);
        assertFalse(path.contains(vendors));

    }

}