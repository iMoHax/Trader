package ru.trader.core;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class VendorTest extends Assert {
    private final static Item ITEM1 = new Item("Item1");
    private final static Item ITEM2 = new Item("Item2");
    private final static Offer SELL_OFFER = new Offer(OFFER_TYPE.SELL, ITEM1, 10);
    private final static Offer BUY_OFFER = new Offer(OFFER_TYPE.BUY, ITEM1, 10);

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
        final Iterable<Offer> offers = sellVendor.getAllSellOffers();
        Offer test = offers.iterator().next();
        assertEquals(test, SELL_OFFER);
    }

    @Test
    public void testAddSellOffer2(){
        final Iterable<Offer> offers = sellVendor.getAllBuyOffers();
        assertFalse(offers.iterator().hasNext());
    }

    @Test
    public void testAddBuyOffer(){
        final Iterable<Offer> offers = buyVendor.getAllSellOffers();
        assertFalse(offers.iterator().hasNext());
    }

    @Test
    public void testAddBuyOffer2(){
        final Iterable<Offer> offers = buyVendor.getAllBuyOffers();
        Offer test = offers.iterator().next();
        assertEquals(test, BUY_OFFER);
    }

    @Test
    public void testGetSellOfferOnSellVendor(){
        Offer test = sellVendor.getSell(ITEM1);
        assertEquals(test, SELL_OFFER);
    }

    @Test
    public void testGetSellOfferOnBuyVendor(){
        Offer test = buyVendor.getSell(ITEM1);
        assertNull(test);
    }

    @Test
    public void testGetWrongItemSellOfferOnSellVendor(){
        Offer test = buyVendor.getSell(ITEM2);
        assertNull(test);
    }

    @Test
    public void testGetBuyOfferOnSellVendor(){
        Offer test = sellVendor.getBuy(ITEM1);
        assertNull(test);
    }

    @Test
    public void testGetBuyOffersOnBuyVendor(){
        Offer test = buyVendor.getBuy(ITEM1);
        assertEquals(test, BUY_OFFER);
    }

    @Test
    public void testGetWrongItemBuyOfferOnBuyVendor(){
        Offer test = sellVendor.getBuy(ITEM2);
        assertNull(test);
    }
}
