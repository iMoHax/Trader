package ru.trader.analysis;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.core.*;
import ru.trader.store.simple.Store;

import java.io.InputStream;


public class ScorerTest extends Assert {
    private final static Logger LOG = LoggerFactory.getLogger(ScorerTest.class);

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
    public void testScore() throws Exception {
        Vendor grantTerminal = breksta.get("Grant Terminal");
        Vendor perezMarket = breksta.get("Perez market");
        Vendor kandelRing = bhadaba.get("Kandel Ring");
        Vendor robertsHub = bhadaba.get("Roberts Hub");
        Vendor cabreraDock = lhs1541.get("Cabrera Dock");
        Vendor hallerPort = lhs1541.get("Haller Port");
        Vendor luikenPort = itza.get("Luiken Port");
        Ship ship = new Ship();
        ship.setCargo(100);
        Profile profile = new Profile(ship);
        LOG.info("Start score test, balance 10000000");
        profile.setBalance(10000000);
        Scorer scorer = new Scorer(fWorld, profile);
        if (LOG.isDebugEnabled()){
            fWorld.getVendors().map(scorer::getScore).sorted().forEach(s -> LOG.debug("{}", s));
        }

        Scorer.Score gtScore = scorer.getScore(grantTerminal);
        Scorer.Score pmScore = scorer.getScore(perezMarket);
        assertTrue(gtScore.getSellProfit() < pmScore.getSellProfit());
        assertTrue(gtScore.getBuyProfit() > pmScore.getBuyProfit());
        assertTrue(gtScore.getScore() > pmScore.getScore());

        Scorer.Score krScore = scorer.getScore(kandelRing);
        Scorer.Score rhScore = scorer.getScore(robertsHub);
        assertTrue(krScore.getScore() < pmScore.getScore());
        assertTrue(krScore.getScore() < rhScore.getScore());
        assertTrue(krScore.getScore() < gtScore.getScore());

        Scorer.Score cdScore = scorer.getScore(cabreraDock);
        Scorer.Score hpScore = scorer.getScore(hallerPort);
        Scorer.Score lpScore = scorer.getScore(luikenPort);
        assertTrue(hpScore.getScore() > pmScore.getScore());
        assertTrue(hpScore.getScore() > krScore.getScore());
        assertTrue(hpScore.getScore() > gtScore.getScore());
        assertTrue(hpScore.getScore() > lpScore.getScore());
        assertTrue(hpScore.getScore() > cdScore.getScore());
        assertTrue(cdScore.getScore() > pmScore.getScore());
        assertTrue(cdScore.getScore() > krScore.getScore());
        assertTrue(cdScore.getScore() > gtScore.getScore());
        assertTrue(cdScore.getScore() > lpScore.getScore());

        LOG.info("Start score test, balance 50000");
        profile.setBalance(50000);
        Scorer scorer2 = new Scorer(fWorld, profile);
        if (LOG.isDebugEnabled()){
            fWorld.getVendors().map(scorer2::getScore).sorted().forEach(s -> LOG.debug("{}", s));
        }

        gtScore = scorer2.getScore(grantTerminal);
        pmScore = scorer2.getScore(perezMarket);
        assertTrue(gtScore.getSellProfit() > pmScore.getSellProfit());
        assertTrue(gtScore.getBuyProfit() > pmScore.getBuyProfit());
        assertTrue(gtScore.getScore() > pmScore.getScore());

        krScore = scorer2.getScore(kandelRing);
        rhScore = scorer2.getScore(robertsHub);
        cdScore = scorer2.getScore(cabreraDock);
        hpScore = scorer2.getScore(hallerPort);
        lpScore = scorer2.getScore(luikenPort);

        assertTrue(lpScore.getScore() > pmScore.getScore());
        assertTrue(lpScore.getScore() > gtScore.getScore());
        assertTrue(lpScore.getScore() > krScore.getScore());
        assertTrue(lpScore.getScore() > rhScore.getScore());
        assertTrue(lpScore.getScore() > cdScore.getScore());
        assertTrue(lpScore.getScore() > hpScore.getScore());

    }

    @After
    public void tearDown() throws Exception {
        world = null;
        fWorld = null;
    }
}
