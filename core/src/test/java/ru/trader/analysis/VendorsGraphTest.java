package ru.trader.analysis;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.analysis.graph.*;
import ru.trader.core.*;
import ru.trader.store.simple.Store;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;


public class VendorsGraphTest extends Assert {
    private final static Logger LOG = LoggerFactory.getLogger(VendorsGraphTest.class);

    private Market world;
    private FilteredMarket fWorld;

    private Place breksta;
    private Place bhadaba;
    private Place lhs1541;
    private Place itza;

    @Before
    public void setUp() throws Exception {
        InputStream is = getClass().getResourceAsStream("/test3.xml");
        world = Store.loadFromFile(is);
        breksta = world.get("Breksta");
        bhadaba = world.get("Bhadaba");
        lhs1541 = world.get("LHS 1541");
        itza = world.get("Itza");

        MarketFilter filter = new MarketFilter();
        fWorld = new FilteredMarket(world, filter);
    }

    @Test
    public void testBuild() throws Exception {
        Vendor grantTerminal = breksta.get("Grant Terminal");
        Vendor perezMarket = breksta.get("Perez market");
        Vendor kandelRing = bhadaba.get("Kandel Ring");
        Vendor robertsHub = bhadaba.get("Roberts Hub");
        Vendor cabreraDock = lhs1541.get("Cabrera Dock");
        Vendor hallerPort = lhs1541.get("Haller Port");
        Vendor luikenPort = itza.get("Luiken Port");
        Ship ship = new Ship();
        ship.setCargo(24); ship.setEngine(2,'A');
        Profile profile = new Profile(ship);
        LOG.info("Start build test");
        profile.setBalance(100000); profile.setJumps(6);
        Scorer scorer = new Scorer(fWorld, profile);
        LOG.info("Build vendors graph");
        VendorsGraph vGraph = new VendorsGraph(scorer, new AnalysisCallBack());
        vGraph.build(cabreraDock, fWorld.getMarkets(true).collect(Collectors.toList()));
        LOG.info("Search");
        SimpleCollector<Vendor> paths = new SimpleCollector<>();
        Crawler<Vendor> crawler = vGraph.crawler(paths::add, new AnalysisCallBack());
        // Cabrera Dock -> Transit Wolf 1323 -> Transit Wolf 1325 -> Quimper Ring -> Transit Bhadaba -> Transit Wolf 1325 -> Cabrera Dock]
        crawler.findMin(cabreraDock, 100);
        assertEquals(100, paths.get().size());
        paths.clear();

        Vertex<Vendor> x = vGraph.getRoot();
        assertNotNull(x);
    }

    private final static Map<String, Integer> edgesStat = new HashMap<>();
    static {
        Object[][] arrays = {
                {"Cabrera Dock",16},
                {"Transit LHS 1507",18},
                {"Transit Bao Yan Luo",12},
                {"Behnken Terminal",12},
                {"Transit LHS 1483",16},
                {"Transit Bhadaba",36},
                {"Transit BD+24 543",23},
                {"Proteus Orbital",23},
                {"Katzenstein Settlement",23},
                {"Ayerohal City",36},
                {"Derleth Orbital",36},
                {"Maire Gateway",36},
                {"Roberts Hub",36},
                {"Dedman Gateway",36},
                {"Bailey Ring",36},
                {"Humphreys Enterprise",36},
                {"Kandel Ring",36},
                {"Polya Enterprise",36},
                {"Transit Kp Tauri",32},
                {"Spedding Orbital",32},
                {"Anders Orbital",32},
                {"Mohmand Dock",32},
                {"Bykovsky Ring",16},
                {"Vaucanson Settlement",16},
                {"Nicollier Ring",16},
                {"Transit LHS 1516",29},
                {"Transit LHS 1573",28},
                {"DG-1 Refinery",28},
                {"Blackman Terminal",28},
                {"Transit LHS 21",21},
                {"Quimper Ring",21},
                {"Crook Orbital",21},
                {"Transit LP 356-106",28},
                {"Carrier Dock",28},
                {"Transit Marduk",30},
                {"Port Sippar",30},
                {"Amar Station",30},
                {"Transit Wolf 1278",29},
                {"Maller Hub",29},
                {"Scott Settlement",29},
                {"Transit Wolf 1325",36},
                {"Gibson Settlement",18},
                {"Euler Port",18},
                {"Transit LHS 1541",16},
                {"Henney City",16},
                {"Haller Port",16},
                {"Pennington City",16},
                {"Foda Station",16},
                {"Stebler City",16},
                {"Transit Wolf 1323",19},
                {"Denis Filippov",19},
                {"Rattus High",19},
                {"Transit Apollo",19},
                {"Reisman Station",19},
                {"Fultion Landing",19},
                {"Transit Meri",19},
                {"Tyurin Port",19},
                {"Transit Bonde",17},
                {"Aksyonov Platform",17},
                {"Transit LHS 1667",10},
                {"Transit Ndozins",19},
                {"Coney Arena",19},
                {"Transit Tascheter Sector CL-Y D99",17},
                {"Transit V491 Persei",25},
                {"Rand City",25},
                {"Transit 39 Tauri",24},
                {"Porta",24},
                {"Transit Lowne 1",7},
                {"Sarich Port",7},
                {"Transit Geras",8},
                {"Transit Herishep",15},
                {"Harris Platform",15},
                {"Transit Ross 592",26},
                {"Transit Sui Xing",24},
                {"Morgan Terminal",24},
                {"Oswald Platform",24},
                {"Transit LTT 11503",19},
                {"Fung Outpost",19},
                {"Transit Ashandras",13},
                {"Bolger Vision",13},
                {"Alpers Refinery",13},
                {"Transit Itza",10},
                {"Ellision Station",10},
                {"Hennepin Enterprise",10},
                {"Luiken Port",10},
                {"Ore Terminal",10},
                {"McDaniel Station",10},
                {"Cochrane Terminal",10},
                {"McDonald Port",10},
                {"Transit G 85-36",3},
                {"Transit Al-Qaum",14},
                {"Duke Hub",14},
                {"Shepard Ring",14},
                {"Cormack Orbital",14},
                {"Transit LTT 11455",15},
                {"Gdeschke Station",15},
                {"Stebler Mines",15},
                {"Transit Tao Ti",0}
        };
        for (Object[] entry : arrays) {
            edgesStat.put((String)entry[0], (Integer)entry[1]);
        }

    }

    @Test
    public void testBuild2() throws Exception {
        Vendor cabreraDock = lhs1541.get("Cabrera Dock");
        Ship ship = new Ship();
        ship.setCargo(24); ship.setEngine(2,'A');
        Profile profile = new Profile(ship);
        LOG.info("Start build test");
        profile.setBalance(100000); profile.setJumps(6);
        LOG.info("Build connectible graph");
        ConnectibleGraph<Vendor> vGraph = new ConnectibleGraph<>(profile, new AnalysisCallBack());
        vGraph.build(cabreraDock, fWorld.getMarkets(true).collect(Collectors.toList()));
        for (Vertex<Vendor> vertex : vGraph.vertexes()) {
            assertEquals(edgesStat.get(vertex.getEntry().toString()).intValue(), vertex.getEdges().size());
        }

        LOG.info("Build vendors graph");
        Scorer scorer = new Scorer(fWorld, profile);
        vGraph = new VendorsGraph(scorer, new AnalysisCallBack());
        vGraph.build(cabreraDock, fWorld.getMarkets(true).collect(Collectors.toList()));
        for (Vertex<Vendor> vertex : vGraph.vertexes()) {
            switch (vertex.getEntry().getName()){
                case "Shepard Ring":
                case "Cormack Orbital":
                case "Duke Hub": assertEquals(8, vertex.getEdges().size());
                    break;
                case "Stebler Mines":
                case "Gdeschke Station": assertEquals(9, vertex.getEdges().size());
                    break;
                default:
                    assertEquals(62, vertex.getEdges().size());
            }
        }
    }

    @Test
    public void testLoop() throws Exception {
        Vendor cabreraDock = lhs1541.get("Cabrera Dock");
        Ship ship = new Ship();
        ship.setCargo(24); ship.setEngine(2,'A');
        Profile profile = new Profile(ship);
        LOG.info("Start build test");
        profile.setBalance(100000); profile.setJumps(6);
        Scorer scorer = new Scorer(fWorld, profile);
        LOG.info("Build vendors graph");
        VendorsGraph vGraph = new VendorsGraph(scorer, new AnalysisCallBack());
        vGraph.build(cabreraDock, fWorld.getMarkets(true).collect(Collectors.toList()));
        LOG.info("Search");
        SimpleCollector<Vendor> paths = new SimpleCollector<>();
        CrawlerSpecificationByProfit specification = new CrawlerSpecificationByProfit(new LoopRouteSpecification<>(true), paths::add, true);
        specification.setGroupCount(60);
        Crawler<Vendor> crawler =  vGraph.crawler(specification, new AnalysisCallBack());
        crawler.findMin(cabreraDock, 100);
        assertEquals(60, paths.get().size());
        Collection<Vendor> vendors = new ArrayList<>(60);
        paths.get().forEach(edges -> {
            Vendor v = edges.get(0).getTarget().getEntry();
            assertFalse(vendors.contains(v));
            vendors.add(v);
        });

    }

    @After
    public void tearDown() throws Exception {
        world = null;
        fWorld = null;
    }
}
