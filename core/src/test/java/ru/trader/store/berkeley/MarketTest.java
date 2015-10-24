package ru.trader.store.berkeley;


import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.core.*;
import ru.trader.store.simple.SimpleMarket;

import java.io.File;
import java.util.Collection;


public class MarketTest  extends Assert {
    private final static Logger LOG = LoggerFactory.getLogger(MarketTest.class);
    private BDBStore store;
    private Market market;

    @Before
    public void setUp() throws Exception {
        File f = new File("test-bd");
        if (!f.exists()){
            f.mkdir();
        }
        store = new BDBStore("test-bd");
        market = new BDBMarket(store);

    }

    @Test
    public void testGroup(){
        LOG.info("Start test group");
        Group group1 = market.addGroup("Group 1", GROUP_TYPE.MARKET);
        Group group2 = market.addGroup("Group 2", GROUP_TYPE.OUTFIT);
        Group group3 = market.addGroup("Group 3", GROUP_TYPE.MARKET);
        int c = 3;
        for (Group group : market.getGroups()) {
            if ("Group 1".equals(group.getName())){
                assertEquals(group1, group);
                assertEquals(GROUP_TYPE.MARKET, group.getType());
                c--;
            } else
            if ("Group 2".equals(group.getName())){
                assertEquals(group2, group);
                assertEquals(GROUP_TYPE.OUTFIT, group.getType());
                c--;
            } else
            if ("Group 3".equals(group.getName())){
                assertEquals(group3, group);
                assertEquals(GROUP_TYPE.MARKET, group.getType());
                c--;
            } else c = -1;
        }
        assertEquals("Wrong group count", 0, c);

        market.remove(group1);
        market.remove(group2);
        market.remove(group3);

        assertEquals(0, market.getGroups().size());
    }


    @Test
    public void testItem(){
        LOG.info("Start test item");
        Group group1 = market.addGroup("Group 1", GROUP_TYPE.MARKET);
        Group group2 = market.addGroup("Group 2", GROUP_TYPE.OUTFIT);

        Item item1 = market.addItem("Item 1", group1);
        Item item2 = market.addItem("Item 2", group2);
        Item item3 = market.addItem("Item 3", group1);
        int c = 3;
        for (Item item : market.getItems()) {
            if ("Item 1".equals(item.getName())){
                assertEquals(item1, item);
                assertEquals(group1, item.getGroup());
                c--;
            } else
            if ("Item 2".equals(item.getName())){
                assertEquals(item2, item);
                assertEquals(group2, item.getGroup());
                c--;
            } else
            if ("Item 3".equals(item.getName())){
                assertEquals(item3, item);
                assertEquals(group1, item.getGroup());
                c--;
            } else c = -1;

        }
        assertEquals("Wrong item count", 0, c);


        market.remove(group1);
        market.remove(group2);

        assertEquals(0, market.getGroups().size());

        market.remove(item1);
        market.remove(item2);
        market.remove(item3);

        assertEquals(0, market.getItems().size());


    }


    @Test
    public void testPlace(){
        LOG.info("Start test place");
        Place place1 = market.addPlace("Place 1", 1, 2, 3);
        Place place2 = market.addPlace("Place 2", 3, 1, 2);

        int c = 2;
        for (Place place : market.get()) {
            if ("Place 1".equals(place.getName())){
                assertEquals(place1, place);
                assertEquals(1, place.getX(), 0.01);
                assertEquals(2, place.getY(), 0.01);
                assertEquals(3, place.getZ(), 0.01);
                c--;
            } else
            if ("Place 2".equals(place.getName())){
                assertEquals(place2, place);
                assertEquals(3, place.getX(), 0.01);
                assertEquals(1, place.getY(), 0.01);
                assertEquals(2, place.getZ(), 0.01);
                c--;
            } else c = -1;

        }
        assertEquals("Wrong item count", 0, c);

        market.remove(place1);
        market.remove(place2);

        assertEquals(0, market.get().size());

    }


    @Test
    public void testVendor(){
        LOG.info("Start test vendor");
        Place place1 = market.addPlace("Place 1", 1, 2, 3);
        Place place2 = market.addPlace("Place 2", 3, 1, 2);
        Vendor vendor1 = place1.addVendor("Vendor 1");
        vendor1.setDistance(100);
        vendor1.add(SERVICE_TYPE.MARKET);
        vendor1.add(SERVICE_TYPE.OUTFIT);

        Vendor vendor2 = place1.addVendor("Vendor 2");
        vendor2.setDistance(40);


        Vendor vendor3 = place2.addVendor("Vendor 3");
        vendor3.add(SERVICE_TYPE.OUTFIT);
        vendor3.add(SERVICE_TYPE.LARGE_LANDPAD);
        vendor3.add(SERVICE_TYPE.MEDIUM_LANDPAD);
        vendor3.remove(SERVICE_TYPE.LARGE_LANDPAD);

        int c = 3;
        for (Vendor vendor : market.getVendors()) {
            if ("Vendor 1".equals(vendor.getName())){
                assertEquals(vendor1, vendor);
                assertEquals(place1, vendor.getPlace());
                assertEquals(100, vendor.getDistance(), 0.001);
                assertTrue(vendor.has(SERVICE_TYPE.MARKET));
                assertTrue(vendor.has(SERVICE_TYPE.OUTFIT));
                assertFalse(vendor.has(SERVICE_TYPE.LARGE_LANDPAD));
                assertFalse(vendor.has(SERVICE_TYPE.BLACK_MARKET));
                assertFalse(vendor.has(SERVICE_TYPE.MEDIUM_LANDPAD));
                c--;
            } else
            if ("Vendor 2".equals(vendor.getName())){
                assertEquals(vendor2, vendor);
                assertEquals(place1, vendor.getPlace());
                assertEquals(40, vendor.getDistance(), 0.001);
                assertFalse(vendor.has(SERVICE_TYPE.MARKET));
                assertFalse(vendor.has(SERVICE_TYPE.OUTFIT));
                assertFalse(vendor.has(SERVICE_TYPE.LARGE_LANDPAD));
                assertFalse(vendor.has(SERVICE_TYPE.BLACK_MARKET));
                assertFalse(vendor.has(SERVICE_TYPE.MEDIUM_LANDPAD));
                c--;
            } else
            if ("Vendor 3".equals(vendor.getName())){
                assertEquals(vendor3, vendor);
                assertEquals(place2, vendor.getPlace());
                assertEquals(0, vendor.getDistance(), 0.001);
                assertFalse(vendor.has(SERVICE_TYPE.MARKET));
                assertTrue(vendor.has(SERVICE_TYPE.OUTFIT));
                assertFalse(vendor.has(SERVICE_TYPE.LARGE_LANDPAD));
                assertFalse(vendor.has(SERVICE_TYPE.BLACK_MARKET));
                assertTrue(vendor.has(SERVICE_TYPE.MEDIUM_LANDPAD));
                c--;
            } else c = -1;

        }
        assertEquals("Wrong vendors count", 0, c);

        place1.remove(vendor1);
        place1.remove(vendor2);
        place2.remove(vendor3);

        assertEquals(0, market.getVendors().size());

        market.remove(place1);
        market.remove(place2);

        assertEquals(0, market.getVendors(true).size());
    }

    @Test
    public void testOffer(){
        LOG.info("Start test offer");
        Group group1 = market.addGroup("Group 1", GROUP_TYPE.MARKET);
        Group group2 = market.addGroup("Group 2", GROUP_TYPE.OUTFIT);
        Item item1 = market.addItem("Item 1", group1);
        Item item2 = market.addItem("Item 2", group2);
        Item item3 = market.addItem("Item 3", group1);
        Place place1 = market.addPlace("Place 1", 1, 2, 3);
        Vendor vendor1 = place1.addVendor("Vendor 1");

        Offer offer1 = vendor1.addOffer(OFFER_TYPE.SELL, item1, 254, 10);
        Offer offer2 = vendor1.addOffer(OFFER_TYPE.BUY, item1, 300, 15);
        Offer offer3 = vendor1.addOffer(OFFER_TYPE.BUY, item3, 100, 100);
        Offer offer4 = vendor1.addOffer(OFFER_TYPE.SELL, item2, 10, -1);

        int c = 2;
        for (Offer offer : vendor1.getAllSellOffers()) {
            if (item1.equals(offer.getItem())){
                assertEquals(offer1, offer);
                assertEquals(OFFER_TYPE.SELL, offer.getType());
                assertEquals(254, offer.getPrice(), 0.01);
                assertEquals(10, offer.getCount());
                c--;
            } else
            if (item2.equals(offer.getItem())){
                assertEquals(offer4, offer);
                assertEquals(OFFER_TYPE.SELL, offer.getType());
                assertEquals(10, offer.getPrice(), 0.01);
                assertEquals(-1, offer.getCount());
                c--;
            } else c = -1;
        }
        assertEquals("Wrong buy offers count", 0, c);


        c = 2;
        for (Offer offer : vendor1.getAllBuyOffers()) {
            if (item1.equals(offer.getItem())){
                assertEquals(offer2, offer);
                assertEquals(OFFER_TYPE.BUY, offer.getType());
                assertEquals(300, offer.getPrice(), 0.01);
                assertEquals(15, offer.getCount());
                c--;
            } else
            if (item3.equals(offer.getItem())){
                assertEquals(offer3, offer);
                assertEquals(OFFER_TYPE.BUY, offer.getType());
                assertEquals(100, offer.getPrice(), 0.01);
                assertEquals(100, offer.getCount());
                c--;
            } else c = -1;
        }
        assertEquals("Wrong buy offers count", 0, c);

        vendor1.remove(offer1);
        vendor1.remove(offer2);
        vendor1.remove(offer3);
        vendor1.remove(offer4);
        assertEquals(0, vendor1.getAllBuyOffers().size());
        assertEquals(0, vendor1.getAllSellOffers().size());

        place1.remove(vendor1);
        market.remove(place1);
        market.remove(item1);
        market.remove(item2);
        market.remove(item3);
        market.remove(group1);
        market.remove(group2);
    }


    @Test
    public void testItemStat(){
        LOG.info("Start item stat test");
        Group group1 = market.addGroup("Group 1", GROUP_TYPE.MARKET);
        Item item1 = market.addItem("Item 1", group1);
        Item item2 = market.addItem("Item 2", group1);
        Place place1 = market.addPlace("Place 1", 1, 2, 3);
        Vendor vendor1 = place1.addVendor("Vendor 1");
        Vendor vendor2 = place1.addVendor("Vendor 2");
        Vendor vendor3 = place1.addVendor("Vendor 3");
        Vendor vendor4 = place1.addVendor("Vendor 4");

        Offer offer1 = vendor1.addOffer(OFFER_TYPE.SELL, item1, 10, 1);
        Offer offer2 = vendor2.addOffer(OFFER_TYPE.SELL, item1, 20, 1);
        Offer offer3 = vendor3.addOffer(OFFER_TYPE.SELL, item1, 30, 1);
        Offer offer4 = vendor4.addOffer(OFFER_TYPE.SELL, item1, 40, 1);

        Offer offer5 = vendor1.addOffer(OFFER_TYPE.BUY, item1, 100, 1);
        Offer offer6 = vendor2.addOffer(OFFER_TYPE.BUY, item1, 200, 1);
        Offer offer7 = vendor3.addOffer(OFFER_TYPE.BUY, item1, 300, 1);
        Offer offer8 = vendor4.addOffer(OFFER_TYPE.BUY, item1, 400, 1);

        Offer offer9 = vendor1.addOffer(OFFER_TYPE.BUY, item2, 1, 30);
        Offer offer10 = vendor2.addOffer(OFFER_TYPE.BUY, item2, 500, 10);


        ItemStat sellStat = market.getStat(OFFER_TYPE.SELL, item1);
        assertEquals((10+20+30+40)/4, sellStat.getAvg(), 0);
        assertEquals(offer1, sellStat.getBest());
        assertEquals(offer1, sellStat.getMin());
        assertEquals(offer4, sellStat.getMax());
        assertEquals(4, sellStat.getOffers().size());

        ItemStat buyStat = market.getStat(OFFER_TYPE.BUY, item1);
        assertEquals((100+200+300+400)/4, buyStat.getAvg(), 0);
        assertEquals(offer8, buyStat.getBest());
        assertEquals(offer5, buyStat.getMin());
        assertEquals(offer8, buyStat.getMax());
        assertEquals(4, buyStat.getOffers().size());

        vendor1.remove(offer1);
        vendor4.remove(offer4);
        assertEquals((20+30)/2, sellStat.getAvg(), 0);
        assertEquals(offer2, sellStat.getBest());
        assertEquals(offer2, sellStat.getMin());
        assertEquals(offer3, sellStat.getMax());
        assertEquals(2, sellStat.getOffers().size());

        market.remove(place1);

        assertEquals(0, market.getSell(item1).size());
        assertEquals(0, market.getBuy(item1).size());
        assertEquals(0, market.getVendors(true).size());
        assertEquals(0, market.get().size());
        assertEquals(0, sellStat.getOffers().size());
        assertEquals(0, buyStat.getOffers().size());

        market.remove(item1);
        market.remove(item2);
        market.remove(group1);
        assertTrue(market.getStatBuy(item1).isEmpty());
        assertTrue(market.getStatBuy(item2).isEmpty());
    }


    private void assertGroup(Group group1, Group group2){
        assertEquals(group1.getName(), group2.getName());
        assertEquals(group1.getType(), group2.getType());
    }

    private void assertItem(Item item1, Item item2){
        assertEquals(item1.getName(), item2.getName());
        assertGroup(item1.getGroup(), item2.getGroup());
    }

    private void assertPlace(Place place1, Place place2){
        assertEquals(place1.getName(), place2.getName());
        assertEquals(place1.getX(), place2.getX(), 0.00001);
        assertEquals(place1.getY(), place2.getY(), 0.00001);
        assertEquals(place1.getZ(), place2.getZ(), 0.00001);
    }

    private void assertVendor(Vendor vendor1, Vendor vendor2){
        assertEquals(vendor1.getName(), vendor2.getName());
        assertEquals(vendor1.getDistance(), vendor2.getDistance(), 0.00001);
    }

    private void assertOffer(Offer offer1, Offer offer2){
        assertEquals(offer1.getType(), offer2.getType());
        assertItem(offer1.getItem(), offer2.getItem());
        assertEquals(offer1.getPrice(), offer2.getPrice(), 0.000001);
        assertEquals(offer1.getCount(), offer2.getCount());
    }

    @Test
    public void testImport(){
        LOG.info("Start import test");

        Market world = new SimpleMarket();
        Group group1 = world.addGroup("Group 1", GROUP_TYPE.MARKET);
        Group group2 = world.addGroup("Group 2", GROUP_TYPE.MARKET);
        Group group3 = world.addGroup("Group 3", GROUP_TYPE.OUTFIT);
        Item item1 = world.addItem("Item 1", group1);
        Item item2 = world.addItem("Item 2", group1);
        Item item3 = world.addItem("Item 3", group2);
        Item item4 = world.addItem("Item 4", group2);
        Item item5 = world.addItem("Item 5", group3);
        Place place1 = world.addPlace("Place 1", 0, 1, 3);
        Place place2 = world.addPlace("Place 2",4,0,5);
        Place place3 = world.addPlace("Place 3",0,0,0);
        Vendor vendor1 = place1.addVendor("Vendor 1");
        Vendor vendor2 = place1.addVendor("Vendor 2");
        Vendor vendor3 = place2.addVendor("Vendor 3");
        vendor1.setDistance(10);
        vendor1.add(SERVICE_TYPE.MARKET);
        vendor1.add(SERVICE_TYPE.OUTFIT);
        vendor1.add(SERVICE_TYPE.REFUEL);
        Offer offer1 = vendor1.addOffer(OFFER_TYPE.SELL, item1, 10,43);
        Offer offer2 = vendor1.addOffer(OFFER_TYPE.BUY, item1, 12,1);
        Offer offer3 = vendor1.addOffer(OFFER_TYPE.SELL, item2, 1012,1000);
        Offer offer4 = vendor1.addOffer(OFFER_TYPE.SELL, item3, 110,0);
        Offer offer5 = vendor1.addOffer(OFFER_TYPE.SELL, item4, 1112,12);
        Offer offer6 = vendor1.addOffer(OFFER_TYPE.BUY, item5, 11,10);
        vendor2.setDistance(100.4);
        vendor3.setDistance(200000.4);
        vendor3.add(SERVICE_TYPE.OUTFIT);
        vendor3.add(SERVICE_TYPE.REFUEL);

        LOG.info("add market");
        market.add(world);

        LOG.info("check groups");
        Collection<Group> groups = world.getGroups();
        int i=0;
        for (Group group : groups) {
            if (group1.getName().equals(group.getName())) {assertGroup(group, group1);i++;}
            if (group2.getName().equals(group.getName())) {assertGroup(group, group2);i++;}
            if (group3.getName().equals(group.getName())) {assertGroup(group, group3);i++;}
        }
        assertEquals(3, i);

        LOG.info("check items");
        Collection<Item> items = world.getItems();
        i=0;
        for (Item item : items) {
            if (item1.getName().equals(item.getName())) {assertItem(item, item1);i++;}
            if (item2.getName().equals(item.getName())) {assertItem(item, item2);i++;}
            if (item3.getName().equals(item.getName())) {assertItem(item, item3);i++;}
            if (item4.getName().equals(item.getName())) {assertItem(item, item4);i++;}
            if (item5.getName().equals(item.getName())) {assertItem(item, item5);i++;}
        }
        assertEquals(5, i);

        LOG.info("check places");
        Collection<Place> places = world.get();
        i=0;
        for (Place place : places) {
            if (place1.getName().equals(place.getName())) {
                assertPlace(place, place1);
                i++;
                Collection<Vendor> vendors = place.get();
                int j=0;
                for (Vendor vendor : vendors) {
                    if (vendor1.getName().equals(vendor.getName())) {
                        LOG.info("check vendor 1");
                        assertVendor(vendor, vendor1);j++;
                        Collection<Offer> offers = vendor.getAllSellOffers();
                        int o = 0;
                        for (Offer offer : offers) {
                            if (offer1.getItem().getName().equals(offer.getItem().getName())) {assertOffer(offer, offer1);o++;}
                            if (offer3.getItem().getName().equals(offer.getItem().getName())) {assertOffer(offer, offer3);o++;}
                            if (offer4.getItem().getName().equals(offer.getItem().getName())) {assertOffer(offer, offer4);o++;}
                            if (offer5.getItem().getName().equals(offer.getItem().getName())) {assertOffer(offer, offer5);o++;}
                        }
                        assertEquals(4, o);
                        offers = vendor.getAllBuyOffers();
                        o = 0;
                        for (Offer offer : offers) {
                            if (offer2.getItem().getName().equals(offer.getItem().getName())) {assertOffer(offer, offer2);o++;}
                            if (offer6.getItem().getName().equals(offer.getItem().getName())) {assertOffer(offer, offer6);o++;}
                        }
                        assertEquals(2, o);
                        assertTrue(vendor.has(SERVICE_TYPE.MARKET));
                        assertFalse(vendor.has(SERVICE_TYPE.BLACK_MARKET));
                        assertFalse(vendor.has(SERVICE_TYPE.REPAIR));
                        assertFalse(vendor.has(SERVICE_TYPE.MUNITION));
                        assertTrue(vendor.has(SERVICE_TYPE.OUTFIT));
                        assertFalse(vendor.has(SERVICE_TYPE.SHIPYARD));
                        assertTrue(vendor.has(SERVICE_TYPE.REFUEL));
                    }
                    if (vendor2.getName().equals(vendor.getName())) {
                        LOG.info("check vendor 2");
                        assertVendor(vendor, vendor2);j++;
                        assertTrue(vendor.getAllBuyOffers().isEmpty());
                        assertTrue(vendor.getAllSellOffers().isEmpty());
                        assertFalse(vendor.has(SERVICE_TYPE.MARKET));
                        assertFalse(vendor.has(SERVICE_TYPE.BLACK_MARKET));
                        assertFalse(vendor.has(SERVICE_TYPE.REPAIR));
                        assertFalse(vendor.has(SERVICE_TYPE.MUNITION));
                        assertFalse(vendor.has(SERVICE_TYPE.OUTFIT));
                        assertFalse(vendor.has(SERVICE_TYPE.SHIPYARD));
                        assertFalse(vendor.has(SERVICE_TYPE.REFUEL));
                    }
                }
                assertEquals(2, j);
                assertTrue(place.canRefill());
            }
            if (place2.getName().equals(place.getName())) {
                assertPlace(place, place2);
                i++;
                Collection<Vendor> vendors = place.get();
                int j=0;
                for (Vendor vendor : vendors) {
                    if (vendor3.getName().equals(vendor.getName())) {
                        LOG.info("check vendor 3");
                        assertVendor(vendor, vendor3);j++;
                        assertTrue(vendor.getAllBuyOffers().isEmpty());
                        assertTrue(vendor.getAllSellOffers().isEmpty());
                        assertFalse(vendor.has(SERVICE_TYPE.MARKET));
                        assertFalse(vendor.has(SERVICE_TYPE.BLACK_MARKET));
                        assertFalse(vendor.has(SERVICE_TYPE.REPAIR));
                        assertFalse(vendor.has(SERVICE_TYPE.MUNITION));
                        assertTrue(vendor.has(SERVICE_TYPE.OUTFIT));
                        assertFalse(vendor.has(SERVICE_TYPE.SHIPYARD));
                        assertTrue(vendor.has(SERVICE_TYPE.REFUEL));
                    }
                }
                assertEquals(1, j);
                assertTrue(place.canRefill());
            }
            if (place3.getName().equals(place.getName())) {
                assertPlace(place, place3);
                i++;
                assertTrue(place.get().isEmpty());
                assertFalse(place.canRefill());
            }

        }
        assertEquals(3, i);
        market.clear();
    }


    @After
    public void tearDown() throws Exception {
        store.close();
        deleteFolder(new File("test-bd"));
    }

    public static void deleteFolder(File folder) {
        File[] files = folder.listFiles();
        if(files!=null) { //some JVMs return null for empty dirs
            for(File f: files) {
                if(f.isDirectory()) {
                    deleteFolder(f);
                } else {
                    f.delete();
                }
            }
        }
        folder.delete();
    }
}
