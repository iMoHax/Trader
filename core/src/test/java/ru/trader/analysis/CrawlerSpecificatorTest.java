package ru.trader.analysis;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.analysis.graph.Crawler;
import ru.trader.core.*;
import ru.trader.store.simple.SimpleOffer;
import ru.trader.store.simple.Store;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class CrawlerSpecificatorTest extends Assert{
    private final static Logger LOG = LoggerFactory.getLogger(CrawlerSpecificatorTest.class);

    private VendorsGraph vGraph;
    private List<Vendor> vendors;

    private Vendor ithaca_st;
    private Vendor lhs3262_st;
    private Vendor morgor_st;
    private Vendor lhs3006_st;
    private Vendor aulin_st;
    private Vendor cmDraco_st;
    private Vendor ovid_st;
    private Vendor aulis_st;

    private Item gold;
    private Item personalweapons;

    @Before
    public void setUp() throws Exception {
        InputStream is = getClass().getResourceAsStream("/world.xml");
        Market world = Store.loadFromFile(is);
        gold = world.getItem("gold");
        personalweapons = world.getItem("personalweapons");
        Place ithaca = world.get("Ithaca");
        Place lhs3262 = world.get("LHS 3262");
        Place morgor = world.get("Morgor");
        Place lhs3006 = world.get("LHS 3006");
        Place ovid = world.get("Ovid");
        Place aulin = world.get("Aulin");
        Place cmDraco = world.get("CM Draco");
        Place aulis = world.get("Aulis");

        ithaca_st = ithaca.get().iterator().next();
        lhs3262_st = lhs3262.get().iterator().next();
        morgor_st = morgor.get().iterator().next();
        lhs3006_st = lhs3006.get().iterator().next();
        aulin_st = aulin.get().iterator().next();
        cmDraco_st = cmDraco.get().iterator().next();
        ovid_st = ovid.get().iterator().next();
        aulis_st = aulis.get().iterator().next();

        MarketFilter filter = new MarketFilter();
        FilteredMarket fWorld = new FilteredMarket(world, filter);

        Ship ship = new Ship();
        ship.setCargo(440); ship.setTank(15);
        ship.setEngine(5, 'A'); ship.setMass(466);
        Profile profile = new Profile(ship);
        profile.setBalance(6000000); profile.setJumps(6);
        profile.setRoutesCount(100); profile.setLands(3);

        Scorer scorer = new Scorer(fWorld, profile);
        LOG.info("Build vendors graph");
        vGraph = new VendorsGraph(scorer, new AnalysisCallBack());
        vendors = fWorld.getMarkets(true).collect(Collectors.toList());
        Vendor ithaca_st = ithaca.get().iterator().next();
        vGraph.build(ithaca_st, vendors);
    }


    @Test
    public void testContainAll() throws Exception {
        LOG.info("Test contain all");
        List<Route> paths = new ArrayList<>();
        CrawlerSpecificator specificator = new CrawlerSpecificator();
        specificator.add(cmDraco_st, true);
        specificator.add(aulin_st, true);
        VendorsCrawlerSpecification spec = specificator.build(vendors, edges -> {paths.add(RouteSearcher.toRoute(edges, vGraph.getScorer()));});

        Crawler<Vendor> crawler = vGraph.crawler(spec, new AnalysisCallBack());
        crawler.setMaxSize(3);
        crawler.findMin(lhs3262_st, 10);
        assertEquals(10, paths.size());
        for (Route path : paths) {
            assertTrue(path.contains(Arrays.asList(cmDraco_st, aulin_st)));
        }
        paths.clear();
    }

    @Test
    public void testAny() throws Exception {
        LOG.info("Test any");
        List<Route> paths = new ArrayList<>();
        CrawlerSpecificator specificator = new CrawlerSpecificator();
        specificator.add(morgor_st, false);
        specificator.add(lhs3006_st, false);
        VendorsCrawlerSpecification spec = specificator.build(vendors, edges -> {paths.add(RouteSearcher.toRoute(edges, vGraph.getScorer()));});

        Crawler<Vendor> crawler = vGraph.crawler(spec, new AnalysisCallBack());
        crawler.setMaxSize(3);
        crawler.findMin(lhs3262_st, 10);
        assertEquals(10, paths.size());
        for (Route path : paths) {
            Vendor target = path.get(path.getJumps()-1).getVendor();
            assertTrue(target == morgor_st || target == lhs3006_st);
        }
        paths.clear();
    }

    @Test
    public void testContainAny() throws Exception {
        LOG.info("Test contain any");
        List<Route> paths = new ArrayList<>();
        CrawlerSpecificator specificator = new CrawlerSpecificator();
        specificator.any(Arrays.asList(aulin_st, morgor_st, lhs3006_st));
        VendorsCrawlerSpecification spec = specificator.build(vendors, edges -> {paths.add(RouteSearcher.toRoute(edges, vGraph.getScorer()));});

        Crawler<Vendor> crawler = vGraph.crawler(spec, new AnalysisCallBack());
        crawler.setMaxSize(3);
        crawler.findMin(lhs3262_st, 10);
        assertEquals(10, paths.size());
        for (Route path : paths) {
            boolean contain = path.contains(Arrays.asList(aulin_st)) ||
                              path.contains(Arrays.asList(morgor_st)) ||
                              path.contains(Arrays.asList(lhs3006_st));

            assertTrue(contain);
        }
        paths.clear();
    }

    @Test
    public void testContainAllAny() throws Exception {
        LOG.info("Test contain all and any target");
        List<Route> paths = new ArrayList<>();
        CrawlerSpecificator specificator = new CrawlerSpecificator();
        specificator.add(cmDraco_st, true);
        specificator.add(aulin_st, true);
        specificator.add(morgor_st, false);
        specificator.add(lhs3006_st, false);
        VendorsCrawlerSpecification spec = specificator.build(vendors, edges -> {paths.add(RouteSearcher.toRoute(edges, vGraph.getScorer()));});

        Crawler<Vendor> crawler = vGraph.crawler(spec, new AnalysisCallBack());
        crawler.setMaxSize(3);
        crawler.findMin(lhs3262_st, 10);
        assertEquals(4, paths.size());
        for (Route path : paths) {
            assertTrue(path.contains(Arrays.asList(cmDraco_st, aulin_st)));
            Vendor target = path.get(path.getJumps()-1).getVendor();
            assertTrue(target == morgor_st || target == lhs3006_st);
        }
        paths.clear();
    }

    @Test
    public void testOffers() throws Exception {
        LOG.info("Test offer");
        List<Route> paths = new ArrayList<>();
        CrawlerSpecificator specificator = new CrawlerSpecificator();
        Offer goldOffer = SimpleOffer.fakeBuy(lhs3262_st, gold, 0, 30);
        Offer weaponOffer = SimpleOffer.fakeBuy(lhs3262_st, personalweapons, 0, 100);
        specificator.buy(Arrays.asList(goldOffer, weaponOffer));
        VendorsCrawlerSpecification spec = specificator.build(vendors, edges -> {paths.add(RouteSearcher.toRoute(edges, vGraph.getScorer()));});

        Crawler<Vendor> crawler = vGraph.crawler(spec, new AnalysisCallBack());
        crawler.setMaxSize(4);
        crawler.findMin(lhs3262_st, 10);
        assertEquals(10, paths.size());
        for (Route path : paths) {
            Collection<Vendor> vs = path.getVendors();
            assertTrue(vs.stream().anyMatch(v -> v.hasSell(gold)) || vs.stream().anyMatch(v -> v.hasSell(personalweapons)));
        }
        paths.clear();
    }

    @Test
    public void testFull() throws Exception {
        LOG.info("Test full");
        List<Route> paths = new ArrayList<>();
        CrawlerSpecificator specificator = new CrawlerSpecificator();
        specificator.add(cmDraco_st, true);
        specificator.add(aulin_st, true);
        specificator.add(lhs3262_st, false);
        specificator.add(lhs3006_st, false);
        specificator.any(Arrays.asList(ovid_st, aulis_st));
        Offer goldOffer = SimpleOffer.fakeBuy(aulin_st, gold, 0, 30);
        Offer weaponOffer = SimpleOffer.fakeBuy(aulin_st, personalweapons, 0, 100);
        specificator.buy(Arrays.asList(goldOffer, weaponOffer));
        VendorsCrawlerSpecification spec = specificator.build(vendors, edges -> {paths.add(RouteSearcher.toRoute(edges, vGraph.getScorer()));});

        Crawler<Vendor> crawler = vGraph.crawler(spec, new AnalysisCallBack());
        crawler.setMaxSize(spec.getMinLands());
        crawler.findMin(ithaca_st, 10);
        assertEquals(10, paths.size());
        for (Route path : paths) {
            Vendor target = path.get(path.getJumps()-1).getVendor();
            assertTrue(target == lhs3262_st || target == lhs3006_st);
            Collection<Vendor> vs = path.getVendors();
            assertTrue(vs.contains(aulis_st)||vs.contains(ovid_st));
            assertTrue(vs.containsAll(Arrays.asList(cmDraco_st, aulin_st)));
            assertTrue(vs.stream().anyMatch(v -> v.hasSell(gold)) || vs.stream().anyMatch(v -> v.hasSell(personalweapons)));
        }
        paths.clear();
    }

    @Test
    public void testMix() throws Exception {
        LOG.info("Test Mix");

        // target A = A
        // contains A and B and C = [A&B&C]
        // target A or B or C = [A|B|C]
        // contains A or B or C = [A,B,C]
        // pair A or B or C to D = [A,B,C - D]

        //A & A -> A
        //A & [A,B,C] -> A
        //A & [B,C] -> A & [B,C]
        //A & [A|B|C] -> A
        //A & [A&B&C] -> A & [B&C]
        //A & [A,B,C - D] -> A & [A,B,C - D]
        //A & [B,C - A] -> A & [B,C]

        //[A,B,C] & A -> A & [B,C]
        //[A,B,C] & [B,C,D] -> [A,B,C,D]
        //[A,B,C] & [B|C|D] -> [B|C] | ([A] & D)
        //[A,B,C] & [B&C&D] -> [B&C&D]
        //[A,B,C] & [B,C,D - E] -> [B,C - E] | ([A] & [D - E])
        //[A,B,C] & [B,C,D - A] -> [B,C,D - A]

        //[A|B|C] & A -> A
        //[A|B|C] & [B,C,D] -> ([D] & A) | ([B|C])
        //[A|B|C] & [B|C|D] -> [A|B|C|D]
        //[A|B|C] & [B&C&D] -> (A & [B&C]) | (B & [C&D]) | (C & [B&D])
        //[A|B|C] & [B,C,D - E] -> [A|B|C] & [B,C,D - E]
        //[A|B|C] & [B,C,D - A] -> (A & [B,C,D]) | ([B|C] & [B,C,D - A])

        //[A&B&C] & A -> A & [B&C]
        //[A&B&C] & [B,C,D] -> [A&B&C]
        //[A&B&C] & [B|C|D] -> ([A&C] & B) | ([A&B] & C) | ([A&B&C] & D)
        //[A&B&C] & [B&C&D] -> [A&B&C&D]
        //[A&B&C] & [B,C,D - E] -> ([A&C] & [B - E]) | ([A&B] & [C - E]) | ([A&B&C] & [D - E])
        //[A&B&C] & [B,C,D,E - A] -> ([B&C] & [D,E - A]) | ([C] & [B - A])

        //[A,B,C - D] & A -> [A,B,C - D] & A
        //[A,B,C - D] & D -> [A,B,C] & D
        //[A,B,C - D] & [B,C,D,E] -> [A,B,C - D]
        //[A,B,C - F] & [B,C,D,E] -> ([A - F] & [D,E]) | [B,C - F]
        //[A,B,C - D] & [B|C|D|E] -> ([A,B,C - D] & D) | ([A,B,C - D] & [B|C|E])
        //[A,B,C - F] & [B|C|D|E] -> [A,B,C - F] & [B|C|D|E]
        //[A,B,C - D] & [B&C&D&E] -> ([B - D] & [C&E]) | ([C - D] & [B&E]) | ([A - D] & [B&C&E])
        //[A,B,C - F] & [B&C&D&E] -> ([B - F] & [C&D&E]) | ([C - F] & [B&D&E]) | ([A - F] & [B&C&D&E])
        //[A,B,C - D] & [B,C,D - E] -> ([B,C - D - E]) | ([A - D] & [B,C - E])
        //[A,B,C - F] & [B,C,D - F] -> [B,C - F] | ([A - D - F]) | ([D - A - F])

    }

}
