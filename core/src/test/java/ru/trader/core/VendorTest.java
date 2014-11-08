package ru.trader.core;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.store.simple.SimpleItem;
import ru.trader.store.simple.SimpleOffer;
import ru.trader.store.simple.SimpleVendor;


public class VendorTest extends Assert {
    private final static Logger LOG = LoggerFactory.getLogger(VendorTest.class);

    private final static Item ITEM1 = new SimpleItem("Item1");
    private final static Item ITEM2 = new SimpleItem("Item2");
    private final Offer SELL_OFFER = new SimpleOffer(OFFER_TYPE.SELL, ITEM1, 10, 1);
    private final Offer BUY_OFFER = new SimpleOffer(OFFER_TYPE.BUY, ITEM1, 10, 1);

    private Vendor sellVendor;
    private Vendor buyVendor;

    @Before
    public void fillVendor(){
        sellVendor = new SimpleVendor();
        sellVendor.add(SELL_OFFER);
        buyVendor = new SimpleVendor();
        buyVendor.add(BUY_OFFER);
    }

    @Test
    public void testAddSellOffer(){
        LOG.info("Start add sell offer test");
        final Iterable<Offer> offers = sellVendor.getAllSellOffers();
        Offer test = offers.iterator().next();
        assertEquals(test, SELL_OFFER);
    }

    @Test
    public void testAddSellOffer2(){
        LOG.info("Start add sell offer test2");
        final Iterable<Offer> offers = sellVendor.getAllBuyOffers();
        assertFalse(offers.iterator().hasNext());
    }

    @Test
    public void testAddBuyOffer(){
        LOG.info("Start add buy offer test");
        final Iterable<Offer> offers = buyVendor.getAllSellOffers();
        assertFalse(offers.iterator().hasNext());
    }

    @Test
    public void testAddBuyOffer2(){
        LOG.info("Start add buy offer test2");
        final Iterable<Offer> offers = buyVendor.getAllBuyOffers();
        Offer test = offers.iterator().next();
        assertEquals(test, BUY_OFFER);
    }

    @Test
    public void testGetSellOfferOnSellVendor(){
        LOG.info("Start get sell offer from sell vendor test");
        Offer test = sellVendor.getSell(ITEM1);
        assertEquals(test, SELL_OFFER);
    }

    @Test
    public void testGetSellOfferOnBuyVendor(){
        LOG.info("Start get sell offer from buy vendor test");
        Offer test = buyVendor.getSell(ITEM1);
        assertNull(test);
    }

    @Test
    public void testGetWrongItemSellOfferOnSellVendor(){
        LOG.info("Start get wrong item from sell vendor test");
        Offer test = buyVendor.getSell(ITEM2);
        assertNull(test);
    }

    @Test
    public void testGetBuyOfferOnSellVendor(){
        LOG.info("Start get buy offer from sell vendor test");
        Offer test = sellVendor.getBuy(ITEM1);
        assertNull(test);
    }

    @Test
    public void testGetBuyOffersOnBuyVendor(){
        LOG.info("Start get buy offer from buy vendor test");
        Offer test = buyVendor.getBuy(ITEM1);
        assertEquals(test, BUY_OFFER);
    }

    @Test
    public void testGetWrongItemBuyOfferOnBuyVendor(){
        LOG.info("Start get wrong item from buy vendor test");
        Offer test = sellVendor.getBuy(ITEM2);
        assertNull(test);
    }
}
