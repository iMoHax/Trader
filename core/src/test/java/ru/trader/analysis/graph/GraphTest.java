package ru.trader.analysis.graph;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.core.Profile;
import ru.trader.core.Ship;

import java.util.ArrayList;

public class GraphTest extends Assert {
    private final static Logger LOG = LoggerFactory.getLogger(GraphTest.class);

    private final static ArrayList<Point> entrys = new ArrayList<>();
    private final static Point x1 = new Point("x1",-40);
    private final static Point x2 = new Point("x2",-20);
    private final static Point x3 = new Point("x3",-10, true);
    private final static Point x4 = new Point("x4",-5, true);
    private final static Point x5 = new Point("x5",0);
    private final static Point x6 = new Point("x6",5);
    private final static Point x7 = new Point("x7",20);
    private final static Point x8 = new Point("x8",30);
    private final static Point x9 = new Point("x9",40);
    private final static Point x10 = new Point("x10",50);

    @Before
    public void setUp() throws Exception {
        entrys.add(x1);
        entrys.add(x2);
        entrys.add(x3);
        entrys.add(x4);
        entrys.add(x5);
        entrys.add(x6);
        entrys.add(x7);
        entrys.add(x8);
        entrys.add(x9);
        entrys.add(x10);
    }

    @Test
    public void testBuild0() throws Exception {
        LOG.info("Start graph build test0");
        //max distance 4.95, 1 jump tank
        Ship ship = new Ship();
        ship.setMass(67);ship.setTank(0.6);
        Profile profile = new Profile(ship);
        profile.setJumps(10);
        profile.setRefill(false);
        LOG.info("Ship = {}, Jumps = {}", profile.getShip(), profile.getJumps());
        ConnectibleGraph<Point> graph = new ConnectibleGraph<>(profile);
        graph.build(x5, entrys);
        // x5
        assertFalse(graph.isAccessible(x1));
        assertFalse(graph.isAccessible(x2));
        assertFalse(graph.isAccessible(x3));
        assertFalse(graph.isAccessible(x4));
        assertTrue(graph.isAccessible(x5));
        assertFalse(graph.isAccessible(x6));
        assertFalse(graph.isAccessible(x7));
        assertFalse(graph.isAccessible(x8));
        assertFalse(graph.isAccessible(x9));
        assertFalse(graph.isAccessible(x10));
    }


    @Test
    public void testBuild1() throws Exception {
        LOG.info("Start graph build test1");
        //max distance 5.167, 1 jump tank
        Ship ship = new Ship();
        ship.setMass(64);ship.setTank(0.6);
        Profile profile = new Profile(ship);
        profile.setJumps(2);
        LOG.info("Ship = {}, Jumps = {}", profile.getShip(), profile.getJumps());
        ConnectibleGraph<Point> graph = new ConnectibleGraph<>(profile);
        graph.build(x5, entrys);
        // x5 <-> x4 <-refill-> x3, x5 -> x6
        assertFalse(graph.isAccessible(x1));
        assertFalse(graph.isAccessible(x2));
        assertTrue(graph.isAccessible(x3));
        assertTrue(graph.isAccessible(x4));
        assertTrue(graph.isAccessible(x5));
        assertTrue(graph.isAccessible(x6));
        assertFalse(graph.isAccessible(x7));
        assertFalse(graph.isAccessible(x8));
        assertFalse(graph.isAccessible(x9));
        assertFalse(graph.isAccessible(x10));

        Vertex<Point> x = graph.getVertex(x5);
        // x5 -> x4, x5 -> x6
        checkEdges(x, new Point[]{x4, x6}, new Point[]{x1, x2, x3, x7, x8, x9, x10});
        // x4 -> x5
        x = graph.getVertex(x4);
        checkEdges(x, new Point[]{x5, x3}, new Point[]{x1, x2, x6, x7, x8, x9, x10});
        // x6 <- x5
        x = graph.getVertex(x6);
        checkEdges(x, new Point[]{}, new Point[]{x1, x2, x3, x4, x5, x7, x8, x9, x10});

    }

    private void checkEdges(Vertex<Point> vertex, Point[] trueEdge, Point[] falseEdge){
        for (Point point : trueEdge) {
            assertTrue(String.format("%s must have edge to %s", vertex, point), vertex.isConnected(point));
        }
        for (Point point : falseEdge) {
            assertFalse(String.format("%s must not have edge to %s", vertex, point), vertex.isConnected(point));
        }
    }

    @Test
    public void testBuild2() throws Exception {
        LOG.info("Start graph build test2");
        //max distance 5.167, 1 jump tank
        Ship ship = new Ship();
        ship.setMass(64);ship.setTank(0.6);
        Profile profile = new Profile(ship);
        profile.setJumps(3);profile.setRefill(false);
        LOG.info("Ship = {}, Jumps = {}", profile.getShip(), profile.getJumps());
        ConnectibleGraph<Point> graph = new ConnectibleGraph<>(profile);
        graph.build(x5, entrys);
        // x5 <-> x4 <-> x3, x5 <-> x6
        assertFalse(graph.isAccessible(x1));
        assertFalse(graph.isAccessible(x2));
        assertTrue(graph.isAccessible(x3));
        assertTrue(graph.isAccessible(x4));
        assertTrue(graph.isAccessible(x5));
        assertTrue(graph.isAccessible(x6));
        assertFalse(graph.isAccessible(x7));
        assertFalse(graph.isAccessible(x8));
        assertFalse(graph.isAccessible(x9));
        assertFalse(graph.isAccessible(x10));

        Vertex<Point> x = graph.getVertex(x5);
        // x5 -> x4, x5 -> x6
        checkEdges(x, new Point[]{x4, x6}, new Point[]{x1, x2, x3, x7, x8, x9, x10});
        // x3 -> x4
        x = graph.getVertex(x3);
        checkEdges(x, new Point[]{x4}, new Point[]{x1, x2, x5, x6, x7, x8, x9, x10});
        // x4 -> x5, x4 -> x3
        x = graph.getVertex(x4);
        checkEdges(x, new Point[]{x3, x5}, new Point[]{x1, x2, x6, x7, x8, x9, x10});
        // x6 -> x5
        x = graph.getVertex(x6);
        checkEdges(x, new Point[]{x5}, new Point[]{x1, x2, x3, x4, x7, x8, x9, x10});
    }

    @Test
    public void testBuild4() throws Exception {
        LOG.info("Start graph build test4");
        //max distance 15.6, 1 jump tank
        Ship ship = new Ship();
        ship.setMass(18);ship.setTank(0.6);
        Profile profile = new Profile(ship);
        profile.setJumps(3); profile.setRefill(false);
        LOG.info("Ship = {}, Jumps = {}", profile.getShip(), profile.getJumps());
        ConnectibleGraph<Point> graph = new ConnectibleGraph<>(profile);
        graph.build(x5, entrys);
        //  x5 <-> x4 <-> x3 -> x2, x5 <-> x6 <-> x7 -> x8
        //  x5 <-> x3, x5 <-> x4 <-> x2,  x3 <-> x6, x4 <-> x6
        assertFalse(graph.isAccessible(x1));
        assertTrue(graph.isAccessible(x2));
        assertTrue(graph.isAccessible(x3));
        assertTrue(graph.isAccessible(x4));
        assertTrue(graph.isAccessible(x5));
        assertTrue(graph.isAccessible(x6));
        assertTrue(graph.isAccessible(x7));
        assertTrue(graph.isAccessible(x8));
        assertFalse(graph.isAccessible(x9));
        assertFalse(graph.isAccessible(x10));

        Vertex<Point> x = graph.getVertex(x5);
        // x5 -> x4, x5 -> x3, x5 -> x6
        checkEdges(x, new Point[]{x3, x4, x6}, new Point[]{x1, x2, x7, x8, x9, x10});
        // x2 -> x3, x2 -> x4
        x = graph.getVertex(x2);
        checkEdges(x, new Point[]{x3, x4}, new Point[]{x1, x5, x6, x7, x8, x9, x10});
        // x3 -> x4, x3 -> x2, x3 -> x5, x3 -> x6
        x = graph.getVertex(x3);
        checkEdges(x, new Point[]{x2, x4, x5, x6}, new Point[]{x1, x7, x8, x9, x10});
        // x4 -> x5, x4 -> x3, x4 -> x2, x4 -> x6
        x = graph.getVertex(x4);
        checkEdges(x, new Point[]{x2, x3, x5, x6}, new Point[]{x1, x7, x8, x9, x10});
        // x6 -> x5, x6 -> x7, x6 -> x3, x6 -> x4
        x = graph.getVertex(x6);
        checkEdges(x, new Point[]{x5, x7, x3, x4}, new Point[]{x1, x2, x8, x9, x10});
        // x7 -> x6, x7 -> x8
        x = graph.getVertex(x7);
        checkEdges(x, new Point[]{x6, x8}, new Point[]{x1, x2, x3, x4, x5, x9, x10});
        // x8 <- x7
        x = graph.getVertex(x8);
        checkEdges(x, new Point[]{}, new Point[]{x1, x2, x3, x4, x5, x6, x7, x9, x10});

    }

    @After
    public void tearDown() throws Exception {
        entrys.clear();
    }
}
