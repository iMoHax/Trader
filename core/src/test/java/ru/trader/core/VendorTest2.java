package ru.trader.core;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.TestUtil;
import ru.trader.store.simple.SimpleItem;
import ru.trader.store.simple.SimpleOffer;
import ru.trader.store.simple.SimpleVendor;

import java.util.Collection;

public class VendorTest2 extends Assert {
    private final static Logger LOG = LoggerFactory.getLogger(VendorTest2.class);

    private final static Item ITEM1 = new SimpleItem("Item1");
    private final static Item ITEM2 = new SimpleItem("Item2");
    private final static Item ITEM3 = new SimpleItem("Item3");
    private final static Offer SELL_OFFER1 = new SimpleOffer(OFFER_TYPE.SELL, ITEM1, 10);
    private final static Offer SELL_OFFER2 = new SimpleOffer(OFFER_TYPE.SELL, ITEM2, 10);
    private final static Offer SELL_OFFER3 = new SimpleOffer(OFFER_TYPE.SELL, ITEM3, 10);
    private final static Offer DUBLE_SELL_OFFER1 = new SimpleOffer(OFFER_TYPE.SELL, ITEM1, 100);

    private final static Offer BUY_OFFER1 = new SimpleOffer(OFFER_TYPE.BUY, ITEM1, 100);
    private final static Offer BUY_OFFER2 = new SimpleOffer(OFFER_TYPE.BUY, ITEM2, 10);
    private final static Offer BUY_OFFER3 = new SimpleOffer(OFFER_TYPE.BUY, ITEM3, 10);
    private final static Offer DUBLE_BUY_OFFER1 = new SimpleOffer(OFFER_TYPE.BUY, ITEM1, 10);


    private Vendor sellVendor;
    private Vendor buyVendor;

    @Before
    public void fillVendor(){
        sellVendor = new SimpleVendor();
        sellVendor.add(SELL_OFFER1);
        sellVendor.add(SELL_OFFER2);
        sellVendor.add(SELL_OFFER3);
        sellVendor.add(DUBLE_SELL_OFFER1);

        buyVendor = new SimpleVendor();
        buyVendor.add(BUY_OFFER1);
        buyVendor.add(BUY_OFFER2);
        buyVendor.add(BUY_OFFER3);
        buyVendor.add(DUBLE_BUY_OFFER1);
    }

    @Test
    public void testGetSellOffer(){
        LOG.info("Start get sell offer test");
        final Offer test = sellVendor.getSell(ITEM1);
        assertEquals(test, DUBLE_SELL_OFFER1);
    }

    @Test
    public void testGetBuyOffer(){
        LOG.info("Start get buy offer test");
        final Offer test = buyVendor.getBuy(ITEM1);
        assertEquals(test, DUBLE_BUY_OFFER1);
    }

    @Test
    public void testGetAllSellOffer(){
        LOG.info("Start get all sell offer test");
        final Collection<Offer> test = sellVendor.getAllSellOffers();
        TestUtil.assertCollectionContainAll(test, DUBLE_SELL_OFFER1, SELL_OFFER2, SELL_OFFER3);
    }

    @Test
    public void testGetAllBuyOffer(){
        LOG.info("Start get all buy offer test");
        final Collection<Offer> test = buyVendor.getAllBuyOffers();
        TestUtil.assertCollectionContainAll(test, DUBLE_BUY_OFFER1, BUY_OFFER3, BUY_OFFER2);
    }
}
