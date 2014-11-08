package ru.trader.store.simple;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.core.Item;
import ru.trader.core.OFFER_TYPE;


public class ItemStatTest extends Assert {
    private final static Logger LOG = LoggerFactory.getLogger(ItemStatTest.class);

    private final static Item ITEM1 = new SimpleItem("Item1");


    private SimpleItemStat itemSellStat = new SimpleItemStat(ITEM1, OFFER_TYPE.SELL);
    private SimpleItemStat itemBuyStat = new SimpleItemStat(ITEM1, OFFER_TYPE.BUY);

    @Before
    public void fill(){
        itemSellStat.put(new SimpleOffer(OFFER_TYPE.SELL, ITEM1, 10, 1));
        itemSellStat.put(new SimpleOffer(OFFER_TYPE.SELL, ITEM1, 20, 1));
        itemSellStat.put(new SimpleOffer(OFFER_TYPE.SELL, ITEM1, 30, 1));
        itemSellStat.put(new SimpleOffer(OFFER_TYPE.SELL, ITEM1, 40, 1));

        itemBuyStat.put(new SimpleOffer(OFFER_TYPE.BUY, ITEM1, 100, 1));
        itemBuyStat.put(new SimpleOffer(OFFER_TYPE.BUY, ITEM1, 200, 1));
        itemBuyStat.put(new SimpleOffer(OFFER_TYPE.BUY, ITEM1, 300, 1));
        itemBuyStat.put(new SimpleOffer(OFFER_TYPE.BUY, ITEM1, 400, 1));

    }


    @Test
    public void testSell(){
        LOG.info("Start sell test");
        assertEquals(itemSellStat.getAvg(), (10+20+30+40)/4, 0);
        assertEquals(itemSellStat.getBest().getPrice(), 10d, 0);
    }

    @Test
    public void testBuy(){
        LOG.info("Start buy test");
        assertEquals(itemBuyStat.getAvg(), (100+200+300+400)/4, 0);
        assertEquals(itemBuyStat.getBest().getPrice(), 400d, 0);
    }


}
