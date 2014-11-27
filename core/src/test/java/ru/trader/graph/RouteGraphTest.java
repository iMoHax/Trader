package ru.trader.graph;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.core.*;
import ru.trader.store.simple.*;

import java.util.ArrayList;

public class RouteGraphTest extends Assert {
    private final static Logger LOG = LoggerFactory.getLogger(RouteGraphTest.class);
    private final static Market market = new SimpleMarket();
    private final static Item ITEM1 = new SimpleItem("ITEM1");
    private final static Item ITEM2 = new SimpleItem("ITEM2");
    private final static Item ITEM3 = new SimpleItem("ITEM3");
    private static Vendor v1;
    private static Vendor v2;
    private static Vendor v3;
    private static Vendor v4;
    private static Place p1;
    private static Place p2;
    private static Place p3;
    private static Place p4;


    static {
        p1 = new SimplePlace("v1");
        p2 = new SimplePlace("v2");
        p3 = new SimplePlace("v3");
        p4 = new SimplePlace("v4");

        v1 = new SimpleVendor("v1");
        v2 = new SimpleVendor("v2");
        v3 = new SimpleVendor("v3");
        v4 = new SimpleVendor("v4");

        v1.add(new SimpleOffer(OFFER_TYPE.SELL, ITEM1, 100, -1));
        v1.add(new SimpleOffer(OFFER_TYPE.SELL, ITEM2, 200, -1));
        v1.add(new SimpleOffer(OFFER_TYPE.SELL, ITEM3, 300, -1));
        v2.add(new SimpleOffer(OFFER_TYPE.SELL, ITEM1, 150, -1));
        v2.add(new SimpleOffer(OFFER_TYPE.SELL, ITEM3, 320, -1));
        v3.add(new SimpleOffer(OFFER_TYPE.SELL, ITEM3, 390, -1));

        v2.add(new SimpleOffer(OFFER_TYPE.BUY, ITEM2, 225, -1));
        v3.add(new SimpleOffer(OFFER_TYPE.BUY, ITEM1, 200, -1));
        v4.add(new SimpleOffer(OFFER_TYPE.BUY, ITEM3, 450, -1));

        p1.add(v1);p2.add(v2);p3.add(v3);p4.add(v4);
        market.add(p1);market.add(p2);market.add(p3);market.add(p4);
    }

    @Test
    public void testRoutes() throws Exception {
        RouteGraph graph = new RouteGraph(p1, market.get(), 1, 1, true, 4);
        graph.setBalance(500);
        graph.setCargo(5);
        //Profit: 150 180 200   230  670   620  950    890   620   950 1015   1180   890   950   930
        //Landings: 1  2   3     4    4     2    3      3     2     3    4      4     3     3     4
        //Prof:   150  90 66.66 57.5 167.5 310 316.66 296.66 310 316.66 253.75 295 296.66 316.66 232.5
        ArrayList<Path<Place>> routes = (ArrayList<Path<Place>>) graph.getPathsTo(p4, 5);
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
