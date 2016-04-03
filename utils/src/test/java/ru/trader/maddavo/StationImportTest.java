package ru.trader.maddavo;

import org.junit.Assert;
import org.junit.Test;
import ru.trader.core.*;
import ru.trader.store.simple.SimpleMarket;

import java.util.Collection;

public class StationImportTest  extends Assert {

    private static final String CSVStrings =
            "'1 GEMINORUM','Collins Settlement',2585,'?','M','Y','N','2015-02-27 04:50:41'\n" +
            "'1 GEMINORUM','Zholobov Orbital',3428,'?','M','N','N','2015-02-27 04:51:21'\n" +
            "'1 HYDRAE','Arber Terminal',0,'?','M','N','N','2015-02-27 04:54:28'\n" +
            "'1 HYDRAE','Feustel Port',null,'?','-','N','Y','2015-02-27 04:54:46'\n" +
            "'1 HYDRAE','Hieb Orbital',160,'?','M','Y','N','2015-02-27 04:52:17'\n" +
            "'1 HYDRAE','Afanasyev Port',200,'?','M','Y','N','2015-02-27 04:52:55'\n" +
            "'1 HYDRAE','Hutton Port',279,'?','L','Y','Y','2015-02-27 04:53:31'\n" +
            "'1 HYDRAE','Yurchikhin Port',369,'?','M','N','N','2015-02-27 04:54:08'\n" +
            "'1 HYDRAE','Voss Hub',823,'Y','L','Y','Y','2015-02-27 04:55:18'\n" +
            "'1 HYDRAE','Hornby Dock',1457,'?','L','Y','Y','2015-02-27 04:55:56'\n" +
            "'1 HYDRAE','Whitney Station',2106,'?','M','N','N','2015-02-27 04:56:35'\n" +
            "'1 I CENTAURI','Armstrong Ring',1093,'?','L','Y','Y','2015-02-27 04:57:33'\n" +
            "'1 I CENTAURI','Ampere Dock',1498,'Y','L','Y','Y','2015-02-25 01:31:54'\n" +
            "'1 KAPPA CYGNI','Hauck Enterprise',0,'?','M','?','?','2015-02-23 23:54:35'\n" +
            "'1 KAPPA CYGNI','Kinsey Ring',0,'?','M','Y','?','2015-02-24 14:31:52'\n" +
            "'1 KAPPA CYGNI','Wohler Port',0,'?','L','?','?','2015-02-23 23:54:35'\n" +
            "'10 CANUM VENATICORUM','Litke Port',0,'?','M','Y','?','2015-02-23 23:54:35'\n" +
            "'10 CANUM VENATICORUM','Collins Port',16,'?','M','Y','?','2015-02-23 23:54:35'\n" +
            "'10 CANUM VENATICORUM','He Port',495,'?','L','Y','?','2015-02-23 23:54:35'\n" +
            "'10 CANUM VENATICORUM','Trevithick Hub',957,'N','L','N','N','2015-02-25 01:31:54'\n" +
            "'10 CANUM VENATICORUM','Spedding Park',1369,'?','M','?','?','2015-02-23 23:54:35'\n" +
            "'10 CANUM VENATICORUM','Creamer Hub',1812,'?','M','?','?','2015-02-23 23:54:35'\n" +
            "'10 G. CANIS MAJORIS','Rushd Enterprise',0,'?','L','Y','Y','2015-02-27 05:01:06'";

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
        assertEquals("Vendors count distinct", 19, vendors.size());

        Vendor station =  market.get("1 Hydrae").get("Hieb Orbital");
        assertNotNull(station);
        assertEquals(STATION_TYPE.OUTPOST, station.getType());
        assertEquals(160, station.getDistance(), 0.00001);
        assertFalse(station.has(SERVICE_TYPE.BLACK_MARKET));
        assertTrue(station.has(SERVICE_TYPE.MARKET));
        assertFalse(station.has(SERVICE_TYPE.SHIPYARD));
        assertFalse(station.has(SERVICE_TYPE.OUTFIT));
        assertFalse(station.has(SERVICE_TYPE.MUNITION));

        station =  market.get("1 Hydrae").get("Voss Hub");
        assertNotNull(station);
        assertEquals(STATION_TYPE.STARPORT, station.getType());
        assertNull(station.getFaction());
        assertNull(station.getGovernment());
        assertEquals(823, station.getDistance(), 0.00001);
        assertTrue(station.has(SERVICE_TYPE.BLACK_MARKET));
        assertTrue(station.has(SERVICE_TYPE.MARKET));
        assertTrue(station.has(SERVICE_TYPE.SHIPYARD));
        assertFalse(station.has(SERVICE_TYPE.OUTFIT));
        assertFalse(station.has(SERVICE_TYPE.MUNITION));

        station =  market.get("1 Hydrae").get("Feustel Port");
        assertNotNull(station);
        assertEquals(0, station.getDistance(), 0.00001);
        assertFalse(station.has(SERVICE_TYPE.BLACK_MARKET));
        assertFalse(station.has(SERVICE_TYPE.MARKET));
        assertTrue(station.has(SERVICE_TYPE.SHIPYARD));
        assertFalse(station.has(SERVICE_TYPE.OUTFIT));
        assertFalse(station.has(SERVICE_TYPE.MUNITION));

        Place place = market.get("10 CANUM VENATICORUM");
        assertNotNull(place);
        assertEquals(FACTION.FEDERATION, place.getFaction());
        assertEquals(GOVERNMENT.PRISON_COLONY, place.getGovernment());

        station = place.get("Trevithick Hub");
        assertNotNull(station);
        assertEquals(STATION_TYPE.STARPORT, station.getType());
        assertEquals(FACTION.ALLIANCE, station.getFaction());
        assertEquals(GOVERNMENT.COMMUNISM, station.getGovernment());
        assertEquals(957, station.getDistance(), 0.00001);
        assertFalse(station.has(SERVICE_TYPE.BLACK_MARKET));
        assertFalse(station.has(SERVICE_TYPE.MARKET));
        assertFalse(station.has(SERVICE_TYPE.SHIPYARD));
        assertTrue(station.has(SERVICE_TYPE.OUTFIT));
        assertFalse(station.has(SERVICE_TYPE.MUNITION));

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

    }
}
