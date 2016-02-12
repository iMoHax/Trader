package ru.trader.store;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import ru.trader.core.*;
import ru.trader.store.simple.SimpleMarket;
import ru.trader.store.simple.Store;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.time.LocalDateTime;
import java.util.Collection;

public class LoadTest extends Assert {
    private final static Logger LOG = LoggerFactory.getLogger(LoadTest.class);

    @Test
    public void testLoad(){
        LOG.info("Start world load test");
        InputStream is = getClass().getResourceAsStream("/test.xml");
        Market world;
        try {
            world = Store.loadFromFile(is);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new AssertionError(e);
        }
        assertNotNull(world);
    }

    private void assertGroup(Group group1, Group group2){
        assertEquals(group1.getName(), group2.getName());
        assertEquals(group1.getType(), group2.getType());
    }

    private void assertItem(Item item1, Item item2){
        assertEquals(item1.getName(), item2.getName());
        assertGroup(item1.getGroup(), item2.getGroup());
        assertEquals(item1.getIllegalFactions(), item2.getIllegalFactions());
        assertEquals(item1.getIllegalGovernments(), item2.getIllegalGovernments());
    }

    private void assertPlace(Place place1, Place place2){
        assertEquals(place1.getName(), place2.getName());
        assertEquals(place1.getX(), place2.getX(), 0.00001);
        assertEquals(place1.getY(), place2.getY(), 0.00001);
        assertEquals(place1.getZ(), place2.getZ(), 0.00001);
        assertEquals(place1.getFaction(), place2.getFaction());
        assertEquals(place1.getGovernment(), place2.getGovernment());
        assertEquals(place1.getPower(), place2.getPower());
        assertEquals(place1.getPowerState(), place2.getPowerState());
    }

    private void assertVendor(Vendor vendor1, Vendor vendor2){
        assertEquals(vendor1.getName(), vendor2.getName());
        assertEquals(vendor1.getType(), vendor2.getType());
        assertEquals(vendor1.getDistance(), vendor2.getDistance(), 0.00001);
        assertEquals(vendor1.getFaction(), vendor2.getFaction());
        assertEquals(vendor1.getGovernment(), vendor2.getGovernment());
        assertEquals(vendor1.getEconomic(), vendor2.getEconomic());
        assertEquals(vendor1.getSubEconomic(), vendor2.getSubEconomic());
        assertEquals(vendor1.getModifiedTime(), vendor2.getModifiedTime());
    }

    private void assertOffer(Offer offer1, Offer offer2){
        assertEquals(offer1.getType(), offer2.getType());
        assertItem(offer1.getItem(), offer2.getItem());
        assertEquals(offer1.getPrice(), offer2.getPrice(), 0.000001);
        assertEquals(offer1.getCount(), offer2.getCount());
    }

    @Test
    public void testSave(){
        LOG.info("Start world save test");

        Market market = new SimpleMarket();
        Group group1 = market.addGroup("Group 1", GROUP_TYPE.MARKET);
        Group group2 = market.addGroup("Group 2", GROUP_TYPE.MARKET);
        Group group3 = market.addGroup("Group 3", GROUP_TYPE.OUTFIT);
        Item item1 = market.addItem("Item 1", group1);
        Item item2 = market.addItem("Item 2", group1);
        Item item3 = market.addItem("Item 3", group2);
        Item item4 = market.addItem("Item 4", group2);
        Item item5 = market.addItem("Item 5", group3);
        item1.setIllegal(FACTION.FEDERATION, true);
        item2.setIllegal(FACTION.EMPIRE, true);
        item2.setIllegal(GOVERNMENT.DEMOCRACY, true);
        item3.setIllegal(GOVERNMENT.CORPORATE, true);
        Place place1 = market.addPlace("Place 1", 0, 1, 3);
        place1.setFaction(FACTION.ALLIANCE);
        place1.setGovernment(GOVERNMENT.PRISON_COLONY);
        place1.setPower(POWER.LAVIGNY_DUVAL, POWER_STATE.EXPLOITED);
        Place place2 = market.addPlace("Place 2",4,0,5);
        place2.setPower(POWER.DELAINE, POWER_STATE.CONTROL);
        Place place3 = market.addPlace("Place 3",0,0,0);
        Vendor vendor1 = place1.addVendor("Vendor 1");
        Vendor vendor2 = place1.addVendor("Vendor 2");
        Vendor vendor3 = place2.addVendor("Vendor 3");
        vendor1.setType(STATION_TYPE.CORIOLIS_STARPORT);
        vendor1.setDistance(10);
        vendor1.setFaction(FACTION.ALLIANCE);
        vendor1.setGovernment(GOVERNMENT.ANARCHY);
        vendor1.setEconomic(ECONOMIC_TYPE.EXTRACTION);
        vendor1.setSubEconomic(ECONOMIC_TYPE.REFINERY);
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
        vendor2.setGovernment(GOVERNMENT.NONE);
        vendor2.setEconomic(ECONOMIC_TYPE.HIGH_TECH);
        vendor2.setType(STATION_TYPE.PLANETARY_PORT);
        vendor3.setDistance(200000.4);
        vendor3.add(SERVICE_TYPE.OUTFIT);
        vendor3.add(SERVICE_TYPE.REFUEL);
        vendor3.setModifiedTime(LocalDateTime.of(2016,10,2,10,20,0));

        LOG.info("save market");
        File xml = new File("save_load_test.xml");
        try {
            Store.saveToFile(market, xml);
        } catch (FileNotFoundException | UnsupportedEncodingException | XMLStreamException e) {
            throw new AssertionError(e);
        }

        LOG.info("load world");
        Market world;
        try {
            world = Store.loadFromFile(xml);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new AssertionError(e);
        }
        assertNotNull(world);
        assertTrue(xml.delete());

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
    }

}
