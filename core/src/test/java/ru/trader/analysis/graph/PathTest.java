package ru.trader.analysis.graph;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import ru.trader.analysis.AnalysisCallBack;
import ru.trader.analysis.TransitPath;
import ru.trader.core.*;
import ru.trader.store.simple.Store;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;

public class PathTest extends Assert {
    private final static Logger LOG = LoggerFactory.getLogger(PathTest.class);

    private final static Point x1 = new Point("x1",0);
    private final static Point x2 = new Point("x2",3);
    private final static Point x3 = new Point("x3",7);
    private final static Point x4 = new Point("x4",8);
    private final static Point x5 = new Point("x5",10);
    private final static Point x6 = new Point("x6",12);
    private final static Point x7 = new Point("x7",27);

    @Test
    public void testCreatePath1() throws Exception {
        LOG.info("Start create path test1");
        //max distance 5.167, 1 jump tank
        Ship ship = new Ship();
        ship.setMass(64);ship.setTank(0.6);

        Profile profile = new Profile(ship);
        TestGraph<Point> graph = new TestGraph<>(profile, new AnalysisCallBack());
        LOG.info("Ship = {}", profile.getShip());

        Collection<ConnectibleGraph<Point>.BuildEdge> edges = new ArrayList<>();
        ConnectibleGraph<Point>.BuildEdge edge1 = graph.createEdge(x1,x2);

        edges.add(edge1);
        Path<Point> path = new Path<>(edges);
        double lastAfterMin1 = path.getMinFuel() - ship.getFuelCost(path.getMinFuel(), edge1.getDistance());
        double lastAfterMax1 = path.getMaxFuel() - ship.getFuelCost(path.getMaxFuel(), edge1.getDistance());

        assertEquals(0.2, path.getMinFuel(),0.01);
        assertEquals(0.6, path.getMaxFuel(),0.01);
        assertEquals(0.2, path.getFuelCost(),0.01);
        assertEquals(0, path.getRefillCount());
        assertEquals(1, path.getSize());
        assertTrue(lastAfterMin1 >= 0);
        assertTrue(lastAfterMax1 >= 0);

        ConnectibleGraph<Point>.BuildEdge edge2 = graph.createEdge(x2,x3);

        edges.add(edge2);
        path = new Path<>(edges);

        lastAfterMin1 = path.getMinFuel() - ship.getFuelCost(path.getMinFuel(), edge1.getDistance());
        lastAfterMax1 = path.getMaxFuel() - ship.getFuelCost(path.getMaxFuel(), edge1.getDistance());
        double lastAfterMin2 = lastAfterMin1 - ship.getFuelCost(lastAfterMin1, edge2.getDistance());
        double lastAfterMax2 = lastAfterMax1 - ship.getFuelCost(lastAfterMax1, edge2.getDistance());

        assertTrue(edge1.getMinFuel() + edge2.getMinFuel() <= path.getMinFuel());
        assertEquals(0.6, path.getMaxFuel(),0.01);
        assertEquals(0.56, path.getFuelCost(),0.01);
        assertEquals(0, path.getRefillCount());
        assertEquals(2, path.getSize());
        assertTrue(lastAfterMin2 >= 0);
        assertTrue(lastAfterMax2 >= 0);

        ConnectibleGraph<Point>.BuildEdge edge3 = graph.createEdge(x3,x4);
        edges.add(edge3);
        path = new Path<>(edges);

        lastAfterMin1 = path.getMinFuel() - ship.getFuelCost(path.getMinFuel(), edge1.getDistance());
        lastAfterMax1 = path.getMaxFuel() - ship.getFuelCost(path.getMaxFuel(), edge1.getDistance());
        lastAfterMin2 = lastAfterMin1 - ship.getFuelCost(lastAfterMin1, edge2.getDistance());
        lastAfterMax2 = lastAfterMax1 - ship.getFuelCost(lastAfterMax1, edge2.getDistance());
        double lastAfterMin3 = lastAfterMin2 - ship.getFuelCost(lastAfterMin2, edge3.getDistance());
        double lastAfterMax3 = lastAfterMax2 - ship.getFuelCost(lastAfterMax2, edge3.getDistance());

        assertTrue(edge1.getMinFuel() + edge2.getMinFuel() + edge3.getMinFuel() <= path.getMinFuel());
        assertEquals(0.6, path.getMaxFuel(),0.01);
        assertEquals(0.58, path.getFuelCost(),0.01);
        assertEquals(0, path.getRefillCount());
        assertEquals(3, path.getSize());
        assertTrue(lastAfterMin3 >= 0);
        assertTrue(lastAfterMax3 >= 0);

    }

    private double getRemain(Collection<ConnectibleGraph<Vendor>.BuildEdge> edges, double fuel, double tank, int refill){
        boolean isFirst = true;
        for (ConnectibleGraph<Vendor>.BuildEdge edge : edges) {
            fuel -= edge.getFuelCost(fuel);
            if (refill == 1 && fuel <= 0 || refill == 2){
                if (!isFirst && edge.getSource().getEntry().canRefill()){
                    fuel = tank - edge.getFuelCost(tank);
                } else {
                    if (fuel <= 0) return fuel;
                }
            }
            isFirst = false;
        }
        return fuel;
    }

    @Test
    public void testTransitPath() throws IOException, SAXException, ParserConfigurationException {
        LOG.info("Start transit path test1");

        Ship ship = new Ship();
        ship.setCargo(24); ship.setEngine(2,'A');
        Profile profile = new Profile(ship);
        profile.setBalance(100000); profile.setJumps(6);
        LOG.info("Ship = {}", profile.getShip());
        TestGraph<Vendor> graph = new TestGraph<>(profile, new AnalysisCallBack());

        InputStream is = getClass().getResourceAsStream("/test3.xml");
        Market world = Store.loadFromFile(is);
        Vendor hallerPort = world.get("LHS 1541").get("Haller Port");
        Vendor transitWolf1325= world.get("Wolf 1325").asTransit();
        Vendor transitBhadaba = world.get("Bhadaba").asTransit();
        Vendor transitLHS21 = world.get("LHS 21").asTransit();
        Vendor transitD99 = world.get("Tascheter Sector CL-Y D99").asTransit();
        Vendor morganTerminal = world.get("Sui Xing").get("Morgan Terminal");

        Collection<ConnectibleGraph<Vendor>.BuildEdge> edges = new ArrayList<>();
        edges.add(graph.createEdge(hallerPort, transitWolf1325));
        edges.add(graph.createEdge(transitWolf1325, transitBhadaba));
        edges.add(graph.createEdge(transitBhadaba, transitLHS21));
        edges.add(graph.createEdge(transitLHS21, transitD99));
        edges.add(graph.createEdge(transitD99, morganTerminal));

        LOG.info("Test path with refill");

        double fuel = getRemain(edges, ship.getTank(), ship.getTank(), 0);
        double fuel2 = getRemain(edges, ship.getTank(), ship.getTank(), 2);
        double fuel3 = getRemain(edges, 0.64, ship.getTank(), 1);
        double fuel4 = getRemain(edges, 0.63, ship.getTank(), 1);

        assertTrue(fuel < 0);
        assertTrue(fuel2 > 0);
        assertTrue(fuel3 > 0);
        assertTrue(fuel4 < 0);

        Path<Vendor> path = new Path<>(edges);

        assertEquals(0.64, path.getMinFuel(),0.01);
        assertEquals(2, path.getMaxFuel(),0.01);
        assertEquals(2.03, path.getFuelCost(),0.01);
        assertEquals(1, path.getRefillCount());
        assertEquals(5, path.getSize());

        TransitPath transitPath = new TransitPath(path, ship.getTank());
        assertEquals(1, transitPath.getRefillCount());
        assertEquals(2.03, transitPath.getFuelCost(), 0.01);
        assertEquals(fuel2, transitPath.getRemain(), 0.01);

        transitPath = new TransitPath(path, 0.64);
        assertEquals(2, transitPath.getRefillCount());
        assertEquals(2.03, transitPath.getFuelCost(), 0.01);
        assertEquals(fuel2, transitPath.getRemain(), 0.01);


/*
        transitPath = new TransitPath(path, 0.64);
        assertEquals(1, transitPath.getRefillCount());
        assertEquals(2.01, transitPath.getFuelCost(), 0.01);
        assertEquals(fuel3, transitPath.getRemain(), 0.01);
        transitPath = new TransitPath(path, 0.62);
        assertEquals(2, transitPath.getRefillCount());
*/

        LOG.info("Test wrong path");

        Vendor quimperPort = world.get("LHS 21").get("Quimper Ring");
        Vendor transitWolf1278= world.get("Wolf 1278").asTransit();
        Vendor sarichPort = world.get("Lowne 1").get("Sarich Port");

        edges = new ArrayList<>();
        edges.add(graph.createEdge(quimperPort, transitBhadaba));
        edges.add(graph.createEdge(transitBhadaba, transitWolf1278));
        edges.add(graph.createEdge(transitWolf1278, sarichPort));

        fuel = getRemain(edges, ship.getTank(), ship.getTank(), 0);
        fuel2 = getRemain(edges, ship.getTank(), ship.getTank(), 2);
        fuel3 = getRemain(edges, 0.55, ship.getTank(), 1);
        fuel4 = getRemain(edges, 0.54, ship.getTank(), 1);

        assertTrue(fuel > 0);
        assertTrue(fuel2 > 0);
        assertTrue(fuel3 > 0);
        assertTrue(fuel4 < 0);

        path = new Path<>(edges);

        assertEquals(0.55, path.getMinFuel(),0.01);
        assertEquals(2, path.getMaxFuel(),0.01);
        assertEquals(1.85, path.getFuelCost(),0.01);
        assertEquals(0, path.getRefillCount());
        assertEquals(3, path.getSize());

        transitPath = new TransitPath(path, ship.getTank());
        assertEquals(0, transitPath.getRefillCount());
        assertEquals(1.85, transitPath.getFuelCost(), 0.01);
        assertEquals(fuel, transitPath.getRemain(), 0.01);

        transitPath = new TransitPath(path, 0.54);
        assertEquals(1, transitPath.getRefillCount());
        assertEquals(1.85, transitPath.getFuelCost(), 0.01);
        assertEquals(0.15, transitPath.getRemain(), 0.01);

        transitPath = new TransitPath(path, 0.55);
        assertEquals(1, transitPath.getRefillCount());
        assertEquals(1.85, transitPath.getFuelCost(), 0.01);
        assertEquals(0.15, transitPath.getRemain(), 0.01);

        /*
        "Transit Wolf 1278 - null(0.5300000082701445 - 2.0) -> Sarich Port"
        "Transit Bhadaba - null(0.7500000033527613 - 2.0) -> Transit Wolf 1278"
        "Quimper Ring - null(0.5500000078231096 - 2.0) -> Transit Bhadaba"

         */
    }

}
