package ru.trader.analysis.graph;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import ru.trader.analysis.AnalysisCallBack;
import ru.trader.analysis.TransitPath;
import ru.trader.core.Market;
import ru.trader.core.Profile;
import ru.trader.core.Ship;
import ru.trader.core.Vendor;
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


    /* TODO: check paths

ERROR: 18.12.2016 20:14:37 (TransitPath.java:48) - Incorrect path, path = {Marley City - null(5.650000063702464 - 32.0) -> Transit Col 285 Sector JY-S a33-0, Transit Col 285 Sector JY-S a33-0 - null(2.9400001242756844 - 32.0) -> Transit LP 91-140, Transit LP 91-140 - null(6.830000037327409 - 32.0) -> Transit Shui Wei Sector FH-U b3-4, Transit Shui Wei Sector FH-U b3-4 - null(8.040000010281801 - 31.938907065299873) -> Transit Shui Wei Sector JN-S b4-3, Transit Shui Wei Sector JN-S b4-3 - null(8.480000000447035 - 9.481771344586832) -> Wales City}, fuel = -1.3063401932628143, ship = Ship{cargo=384, engine=7C {optMass=1800.0, fuelPJ=8.5}, tank=32.0, mass=760.3, jumpRange=19.285799993506345, maxDist=19.678943903852804, fullTankDist=76.16151750390145}
ERROR: 18.12.2016 20:14:37 (VendorsCrawler.java:95) - Wrong path, entry {head=[Poindexter Horizons - 0.9589628706630414 -> Anning Enterprise], edge=Anning Enterprise - 0.3624985921259898 -> Marley City}, fuel = 19.302461271880986
ERROR: 18.12.2016 20:14:37 (TransitPath.java:48) - Incorrect path, path = {Marley City - null(5.650000063702464 - 32.0) -> Transit Col 285 Sector JY-S a33-0, Transit Col 285 Sector JY-S a33-0 - null(2.9400001242756844 - 32.0) -> Transit LP 91-140, Transit LP 91-140 - null(6.830000037327409 - 32.0) -> Transit Shui Wei Sector FH-U b3-4, Transit Shui Wei Sector FH-U b3-4 - null(8.040000010281801 - 31.938907065299873) -> Transit Shui Wei Sector JN-S b4-3, Transit Shui Wei Sector JN-S b4-3 - null(8.480000000447035 - 9.481771344586832) -> Wales City}, fuel = -1.3063401932628143, ship = Ship{cargo=384, engine=7C {optMass=1800.0, fuelPJ=8.5}, tank=32.0, mass=760.3, jumpRange=19.285799993506345, maxDist=19.678943903852804, fullTankDist=76.16151750390145}
ERROR: 18.12.2016 20:14:37 (VendorsCrawler.java:95) - Wrong path, entry {head=[Poindexter Horizons - 1.0645592731448934 -> Waldrop Gateway], edge=Waldrop Gateway - 0.5196667298732823 -> Marley City}, fuel = 11.649596203747677
ERROR: 18.12.2016 20:14:37 (TransitPath.java:48) - Incorrect path, path = {Marley City - null(5.650000063702464 - 32.0) -> Transit Col 285 Sector JY-S a33-0, Transit Col 285 Sector JY-S a33-0 - null(2.9400001242756844 - 32.0) -> Transit LP 91-140, Transit LP 91-140 - null(6.830000037327409 - 32.0) -> Transit Shui Wei Sector FH-U b3-4, Transit Shui Wei Sector FH-U b3-4 - null(8.040000010281801 - 31.938907065299873) -> Transit Shui Wei Sector JN-S b4-3, Transit Shui Wei Sector JN-S b4-3 - null(8.480000000447035 - 9.481771344586832) -> Wales City}, fuel = -1.3063401932628143, ship = Ship{cargo=384, engine=7C {optMass=1800.0, fuelPJ=8.5}, tank=32.0, mass=760.3, jumpRange=19.285799993506345, maxDist=19.678943903852804, fullTankDist=76.16151750390145}
ERROR: 18.12.2016 20:14:37 (VendorsCrawler.java:95) - Wrong path, entry {head=[Poindexter Horizons - 1.022834181578632 -> Malenchenko Dock], edge=Malenchenko Dock - 0.8421773536261806 -> Marley City}, fuel = 19.40800122650537
ERROR: 18.12.2016 20:14:37 (TransitPath.java:48) - Incorrect path, path = {Marley City - null(5.650000063702464 - 32.0) -> Transit Col 285 Sector JY-S a33-0, Transit Col 285 Sector JY-S a33-0 - null(2.9400001242756844 - 32.0) -> Transit LP 91-140, Transit LP 91-140 - null(6.830000037327409 - 32.0) -> Transit Shui Wei Sector FH-U b3-4, Transit Shui Wei Sector FH-U b3-4 - null(8.040000010281801 - 31.938907065299873) -> Transit Shui Wei Sector JN-S b4-3, Transit Shui Wei Sector JN-S b4-3 - null(8.480000000447035 - 9.481771344586832) -> Wales City}, fuel = -1.3063401932628143, ship = Ship{cargo=384, engine=7C {optMass=1800.0, fuelPJ=8.5}, tank=32.0, mass=760.3, jumpRange=19.285799993506345, maxDist=19.678943903852804, fullTankDist=76.16151750390145}
ERROR: 18.12.2016 20:14:37 (VendorsCrawler.java:95) - Wrong path, entry {head=[Poindexter Horizons - 1.059486010716856 -> Arnold Hub], edge=Arnold Hub - 0.5363394599627498 -> Marley City}, fuel = 9.01892987314588
ERROR: 18.12.2016 20:14:37 (TransitPath.java:48) - Incorrect path, path = {Fung Station - null(7.730000017210841 - 32.0) -> Transit Col 285 Sector JY-S a33-0, Transit Col 285 Sector JY-S a33-0 - null(2.9400001242756844 - 32.0) -> Transit LP 91-140, Transit LP 91-140 - null(6.830000037327409 - 32.0) -> Transit Shui Wei Sector FH-U b3-4, Transit Shui Wei Sector FH-U b3-4 - null(8.040000010281801 - 31.938907065299873) -> Transit Shui Wei Sector JN-S b4-3, Transit Shui Wei Sector JN-S b4-3 - null(8.480000000447035 - 9.481771344586832) -> Wales City}, fuel = -1.3063401932628143, ship = Ship{cargo=384, engine=7C {optMass=1800.0, fuelPJ=8.5}, tank=32.0, mass=760.3, jumpRange=19.285799993506345, maxDist=19.678943903852804, fullTankDist=76.16151750390145}
ERROR: 18.12.2016 20:14:37 (VendorsCrawler.java:95) - Wrong path, entry {head=[Poindexter Horizons - 1.059486010716856 -> Arnold Hub], edge=Arnold Hub - 0.7340450745407797 -> Fung Station}, fuel = 9.58410781172928
ERROR: 18.12.2016 20:14:37 (TransitPath.java:48) - Incorrect path, path = {Marley City - null(5.650000063702464 - 32.0) -> Transit Col 285 Sector JY-S a33-0, Transit Col 285 Sector JY-S a33-0 - null(2.9400001242756844 - 32.0) -> Transit LP 91-140, Transit LP 91-140 - null(6.830000037327409 - 32.0) -> Transit Shui Wei Sector FH-U b3-4, Transit Shui Wei Sector FH-U b3-4 - null(8.040000010281801 - 31.938907065299873) -> Transit Shui Wei Sector JN-S b4-3, Transit Shui Wei Sector JN-S b4-3 - null(8.480000000447035 - 9.481771344586832) -> Wales City}, fuel = -1.3063401932628143, ship = Ship{cargo=384, engine=7C {optMass=1800.0, fuelPJ=8.5}, tank=32.0, mass=760.3, jumpRange=19.285799993506345, maxDist=19.678943903852804, fullTankDist=76.16151750390145}
ERROR: 18.12.2016 20:14:37 (VendorsCrawler.java:95) - Wrong path, entry {head=[Poindexter Horizons - 1.114615867252881 -> Kaku City], edge=Kaku City - 0.507413400537874 -> Marley City}, fuel = 10.494978262690863
ERROR: 18.12.2016 20:14:37 (TransitPath.java:48) - Incorrect path, path = {Fung Station - null(7.730000017210841 - 32.0) -> Transit Col 285 Sector JY-S a33-0, Transit Col 285 Sector JY-S a33-0 - null(2.9400001242756844 - 32.0) -> Transit LP 91-140, Transit LP 91-140 - null(6.830000037327409 - 32.0) -> Transit Shui Wei Sector FH-U b3-4, Transit Shui Wei Sector FH-U b3-4 - null(8.040000010281801 - 31.938907065299873) -> Transit Shui Wei Sector JN-S b4-3, Transit Shui Wei Sector JN-S b4-3 - null(8.480000000447035 - 9.481771344586832) -> Wales City}, fuel = -1.3063401932628143, ship = Ship{cargo=384, engine=7C {optMass=1800.0, fuelPJ=8.5}, tank=32.0, mass=760.3, jumpRange=19.285799993506345, maxDist=19.678943903852804, fullTankDist=76.16151750390145}
ERROR: 18.12.2016 20:14:37 (VendorsCrawler.java:95) - Wrong path, entry {head=[Poindexter Horizons - 1.12967434055453 -> Windt Terminal], edge=Windt Terminal - 0.21019712790181494 -> Fung Station}, fuel = 19.185052069305907
ERROR: 18.12.2016 20:14:37 (TransitPath.java:48) - Incorrect path, path = {Marley City - null(5.650000063702464 - 32.0) -> Transit Col 285 Sector JY-S a33-0, Transit Col 285 Sector JY-S a33-0 - null(2.9400001242756844 - 32.0) -> Transit LP 91-140, Transit LP 91-140 - null(6.830000037327409 - 32.0) -> Transit Shui Wei Sector FH-U b3-4, Transit Shui Wei Sector FH-U b3-4 - null(8.040000010281801 - 31.938907065299873) -> Transit Shui Wei Sector JN-S b4-3, Transit Shui Wei Sector JN-S b4-3 - null(8.480000000447035 - 9.481771344586832) -> Wales City}, fuel = -1.3063401932628143, ship = Ship{cargo=384, engine=7C {optMass=1800.0, fuelPJ=8.5}, tank=32.0, mass=760.3, jumpRange=19.285799993506345, maxDist=19.678943903852804, fullTankDist=76.16151750390145}
ERROR: 18.12.2016 20:14:37 (VendorsCrawler.java:95) - Wrong path, entry {head=[Poindexter Horizons - 1.12967434055453 -> Windt Terminal], edge=Windt Terminal - 0.4253810806857503 -> Marley City}, fuel = 21.335282458210564
ERROR: 18.12.2016 20:14:37 (TransitPath.java:48) - Incorrect path, path = {Packard Goose - null(0.7500001732259989 - 32.0) -> Transit Col 285 Sector SJ-W b16-3, Transit Col 285 Sector SJ-W b16-3 - null(2.160000141710043 - 32.0) -> Transit Core Sys Sector ZJ-R a4-2, Transit Core Sys Sector ZJ-R a4-2 - null(6.700000040233135 - 32.0) -> Transit LFT 69, Transit LFT 69 - null(6.7200000397861 - 32.0) -> Transit Zhi, Transit Zhi - null(4.670000085607171 - 32.0) -> Transit Pantaa Cezisa, Transit Pantaa Cezisa - null(8.450000001117587 - 10.998717639182587) -> Walker City}, fuel = -0.39807008884942263, ship = Ship{cargo=384, engine=7C {optMass=1800.0, fuelPJ=8.5}, tank=32.0, mass=760.3, jumpRange=19.285799993506345, maxDist=19.678943903852804, fullTankDist=76.16151750390145}
ERROR: 18.12.2016 20:14:37 (VendorsCrawler.java:95) - Wrong path, entry {head=[Poindexter Horizons - 1.12967434055453 -> Windt Terminal], edge=Windt Terminal - 0.4839466057667837 -> Packard Goose}, fuel = 22.101436205250103
ERROR: 18.12.2016 20:14:37 (TransitPath.java:48) - Incorrect path, path = {Highbanks - null(0.7500001732259989 - 32.0) -> Transit Col 285 Sector SJ-W b16-3, Transit Col 285 Sector SJ-W b16-3 - null(2.160000141710043 - 32.0) -> Transit Core Sys Sector ZJ-R a4-2, Transit Core Sys Sector ZJ-R a4-2 - null(6.700000040233135 - 32.0) -> Transit LFT 69, Transit LFT 69 - null(6.7200000397861 - 32.0) -> Transit Zhi, Transit Zhi - null(4.670000085607171 - 32.0) -> Transit Pantaa Cezisa, Transit Pantaa Cezisa - null(8.450000001117587 - 10.998717639182587) -> Walker City}, fuel = -0.39807008884942263, ship = Ship{cargo=384, engine=7C {optMass=1800.0, fuelPJ=8.5}, tank=32.0, mass=760.3, jumpRange=19.285799993506345, maxDist=19.678943903852804, fullTankDist=76.16151750390145}
ERROR: 18.12.2016 20:14:37 (VendorsCrawler.java:95) - Wrong path, entry {head=[Poindexter Horizons - 1.12967434055453 -> Windt Terminal], edge=Windt Terminal - 0.4853098357040021 -> Highbanks}, fuel = 22.101435846439685
ERROR: 18.12.2016 20:14:37 (TransitPath.java:48) - Incorrect path, path = {Marley City - null(5.650000063702464 - 32.0) -> Transit Col 285 Sector JY-S a33-0, Transit Col 285 Sector JY-S a33-0 - null(2.9400001242756844 - 32.0) -> Transit LP 91-140, Transit LP 91-140 - null(6.830000037327409 - 32.0) -> Transit Shui Wei Sector FH-U b3-4, Transit Shui Wei Sector FH-U b3-4 - null(8.040000010281801 - 31.938907065299873) -> Transit Shui Wei Sector JN-S b4-3, Transit Shui Wei Sector JN-S b4-3 - null(8.480000000447035 - 9.481771344586832) -> Wales City}, fuel = -1.3063401932628143, ship = Ship{cargo=384, engine=7C {optMass=1800.0, fuelPJ=8.5}, tank=32.0, mass=760.3, jumpRange=19.285799993506345, maxDist=19.678943903852804, fullTankDist=76.16151750390145}
ERROR: 18.12.2016 20:14:37 (VendorsCrawler.java:95) - Wrong path, entry {head=[Poindexter Horizons - 1.1685569334619363 -> Sullivan Dock], edge=Sullivan Dock - 0.40942306598203076 -> Marley City}, fuel = 11.918362887905289
ERROR: 18.12.2016 20:14:37 (TransitPath.java:48) - Incorrect path, path = {Packard Goose - null(0.7500001732259989 - 32.0) -> Transit Col 285 Sector SJ-W b16-3, Transit Col 285 Sector SJ-W b16-3 - null(2.160000141710043 - 32.0) -> Transit Core Sys Sector ZJ-R a4-2, Transit Core Sys Sector ZJ-R a4-2 - null(6.700000040233135 - 32.0) -> Transit LFT 69, Transit LFT 69 - null(6.7200000397861 - 32.0) -> Transit Zhi, Transit Zhi - null(4.670000085607171 - 32.0) -> Transit Pantaa Cezisa, Transit Pantaa Cezisa - null(8.450000001117587 - 10.998717639182587) -> Walker City}, fuel = -0.39807008884942263, ship = Ship{cargo=384, engine=7C {optMass=1800.0, fuelPJ=8.5}, tank=32.0, mass=760.3, jumpRange=19.285799993506345, maxDist=19.678943903852804, fullTankDist=76.16151750390145}
ERROR: 18.12.2016 20:14:37 (VendorsCrawler.java:95) - Wrong path, entry {head=[Poindexter Horizons - 1.209403569396717 -> Faber Terminal], edge=Faber Terminal - 0.19651840170950163 -> Packard Goose}, fuel = 22.09682950919931
ERROR: 18.12.2016 20:14:37 (TransitPath.java:48) - Incorrect path, path = {Highbanks - null(0.7500001732259989 - 32.0) -> Transit Col 285 Sector SJ-W b16-3, Transit Col 285 Sector SJ-W b16-3 - null(2.160000141710043 - 32.0) -> Transit Core Sys Sector ZJ-R a4-2, Transit Core Sys Sector ZJ-R a4-2 - null(6.700000040233135 - 32.0) -> Transit LFT 69, Transit LFT 69 - null(6.7200000397861 - 32.0) -> Transit Zhi, Transit Zhi - null(4.670000085607171 - 32.0) -> Transit Pantaa Cezisa, Transit Pantaa Cezisa - null(8.450000001117587 - 10.998717639182587) -> Walker City}, fuel = -0.39807008884942263, ship = Ship{cargo=384, engine=7C {optMass=1800.0, fuelPJ=8.5}, tank=32.0, mass=760.3, jumpRange=19.285799993506345, maxDist=19.678943903852804, fullTankDist=76.16151750390145}
ERROR: 18.12.2016 20:14:37 (VendorsCrawler.java:95) - Wrong path, entry {head=[Poindexter Horizons - 1.209403569396717 -> Faber Terminal], edge=Faber Terminal - 0.19707197468372828 -> Highbanks}, fuel = 22.09682909861441
ERROR: 18.12.2016 20:14:37 (TransitPath.java:48) - Incorrect path, path = {Fung Station - null(7.730000017210841 - 32.0) -> Transit Col 285 Sector JY-S a33-0, Transit Col 285 Sector JY-S a33-0 - null(2.9400001242756844 - 32.0) -> Transit LP 91-140, Transit LP 91-140 - null(6.830000037327409 - 32.0) -> Transit Shui Wei Sector FH-U b3-4, Transit Shui Wei Sector FH-U b3-4 - null(8.040000010281801 - 31.938907065299873) -> Transit Shui Wei Sector JN-S b4-3, Transit Shui Wei Sector JN-S b4-3 - null(8.480000000447035 - 9.481771344586832) -> Wales City}, fuel = -1.3063401932628143, ship = Ship{cargo=384, engine=7C {optMass=1800.0, fuelPJ=8.5}, tank=32.0, mass=760.3, jumpRange=19.285799993506345, maxDist=19.678943903852804, fullTankDist=76.16151750390145}
ERROR: 18.12.2016 20:14:37 (VendorsCrawler.java:95) - Wrong path, entry {head=[Poindexter Horizons - 1.2402503460630483 -> Berezovoy Gateway], edge=Berezovoy Gateway - 0.22224602681914715 -> Fung Station}, fuel = 18.361482828396284
ERROR: 18.12.2016 20:14:37 (TransitPath.java:48) - Incorrect path, path = {Alkaabi - null(1.4300001580268145 - 32.0) -> Transit Col 285 Sector SJ-W b16-3, Transit Col 285 Sector SJ-W b16-3 - null(2.160000141710043 - 32.0) -> Transit Core Sys Sector ZJ-R a4-2, Transit Core Sys Sector ZJ-R a4-2 - null(6.700000040233135 - 32.0) -> Transit LFT 69, Transit LFT 69 - null(6.7200000397861 - 32.0) -> Transit Zhi, Transit Zhi - null(4.670000085607171 - 32.0) -> Transit Pantaa Cezisa, Transit Pantaa Cezisa - null(8.450000001117587 - 10.998717639182587) -> Walker City}, fuel = -0.39807008884942263, ship = Ship{cargo=384, engine=7C {optMass=1800.0, fuelPJ=8.5}, tank=32.0, mass=760.3, jumpRange=19.285799993506345, maxDist=19.678943903852804, fullTankDist=76.16151750390145}
ERROR: 18.12.2016 20:14:37 (VendorsCrawler.java:95) - Wrong path, entry {head=[Poindexter Horizons - 1.209403569396717 -> Faber Terminal], edge=Faber Terminal - 0.26566672776028033 -> Alkaabi}, fuel = 23.19443648025583
ERROR: 18.12.2016 20:14:37 (TransitPath.java:48) - Incorrect path, path = {Harry Moore & Co - null(1.4300001580268145 - 32.0) -> Transit Col 285 Sector SJ-W b16-3, Transit Col 285 Sector SJ-W b16-3 - null(2.160000141710043 - 32.0) -> Transit Core Sys Sector ZJ-R a4-2, Transit Core Sys Sector ZJ-R a4-2 - null(6.700000040233135 - 32.0) -> Transit LFT 69, Transit LFT 69 - null(6.7200000397861 - 32.0) -> Transit Zhi, Transit Zhi - null(4.670000085607171 - 32.0) -> Transit Pantaa Cezisa, Transit Pantaa Cezisa - null(8.450000001117587 - 10.998717639182587) -> Walker City}, fuel = -0.39807008884942263, ship = Ship{cargo=384, engine=7C {optMass=1800.0, fuelPJ=8.5}, tank=32.0, mass=760.3, jumpRange=19.285799993506345, maxDist=19.678943903852804, fullTankDist=76.16151750390145}
ERROR: 18.12.2016 20:14:37 (VendorsCrawler.java:95) - Wrong path, entry {head=[Poindexter Horizons - 1.209403569396717 -> Faber Terminal], edge=Faber Terminal - 0.2695206964507874 -> Harry Moore & Co}, fuel = 23.194436510089336

     */
}
