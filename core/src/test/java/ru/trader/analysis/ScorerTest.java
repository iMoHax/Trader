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

    @Before
    public void setUp() throws Exception {
        InputStream is = getClass().getResourceAsStream("/test3.xml");
        world = Store.loadFromFile(is);

        MarketFilter filter = new MarketFilter();
        fWorld = new FilteredMarket(world, filter);
    }

    @Test
    public void testScore2() throws Exception {
        Ship ship = new Ship();
        ship.setCargo(100);
        Profile profile = new Profile(ship);
        LOG.info("Start score test, balance 10000000");
        profile.setBalance(1000000);
        Scorer scorer = new Scorer(fWorld, profile);

        double score = scorer.getScore(scorer.getAvgDistance(), 0, 1, 1, 4);
        double score1 = scorer.getScore(scorer.getAvgDistance(), scorer.getAvgProfit()/2, 1, 1, 4);
        double score2 = scorer.getScore(scorer.getAvgDistance(), scorer.getAvgProfit()*10, 1, 1, 4);
        double score3 = scorer.getScore(scorer.getAvgDistance(), scorer.getAvgProfit(), 1, 1, 4);

        assertTrue(score < score1);
        assertTrue(score1 < score3);
        assertTrue(score3 < score2);
        assertTrue(score3 < score2);

        assertTrue(Math.abs(score2/score1) >= 20);
        assertTrue(Math.abs(score3/score1) >= 2);
        assertTrue(Math.abs(score2/score3) >= 10);


        score1 = scorer.getScore(scorer.getAvgDistance(), scorer.getAvgProfit(), 0, 1, 4);
        score2 = scorer.getScore(scorer.getAvgDistance(), scorer.getAvgProfit(), 1, 1, 4);
        score3 = scorer.getScore(scorer.getAvgDistance(), scorer.getAvgProfit(), 2, 1, 4);
        assertTrue(score1 > score2);
        assertTrue(score2 > score3);

        score1 = scorer.getScore(scorer.getAvgDistance(), scorer.getAvgProfit(), 1, 1, 4);
        score2 = scorer.getScore(scorer.getAvgDistance(), scorer.getAvgProfit(), 1, 2, 4);
        score3 = scorer.getScore(scorer.getAvgDistance(), scorer.getAvgProfit(), 1, 3, 4);
        assertTrue(score1 > score2);
        assertTrue(score2 > score3);

        score1 = scorer.getScore(scorer.getAvgDistance(), scorer.getAvgProfit(), 1, 1, 3);
        score2 = scorer.getScore(scorer.getAvgDistance(), scorer.getAvgProfit(), 1, 1, 4);
        score3 = scorer.getScore(scorer.getAvgDistance(), scorer.getAvgProfit(), 1, 1, 5);
        assertTrue(score1 > score2);
        assertTrue(score2 > score3);

        score = scorer.getScore(0, scorer.getAvgProfit(), 1, 1, 4);
        score1 = scorer.getScore(scorer.getAvgDistance()/2, scorer.getAvgProfit(), 1, 1, 4);
        score2 = scorer.getScore(scorer.getAvgDistance(), scorer.getAvgProfit(), 1, 1, 4);
        score3 = scorer.getScore(scorer.getAvgDistance()*2, scorer.getAvgProfit(), 1, 1, 4);
        assertTrue(score > score1);
        assertTrue(score1 > score2);
        assertTrue(score2 > score3);

        score1 = scorer.getScore(700, scorer.getAvgProfit(), 1, 1, 4);
        score2 = scorer.getScore(2800, scorer.getAvgProfit()*1.2, 1, 1, 4);
        score3 = scorer.getScore(2800, scorer.getAvgProfit()*1.5, 1, 1, 4);
        assertTrue(score1 > score2);
        assertTrue(score3 > score1);

        score1 = scorer.getScore(scorer.getAvgDistance(), scorer.getAvgProfit()/2, 0, 1, 4);
        score2 = scorer.getScore(scorer.getAvgDistance(), scorer.getAvgProfit()*2, 1, 1, 4);
        score3 = scorer.getScore(scorer.getAvgDistance(), scorer.getAvgProfit()*4, 0, 1, 4);
        assertTrue(score2 > score1);
        assertTrue(score3 > score2);

        score = scorer.getScore(scorer.getAvgDistance(), scorer.getAvgProfit(), 2, 2, 4);
        score1 = scorer.getScore(scorer.getAvgDistance(), scorer.getAvgProfit()*1.2, 8, 2, 4);
        score2 = scorer.getScore(scorer.getAvgDistance(), scorer.getAvgProfit(), 8, 2, 4);
        score3 = scorer.getScore(scorer.getAvgDistance(), scorer.getAvgProfit()*1.4, 8, 2, 4);
        assertTrue(score > score1);
        assertTrue(score > score2);
        assertTrue(score < score3);

        score = scorer.getScore(scorer.getAvgDistance(), scorer.getAvgProfit(), 4, 2, 4);
        score1 = scorer.getScore(scorer.getAvgDistance(), scorer.getAvgProfit()*1.8, 4, 4, 4);
        score2 = scorer.getScore(scorer.getAvgDistance(), scorer.getAvgProfit(), 4, 4, 4);
        score3 = scorer.getScore(scorer.getAvgDistance(), scorer.getAvgProfit()*2.1, 4, 4, 4);
        assertTrue(score >= score1);
        assertTrue(score > score2);
        assertTrue(score < score3);

    }

    @After
    public void tearDown() throws Exception {
        world = null;
        fWorld = null;
    }
}
