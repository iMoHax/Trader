package ru.trader.maddavo;

import org.junit.Assert;
import org.junit.Test;
import ru.trader.core.*;
import ru.trader.store.simple.SimpleMarket;

import java.util.Collection;

public class StationImportTest  extends Assert {

    private static final String CSVStrings =
                    "'1 G. CAELI','Giacobini Base',0,'?','L','?','N','2016-01-08 16:48:42','Y','?','?','?','Y'\n" +
                    "'1 G. CAELI','Kandel Arsenal',3486,'Y','L','N','N','2016-01-03 17:48:47','Y','Y','Y','Y','Y'\n" +
                    "'1 G. CAELI','Smoot Gateway',4761,'N','L','Y','Y','2015-05-06 17:03:00','Y','Y','Y','Y','N'\n" +
                    "'1 GEMINORUM','Collins Settlement',2598,'Y','M','Y','N','2016-01-30 16:25:40','Y','Y','N','Y','N'\n" +
                    "'1 GEMINORUM','Zholobov Orbital',3430,'Y','M','N','N','2015-06-21 23:25:43','N','Y','Y','Y','N'\n" +
                    "'1 GEMINORUM','Seddon Silo',3441,'N','L','N','N','2015-01-01 00:01:00','Y','Y','Y','Y','Y'\n" +
                    "'1 HYDRAE','Chern Barracks',0,'?','?','Y','?','2016-02-29 15:54:00','?','?','?','?','?'\n" +
                    "'1 HYDRAE','Kepler''s Inheritance',0,'?','?','Y','?','2016-02-29 15:44:00','?','?','?','?','?'\n" +
                    "'1 HYDRAE','Greenleaf''s Progress',159,'N','L','N','N','2015-01-01 00:01:00','Y','Y','Y','Y','Y'\n" +
                    "'1 HYDRAE','Weitz''s Folly',159,'N','L','N','N','2015-01-01 00:01:00','Y','Y','Y','Y','Y'\n" +
                    "'1 HYDRAE','Hieb Orbital',160,'N','M','Y','N','2015-08-09 22:16:03','N','Y','Y','Y','N'\n" +
                    "'1 HYDRAE','Afanasyev Port',200,'N','M','Y','N','2015-10-10 16:16:04','N','Y','Y','Y','N'\n" +
                    "'1 HYDRAE','Littlewood Reach',277,'N','L','N','N','2015-01-01 00:01:00','Y','Y','Y','Y','Y'\n" +
                    "'1 HYDRAE','Hutton Port',279,'N','L','Y','Y','2015-06-30 07:34:00','Y','Y','Y','Y','N'\n" +
                    "'1 HYDRAE','Yurchikhin Port',369,'N','M','N','N','2015-06-21 23:11:54','N','N','Y','Y','N'\n" +
                    "'1 HYDRAE','Arber Terminal',664,'N','M','N','N','2015-06-21 23:11:54','N','N','Y','Y','N'\n" +
                    "'1 HYDRAE','Feustel Port',null,'?','-','N','Y','2015-10-10 16:16:05','?','?','?','?','?'\n" +
                    "'1 HYDRAE','Voss Hub',823,'Y','L','Y','Y','2015-10-10 16:16:05','Y','Y','Y','Y','N'\n" +
                    "'1 HYDRAE','Hornby Dock',1457,'N','L','Y','Y','2015-09-24 10:52:01','Y','Y','Y','Y','N'\n" +
                    "'1 HYDRAE','Whitney Station',2106,'N','M','N','N','2015-06-21 23:11:54','N','N','Y','Y','N'\n" +
                    "'1 I CENTAURI','Armstrong Ring',1093,'Y','L','Y','Y','2015-06-21 23:04:19','Y','Y','Y','Y','N'\n" +
                    "'1 I CENTAURI','Ampere Dock',1498,'Y','L','Y','Y','2015-05-01 04:33:33','Y','Y','Y','Y','N'\n" +
                    "'1 KAPPA CYGNI','Kinsey Ring',2359,'N','M','Y','N','2015-06-21 23:31:26','N','N','Y','Y','N'\n" +
                    "'1 KAPPA CYGNI','Wohler Port',3520,'Y','L','Y','Y','2015-06-21 23:31:26','Y','Y','Y','Y','N'\n" +
                    "'1 KAPPA CYGNI','Hauck Enterprise',4832,'Y','M','N','N','2015-06-21 23:31:26','N','Y','Y','Y','N'\n" +
                    "'10 ARIETIS','Archimedes Base',0,'N','M','Y','N','2015-10-10 16:16:05','N','N','Y','Y','N'\n" +
                    "'10 ARIETIS','Chadwick Station',0,'N','M','Y','N','2015-10-10 16:16:05','Y','N','Y','Y','N'\n" +
                    "'10 CANUM VENATICORUM','Litke Port',0,'?','M','Y','?','2015-12-26 19:22:38','Y','?','?','?','?'\n" +
                    "'10 CANUM VENATICORUM','Collins Port',352,'N','M','Y','N','2016-03-05 22:28:07','N','N','Y','Y','N'\n" +
                    "'10 CANUM VENATICORUM','He Port',495,'N','L','Y','Y','2015-10-10 16:16:05','Y','Y','Y','Y','N'\n" +
                    "'10 CANUM VENATICORUM','Trevithick Hub',957,'N','L','N','N','2015-06-21 23:31:06','?','?','?','?','Y'\n" +
                    "'10 CANUM VENATICORUM','Alvares Bastion',1353,'N','L','Y','N','2016-02-21 22:06:30','Y','N','N','N','Y'\n" +
                    "'10 CANUM VENATICORUM','Godwin Installation',1353,'N','L','N','N','2015-01-01 00:01:00','Y','Y','Y','Y','Y'\n" +
                    "'10 DELTA CORONAE BOREALIS','Ramaswamy Port',0,'N','M','Y','N','2015-10-10 16:16:05','Y','N','Y','Y','N'\n" +
                    "'10 G. CANIS MAJORIS','Babbage Gateway',195,'N','M','Y','N','2015-06-21 23:10:38','N','N','Y','Y','N'\n" +
                    "'10 G. CANIS MAJORIS','Gohar City',359,'N','M','Y','N','2015-06-21 23:10:38','N','N','Y','N','N'\n" +
                    "'10 G. CANIS MAJORIS','Precourt Enterprise',614,'N','M','Y','N','2015-06-21 23:10:38','N','N','Y','Y','N'\n" +
                    "'10 G. CANIS MAJORIS','Rushd Enterprise',1078,'N','L','Y','Y','2015-05-02 02:00:12','Y','Y','Y','Y','N'\n" +
                    "'10 G. CANIS MAJORIS','Vesalius Port',1080,'Y','L','Y','Y','2015-05-02 02:00:12','Y','Y','Y','Y','N'";

    private static final String[] lines = CSVStrings.split("\\n");

    private Market createMarket(){
        Market market = new SimpleMarket();
        market.addPlace("1 Hydrae", 0, 0, 0);
        market.addPlace("1 Kappa Cygni", 0, 0, 0);
        Place system = market.addPlace("10 CANUM VENATICORUM", 0, 0, 0);
        system.setFaction(FACTION.FEDERATION);
        system.setGovernment(GOVERNMENT.PRISON_COLONY);

        Vendor station = system.addVendor("Trevithick Hub");
        station.setType(STATION_TYPE.OUTPOST);
        station.setDistance(2000);
        station.add(SERVICE_TYPE.MARKET);
        station.add(SERVICE_TYPE.BLACK_MARKET);
        station.add(SERVICE_TYPE.OUTFIT);

        station.setFaction(FACTION.ALLIANCE);
        station.setGovernment(GOVERNMENT.COMMUNISM);

        station = system.addVendor("Litke Port");
        station.setDistance(2000);
        station.add(SERVICE_TYPE.BLACK_MARKET);
        station.add(SERVICE_TYPE.OUTFIT);
        station.add(SERVICE_TYPE.SHIPYARD);
        station.setFaction(FACTION.EMPIRE);
        station.setGovernment(GOVERNMENT.CONFEDERACY);


        system = market.addPlace("Test sys", 0, 0, 0);
        system.addVendor("Test stat");

        return market;
    }

    @Test
    public void testImport() throws Exception {
        Market market = createMarket();
        StationHandler handler = new StationHandler(market);
        for (String line : lines) {
            handler.parse(line);
        }

        Collection<Vendor> vendors = market.getVendors();
        assertEquals("Vendors count distinct", 24, vendors.size());

        Vendor station =  market.get("1 Hydrae").get("Hieb Orbital");
        assertNotNull(station);
        assertEquals(STATION_TYPE.OUTPOST, station.getType());
        assertEquals(160, station.getDistance(), 0.00001);
        assertFalse(station.has(SERVICE_TYPE.BLACK_MARKET));
        assertTrue(station.has(SERVICE_TYPE.MARKET));
        assertFalse(station.has(SERVICE_TYPE.SHIPYARD));
        assertFalse(station.has(SERVICE_TYPE.OUTFIT));
        assertTrue(station.has(SERVICE_TYPE.MUNITION));
        assertTrue(station.has(SERVICE_TYPE.REFUEL));
        assertTrue(station.has(SERVICE_TYPE.REPAIR));

        station =  market.get("1 Hydrae").get("Voss Hub");
        assertNotNull(station);
        assertEquals(STATION_TYPE.STARPORT, station.getType());
        assertNull(station.getFaction());
        assertNull(station.getGovernment());
        assertEquals(823, station.getDistance(), 0.00001);
        assertTrue(station.has(SERVICE_TYPE.BLACK_MARKET));
        assertTrue(station.has(SERVICE_TYPE.MARKET));
        assertTrue(station.has(SERVICE_TYPE.SHIPYARD));
        assertTrue(station.has(SERVICE_TYPE.OUTFIT));
        assertTrue(station.has(SERVICE_TYPE.MUNITION));
        assertTrue(station.has(SERVICE_TYPE.REFUEL));
        assertTrue(station.has(SERVICE_TYPE.REPAIR));


        station =  market.get("1 Hydrae").get("Feustel Port");
        assertNotNull(station);
        assertEquals(0, station.getDistance(), 0.00001);
        assertFalse(station.has(SERVICE_TYPE.BLACK_MARKET));
        assertFalse(station.has(SERVICE_TYPE.MARKET));
        assertTrue(station.has(SERVICE_TYPE.SHIPYARD));
        assertFalse(station.has(SERVICE_TYPE.OUTFIT));
        assertFalse(station.has(SERVICE_TYPE.MUNITION));
        assertFalse(station.has(SERVICE_TYPE.REFUEL));
        assertFalse(station.has(SERVICE_TYPE.REPAIR));

        Place place = market.get("10 CANUM VENATICORUM");
        assertNotNull(place);
        assertEquals(FACTION.FEDERATION, place.getFaction());
        assertEquals(GOVERNMENT.PRISON_COLONY, place.getGovernment());

        station = place.get("Trevithick Hub");
        assertNotNull(station);
        assertEquals(STATION_TYPE.PLANETARY_OUTPOST, station.getType());
        assertEquals(FACTION.ALLIANCE, station.getFaction());
        assertEquals(GOVERNMENT.COMMUNISM, station.getGovernment());
        assertEquals(957, station.getDistance(), 0.00001);
        assertFalse(station.has(SERVICE_TYPE.BLACK_MARKET));
        assertFalse(station.has(SERVICE_TYPE.MARKET));
        assertFalse(station.has(SERVICE_TYPE.SHIPYARD));
        assertTrue(station.has(SERVICE_TYPE.OUTFIT));
        assertFalse(station.has(SERVICE_TYPE.MUNITION));
        assertFalse(station.has(SERVICE_TYPE.REFUEL));
        assertFalse(station.has(SERVICE_TYPE.REPAIR));

        station =  market.get("10 CANUM VENATICORUM").get("Litke Port");
        assertNotNull(station);
        assertEquals(STATION_TYPE.OUTPOST, station.getType());
        assertEquals(FACTION.EMPIRE, station.getFaction());
        assertEquals(GOVERNMENT.CONFEDERACY, station.getGovernment());
        assertEquals(2000, station.getDistance(), 0.00001);
        assertTrue(station.has(SERVICE_TYPE.BLACK_MARKET));
        assertTrue(station.has(SERVICE_TYPE.MARKET));
        assertTrue(station.has(SERVICE_TYPE.SHIPYARD));
        assertTrue(station.has(SERVICE_TYPE.OUTFIT));
        assertFalse(station.has(SERVICE_TYPE.MUNITION));
        assertFalse(station.has(SERVICE_TYPE.REFUEL));
        assertFalse(station.has(SERVICE_TYPE.REPAIR));

    }
}
