package ru.trader.core;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class ItemStatTest extends Assert {
    private final static Item ITEM1 = new Item("Item1");


    private ItemStat itemSellStat = new ItemStat(ITEM1, OFFER_TYPE.SELL);
    private ItemStat itemBuyStat = new ItemStat(ITEM1, OFFER_TYPE.BUY);

    @Before
    public void fill(){
        itemSellStat.put(new Offer(OFFER_TYPE.SELL, ITEM1, 10));
        itemSellStat.put(new Offer(OFFER_TYPE.SELL, ITEM1, 20));
        itemSellStat.put(new Offer(OFFER_TYPE.SELL, ITEM1, 30));
        itemSellStat.put(new Offer(OFFER_TYPE.SELL, ITEM1, 40));

        itemBuyStat.put(new Offer(OFFER_TYPE.BUY, ITEM1, 100));
        itemBuyStat.put(new Offer(OFFER_TYPE.BUY, ITEM1, 200));
        itemBuyStat.put(new Offer(OFFER_TYPE.BUY, ITEM1, 300));
        itemBuyStat.put(new Offer(OFFER_TYPE.BUY, ITEM1, 400));

    }


    @Test
    public void testSell(){
        assertEquals(itemSellStat.getAvg(), (10+20+30+40)/4, 0);
        assertEquals(itemSellStat.getBest().getPrice(), 10d, 0);
    }

    @Test
    public void testBuy(){
        assertEquals(itemBuyStat.getAvg(), (100+200+300+400)/4, 0);
        assertEquals(itemBuyStat.getBest().getPrice(), 400d, 0);
    }


}
