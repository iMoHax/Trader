package ru.trader.graph;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.core.*;

import java.util.ArrayList;

public class RouteGraphTest extends Assert {
    private final static Logger LOG = LoggerFactory.getLogger(RouteGraphTest.class);
    private final static Market market = new SimpleMarket();
    private final static Item ITEM1 = new Item("ITEM1");
    private final static Item ITEM2 = new Item("ITEM2");
    private final static Item ITEM3 = new Item("ITEM3");
    private static Vendor v1;
    private static Vendor v2;
    private static Vendor v3;
    private static Vendor v4;

    static {
        v1 = new SimpleVendor("v1");
        v2 = new SimpleVendor("v2");
        v3 = new SimpleVendor("v3");
        v4 = new SimpleVendor("v4");

        v1.add(new Offer(OFFER_TYPE.SELL, ITEM1, 100));
        v1.add(new Offer(OFFER_TYPE.SELL, ITEM2, 200));
        v1.add(new Offer(OFFER_TYPE.SELL, ITEM3, 300));
        v2.add(new Offer(OFFER_TYPE.SELL, ITEM1, 150));
        v2.add(new Offer(OFFER_TYPE.SELL, ITEM3, 320));
        v3.add(new Offer(OFFER_TYPE.SELL, ITEM3, 390));

        v2.add(new Offer(OFFER_TYPE.BUY, ITEM2, 225));
        v3.add(new Offer(OFFER_TYPE.BUY, ITEM1, 200));
        v4.add(new Offer(OFFER_TYPE.BUY, ITEM3, 450));

        market.add(v1);market.add(v2);market.add(v3);market.add(v4);
    }

    @Test
    public void testRoutes() throws Exception {
        RouteGraph graph = new RouteGraph(v1, market.get(), 1, 1, true, 4);
        graph.setBalance(500);
        graph.setLimit(5);
        //Profit: 150 180 200   230  670   620  950    890   620   950 1015   1180   890   950   930
        //Landings: 1  2   3     4    4     2    3      3     2     3    4      4     3     3     4
        //Prof:   150  90 66.66 57.5 167.5 310 316.66 296.66 310 316.66 253.75 295 296.66 316.66 232.5
        ArrayList<Path<Vendor>> routes = (ArrayList<Path<Vendor>>) graph.getPathsTo(v4, 5);
        assertEquals(5, routes.size());

        PathRoute path = (PathRoute) routes.get(0).getRoot();
        assertEquals(950, path.getProfit(), 0.001);
        assertEquals(3, path.getLandsCount());

        path = (PathRoute) routes.get(1).getRoot();
        assertEquals(950, path.getProfit(), 0.001);
        assertEquals(3, path.getLandsCount());

        path = (PathRoute) routes.get(2).getRoot();
        assertEquals(950, path.getProfit(), 0.001);
        assertEquals(3, path.getLandsCount());

        path = (PathRoute) routes.get(3).getRoot();
        assertEquals(620, path.getProfit(), 0.001);
        assertEquals(2, path.getLandsCount());

        path = (PathRoute) routes.get(4).getRoot();
        assertEquals(620, path.getProfit(), 0.001);
        assertEquals(2, path.getLandsCount());
    }
}
