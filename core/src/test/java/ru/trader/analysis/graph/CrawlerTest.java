package ru.trader.analysis.graph;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.TestUtil;
import ru.trader.analysis.AnalysisCallBack;
import ru.trader.core.Profile;
import ru.trader.core.Ship;

import java.util.ArrayList;
import java.util.List;

public class CrawlerTest extends Assert {
    private final static Logger LOG = LoggerFactory.getLogger(CrawlerTest.class);

    private final static ArrayList<Point> entrys = new ArrayList<>();
    private final static Point x1 = new Point("x1", -40);
    private final static Point x2 = new Point("x2", -20);
    private final static Point x3 = new Point("x3", -10, true);
    private final static Point x4 = new Point("x4", -5, true);
    private final static Point x5 = new Point("x5", 0);
    private final static Point x6 = new Point("x6", 5);
    private final static Point x7 = new Point("x7", 20);
    private final static Point x8 = new Point("x8", 30);
    private final static Point x9 = new Point("x9", 40);
    private final static Point x10 = new Point("x10", 50);

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

    private void assertEdges(List<Edge<Point>> edges, Point ... points){
        for (int i = 1; i < points.length; i++) {
            if (i > edges.size()){
                Assert.fail(String.format("Wrong edges count. Expected: %s Actual: %s", points.length-1, edges.size()));
            }
            Edge<Point> edge = edges.get(i-1);
            Point expSource = points[i-1];
            Point expTarget = points[i];
            if (!edge.getSource().isEntry(expSource)){
                Assert.fail(String.format("Edge start differed. Expected: %s Actual: %s", expSource, edge.getSource().getEntry()));
            }
            if (!edge.getTarget().isEntry(expTarget)){
                Assert.fail(String.format("Edge end differed. Expected: %s Actual: %s", expTarget, edge.getTarget().getEntry()));
            }
        }
    }

    @Test
    public void testGetPaths() throws Exception {
        LOG.info("Start get paths test");
        //max distance 5.126, 1 jump tank
        Ship ship = new Ship();
        ship.setMass(64);ship.setTank(0.6);
        Profile profile = new Profile(ship);
        profile.setJumps(2); profile.setRefill(false);
        LOG.info("Ship = {}, Jumps = {}", profile.getShip(), profile.getJumps());
        ConnectibleGraph<Point> graph = new ConnectibleGraph<>(profile, new AnalysisCallBack());
        graph.build(x5, entrys);
        // x5 <-> x4, x5 <-> x6

        SimpleCollector<Point> paths = new SimpleCollector<>();
        Crawler<Point> crawler = new CCrawler<>(graph, paths::add, new AnalysisCallBack());
        crawler.findMin(x4, 10);
        TestUtil.assertPaths(paths.get(), PPath.of(x5, x4));
        paths.clear();

        crawler.findMin(x6, 10);
        TestUtil.assertPaths(paths.get(), PPath.of(x5, x6));
        paths.clear();

        crawler.findMin(x7, 10);
        assertEquals(paths.get().size(), 0);
        paths.clear();

    }

    @Test
    public void testGetPaths2() throws Exception {
        LOG.info("Start get paths test2");
        //max distance 15.6, 1 jump tank
        Ship ship = new Ship();
        ship.setMass(18);ship.setTank(0.6);
        Profile profile = new Profile(ship);
        profile.setJumps(3); profile.setRefill(false);
        LOG.info("Ship = {}, Jumps = {}", profile.getShip(), profile.getJumps());
        ConnectibleGraph<Point> graph = new ConnectibleGraph<>(profile, new AnalysisCallBack());
        graph.build(x5, entrys);
        //  x5 <-> x4 <-> x3 <-> x2, x5 <-> x6 <-> x7 <-> x8
        //  x5 <-> x3,  x4 <-> x2,  x3 <-> x6, x4 <-> x6
        SimpleCollector<Point> paths = new SimpleCollector<>();
        Crawler<Point> crawler = new CCrawler<>(graph, paths::add, new AnalysisCallBack());

        crawler.findMin(x8, 10);
        TestUtil.assertPaths(paths.get(), PPath.of(x5, x6, x7, x8));
        paths.clear();
        
        crawler.findMin(x7, 10);
        TestUtil.assertPaths(paths.get(), PPath.of(x5, x6, x7), PPath.of(x5, x4, x6, x7), PPath.of(x5, x3, x6, x7));
        paths.clear();

        crawler.findMin(x7);
        assertEquals(1, paths.get().size());
        TestUtil.assertPaths(paths.get(), PPath.of(x5, x6, x7));
        paths.clear();

        crawler.findMin(x4, 20);
        TestUtil.assertPaths(paths.get(), PPath.of(x5, x4), PPath.of(x5, x3, x4), PPath.of(x5, x6, x4),
                PPath.of(x5, x6, x5, x4), PPath.of(x5, x4, x5, x4), PPath.of(x5, x4, x3, x4),
                PPath.of(x5, x4, x6, x4), PPath.of(x5, x6, x3, x4),
                PPath.of(x5, x3, x5, x4), PPath.of(x5, x4, x2, x4),
                PPath.of(x5, x3, x6, x4), PPath.of(x5, x3, x2, x4)
        );
        TestUtil.assertCollectionEquals(paths.getWeights(), 5.0, 15.0, 15.0,
                15.0, 15.0, 15.0,
                25.0, 25.0,
                25.0, 35.0,
                35.0, 35.0);
        paths.clear();

        crawler.findMin(x5, 20);
        TestUtil.assertPaths(paths.get(), PPath.of(x5, x4, x5), PPath.of(x5, x4, x6, x5), PPath.of(x5, x4, x3, x5),
                PPath.of(x5, x6, x5), PPath.of(x5, x6, x4, x5), PPath.of(x5, x6, x3, x5), PPath.of(x5, x3, x5),
                PPath.of(x5, x3, x4, x5), PPath.of(x5, x3, x6, x5));
        paths.clear();

        crawler.findFast(x8);
        assertEdges(paths.get(0), x5, x6, x7, x8);
        paths.clear();

        crawler.findFast(x7, 10);
        assertEdges(paths.get(0), x5, x6, x7);
        TestUtil.assertPaths(paths.get(), PPath.of(x5, x6, x7), PPath.of(x5, x4, x6, x7), PPath.of(x5, x3, x6, x7));
        paths.clear();

        crawler.findFast(x4);
        assertEdges(paths.get(0), x5, x4);
        paths.clear();

    }

    @Test
    public void testGetRefillPaths() throws Exception {
        LOG.info("Start get refill paths");
        //max distance 10.1, 1 jump tank
        Ship ship = new Ship();
        ship.setMass(30.3);ship.setTank(0.6);
        Profile profile = new Profile(ship);
        profile.setJumps(3); profile.setRefill(false);
        LOG.info("Ship = {}, Jumps = {}", profile.getShip(), profile.getJumps());
        ConnectibleGraph<Point> graph = new ConnectibleGraph<>(profile, new AnalysisCallBack());
        graph.build(x5, entrys);
        //  x5 <-> x4 <- refill -> x3 <- refill -> x2, x5 <-> x6
        //  x5 <-> x3 <- refill -> x2,  x5 <-> x4 <- refill -> x6
        SimpleCollector<Point> paths = new SimpleCollector<>();
        Crawler<Point> crawler = new CCrawler<>(graph, paths::add, new AnalysisCallBack());

        crawler.findMin(x1, 10);
        assertTrue(paths.get().isEmpty());
        paths.clear();

        crawler.findMin(x2, 10);
        TestUtil.assertPaths(paths.get(), PPath.of(x5, x4, x3, x2), PPath.of(x5, x3, x2));
        paths.clear();

        crawler.findMin(x6, 10);
        TestUtil.assertPaths(paths.get(), PPath.of(x5, x6), PPath.of(x5, x4, x6),
                PPath.of(x5, x4, x5, x6), PPath.of(x5, x6, x5, x6),
                PPath.of(x5, x3, x4, x6), PPath.of(x5, x3, x5, x6),
                PPath.of(x5, x6, x4, x6));
        TestUtil.assertCollectionEquals(paths.getWeights(), 5.0, 15.0,
                15.0, 15.0,
                25.0, 25.0,
                25.0);
        paths.clear();

        crawler.findFast(x2);
        TestUtil.assertPaths(paths.get(), PPath.of(x5, x3, x2));
        paths.clear();

    }

    @Test
    public void testGetRefillPaths2() throws Exception {
        LOG.info("Start get refill paths 2 ");
        //max distance 15.6, 1 jump tank
        Ship ship = new Ship();
        ship.setMass(18);ship.setTank(0.6);
        Profile profile = new Profile(ship);
        profile.setJumps(4);
        LOG.info("Ship = {}, Jumps = {}", profile.getShip(), profile.getJumps());
        ConnectibleGraph<Point> graph = new ConnectibleGraph<>(profile, new AnalysisCallBack());
        graph.build(x5, entrys);
        // x5 <-> x4 <-> x3 - refill -> x2,
        // x5 <-> x6 <-> x4 <-refill -> x2
        // x5 <-> x3 <- refill -> x2
        // x5 <-> x4 <- refill -> x6
        SimpleCollector<Point> paths = new SimpleCollector<>();
        Crawler<Point> crawler = new CCrawler<>(graph, paths::add, new AnalysisCallBack());

        crawler.findMin(x1, 10);
        assertTrue(paths.get().isEmpty());
        paths.clear();

        crawler.findMin(x2, 20);
        TestUtil.assertPaths(paths.get(), PPath.of(x5, x3, x4, x2), PPath.of(x5, x3, x2),
                PPath.of(x5, x4, x3, x2), PPath.of(x5, x4, x2), PPath.of(x5, x3, x5, x4, x2),
                PPath.of(x5, x6, x4, x2), PPath.of(x5, x6, x4, x3, x2), PPath.of(x5, x4, x3, x4, x2),
                PPath.of(x5, x4, x5, x4, x2), PPath.of(x5, x6, x5, x4, x2), PPath.of(x5, x3, x4, x3, x2),
                PPath.of(x5, x3, x4, x3, x2), PPath.of(x5, x3, x4, x3, x2), PPath.of(x5, x3, x4, x3, x2));
        paths.clear();

        crawler.findMin(x6, 30);
        TestUtil.assertPaths(paths.get(), PPath.of(x5, x6), PPath.of(x5, x4, x6),
                PPath.of(x5, x3, x4, x6), PPath.of(x5, x3, x6), PPath.of(x5, x4, x3, x6),
                PPath.of(x5, x3, x4, x3, x6), PPath.of(x5, x3, x4, x5, x6), PPath.of(x5, x3, x5, x6),
                PPath.of(x5, x3, x5, x4, x6), PPath.of(x5, x4, x3, x4, x6), PPath.of(x5, x4, x3, x5, x6),
                PPath.of(x5, x4, x5, x6), PPath.of(x5, x4, x5, x4, x6), PPath.of(x5, x4, x5, x3, x6),
                PPath.of(x5, x6, x5, x6), PPath.of(x5, x6, x4, x6), PPath.of(x5, x4, x6, x5, x6),
                PPath.of(x5, x6, x4, x5, x6), PPath.of(x5, x6, x5, x4, x6), PPath.of(x5, x6, x5, x3, x6),
                PPath.of(x5, x4, x6, x4, x6), PPath.of(x5, x6, x4, x3, x6)
                );
        paths.clear();

        crawler.findMin(x7, 10);
        assertTrue(paths.get().isEmpty());
        paths.clear();

        crawler.findFast(x2);
        assertNotNull(paths.get());
        assertEquals(2, paths.get().get(0).size());
        paths.clear();

    }

    @Test
    public void testGetCustomPaths() throws Exception {
        LOG.info("Start get custom paths");
        //max distance 15.6, 1 jump tank
        Ship ship = new Ship();
        ship.setMass(18);ship.setTank(0.6);
        Profile profile = new Profile(ship);
        profile.setJumps(4);
        LOG.info("Ship = {}, Jumps = {}", profile.getShip(), profile.getJumps());
        ConnectibleGraph<Point> graph = new ConnectibleGraph<>(profile, new AnalysisCallBack());
        graph.build(x5, entrys);
        // x5 <-> x4 <-> x3 - refill -> x2,
        // x5 <-> x6 <-> x4 <-refill -> x2
        // x5 <-> x3 <- refill -> x2
        // x5 <-> x4 <- refill -> x6
        SimpleCollector<Point> paths = new SimpleCollector<>();
        CCrawler<Point> crawler = new CCrawler<>(graph, paths::add, new AnalysisCallBack());

        crawler.setStartFuel(0.3);
        crawler.findMin(x3, x2);
        TestUtil.assertPaths(paths.get(), PPath.of(x3, x2));
        paths.clear();

        crawler.findFast(x3, x2);
        TestUtil.assertPaths(paths.get(), PPath.of(x3, x2));
        paths.clear();

        crawler.setStartFuel(0.6);
        crawler.findMin(x6, x2);
        TestUtil.assertPaths(paths.get(), PPath.of(x6, x4, x2));
        paths.clear();

    }

    @After
    public void tearDown() throws Exception {
        entrys.clear();
    }

}