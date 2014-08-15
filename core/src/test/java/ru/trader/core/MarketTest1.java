package ru.trader.core;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MarketTest1 extends Assert {
    private final static Logger LOG = LoggerFactory.getLogger(MarketTest1.class);

    private final static Item ITEM1 = new Item("Item1");
    private final static Item ITEM2 = new Item("Item2");
    private final static Item ITEM3 = new Item("Item3");

    private final static Offer bestSellOffer1 = new Offer(OFFER_TYPE.SELL,ITEM1,10);
    private final static Offer bestSellOffer2 = new Offer(OFFER_TYPE.SELL,ITEM2,15);
    private final static Offer bestSellOffer3 = new Offer(OFFER_TYPE.SELL,ITEM3,20);

    private final static Offer bestBuyOffer1 = new Offer(OFFER_TYPE.BUY,ITEM1,100);
    private final static Offer bestBuyOffer2 = new Offer(OFFER_TYPE.BUY,ITEM2,200);
    private final static Offer bestBuyOffer3 = new Offer(OFFER_TYPE.BUY,ITEM3,100);
    private final static Offer bestBuyOffer4 = new Offer(OFFER_TYPE.BUY,ITEM2,150);


    private final static Vendor sellVendor1 = new SimpleVendor();
    private final static Vendor sellVendor2 = new SimpleVendor();
    private final static Vendor sellVendor3 = new SimpleVendor();
    private final static Vendor sellVendor4 = new SimpleVendor();
    private final static Vendor buyVendor1 = new SimpleVendor();
    private final static Vendor buyVendor2 = new SimpleVendor();
    private final static Vendor buyVendor3 = new SimpleVendor();
    private final static Vendor buyVendor4 = new SimpleVendor();

    private Market market;

    @Before
    public void fillMarket(){
        sellVendor1.add(bestSellOffer1);
        sellVendor1.add(new Offer(OFFER_TYPE.SELL,ITEM2,100));
        sellVendor2.add(new Offer(OFFER_TYPE.SELL,ITEM3,200));
        sellVendor2.add(bestSellOffer2);
        sellVendor3.add(new Offer(OFFER_TYPE.SELL,ITEM1,300));
        sellVendor3.add(new Offer(OFFER_TYPE.SELL,ITEM2,300));
        sellVendor3.add(bestSellOffer3);
        sellVendor4.add(new Offer(OFFER_TYPE.SELL,ITEM2,150));

        buyVendor1.add(new Offer(OFFER_TYPE.BUY,ITEM2,50));
        buyVendor1.add(bestBuyOffer1);
        buyVendor2.add(new Offer(OFFER_TYPE.BUY,ITEM1,40));
        buyVendor2.add(bestBuyOffer2);
        buyVendor2.add(new Offer(OFFER_TYPE.BUY,ITEM3,50));
        buyVendor3.add(bestBuyOffer3);
        buyVendor3.add(new Offer(OFFER_TYPE.BUY,ITEM2,20));
        buyVendor4.add(new Offer(OFFER_TYPE.BUY,ITEM1,80));
        buyVendor4.add(bestBuyOffer4);

        market = new SimpleMarket();
        market.add(sellVendor1);
        market.add(sellVendor2);
        market.add(sellVendor3);
        market.add(sellVendor4);
        market.add(buyVendor1);
        market.add(buyVendor2);
        market.add(buyVendor3);
        market.add(buyVendor4);
    }

    @Test
    public void testBestSell(){
        LOG.info("Start best sell test");
        Offer test = market.getStatSell(ITEM1).getBest();
        assertEquals(test, bestSellOffer1);
        test = market.getStatSell(ITEM2).getBest();
        assertEquals(test, bestSellOffer2);
        test = market.getStatSell(ITEM3).getBest();
        assertEquals(test, bestSellOffer3);
    }

    @Test
    public void testBestBuy(){
        LOG.info("Start best buy test");
        Offer test = market.getStatBuy(ITEM1).getBest();
        assertEquals(test, bestBuyOffer1);
        test = market.getStatBuy(ITEM2).getBest();
        assertEquals(test, bestBuyOffer2);
        test = market.getStatBuy(ITEM3).getBest();
        assertEquals(test, bestBuyOffer3);
    }
}
