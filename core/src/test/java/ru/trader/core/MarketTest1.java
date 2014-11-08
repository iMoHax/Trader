package ru.trader.core;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.store.simple.SimpleItem;
import ru.trader.store.simple.SimpleMarket;
import ru.trader.store.simple.SimpleOffer;
import ru.trader.store.simple.SimpleVendor;

public class MarketTest1 extends Assert {
    private final static Logger LOG = LoggerFactory.getLogger(MarketTest1.class);

    private final static Item ITEM1 = new SimpleItem("Item1");
    private final static Item ITEM2 = new SimpleItem("Item2");
    private final static Item ITEM3 = new SimpleItem("Item3");

    private final Offer bestSellOffer1 = new SimpleOffer(OFFER_TYPE.SELL,ITEM1,10,1);
    private final Offer bestSellOffer2 = new SimpleOffer(OFFER_TYPE.SELL,ITEM2,15,1);
    private final Offer bestSellOffer3 = new SimpleOffer(OFFER_TYPE.SELL,ITEM3,20,1);

    private final Offer bestBuyOffer1 = new SimpleOffer(OFFER_TYPE.BUY,ITEM1,100,1);
    private final Offer bestBuyOffer2 = new SimpleOffer(OFFER_TYPE.BUY,ITEM2,200,1);
    private final Offer bestBuyOffer3 = new SimpleOffer(OFFER_TYPE.BUY,ITEM3,100,1);
    private final Offer bestBuyOffer4 = new SimpleOffer(OFFER_TYPE.BUY,ITEM2,150,1);


    private final Vendor sellVendor1 = new SimpleVendor("",0,0,0);
    private final Vendor sellVendor2 = new SimpleVendor("",0,0,0);
    private final Vendor sellVendor3 = new SimpleVendor("",0,0,0);
    private final Vendor sellVendor4 = new SimpleVendor("",0,0,0);
    private final Vendor buyVendor1 = new SimpleVendor("",0,0,0);
    private final Vendor buyVendor2 = new SimpleVendor("",0,0,0);
    private final Vendor buyVendor3 = new SimpleVendor("",0,0,0);
    private final Vendor buyVendor4 = new SimpleVendor("",0,0,0);

    private Market market;

    @Before
    public void fillMarket(){
        sellVendor1.add(bestSellOffer1);
        sellVendor1.add(new SimpleOffer(OFFER_TYPE.SELL,ITEM2,100,1));
        sellVendor2.add(new SimpleOffer(OFFER_TYPE.SELL,ITEM3,200,1));
        sellVendor2.add(bestSellOffer2);
        sellVendor3.add(new SimpleOffer(OFFER_TYPE.SELL,ITEM1,300,1));
        sellVendor3.add(new SimpleOffer(OFFER_TYPE.SELL,ITEM2,300,1));
        sellVendor3.add(bestSellOffer3);
        sellVendor4.add(new SimpleOffer(OFFER_TYPE.SELL,ITEM2,150,1));

        buyVendor1.add(new SimpleOffer(OFFER_TYPE.BUY,ITEM2,50,1));
        buyVendor1.add(bestBuyOffer1);
        buyVendor2.add(new SimpleOffer(OFFER_TYPE.BUY,ITEM1,40,1));
        buyVendor2.add(bestBuyOffer2);
        buyVendor2.add(new SimpleOffer(OFFER_TYPE.BUY,ITEM3,50,1));
        buyVendor3.add(bestBuyOffer3);
        buyVendor3.add(new SimpleOffer(OFFER_TYPE.BUY,ITEM2,20,1));
        buyVendor4.add(new SimpleOffer(OFFER_TYPE.BUY,ITEM1,80,1));
        buyVendor4.add(bestBuyOffer4);

        market = new SimpleMarket();
        market.add(sellVendor1.getPlace());
        market.add(sellVendor2.getPlace());
        market.add(sellVendor3.getPlace());
        market.add(sellVendor4.getPlace());
        market.add(buyVendor1.getPlace());
        market.add(buyVendor2.getPlace());
        market.add(buyVendor3.getPlace());
        market.add(buyVendor4.getPlace());
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
