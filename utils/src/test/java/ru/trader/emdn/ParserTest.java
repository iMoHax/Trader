package ru.trader.emdn;

import com.fasterxml.jackson.core.JsonParseException;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.TestUtils;
import ru.trader.emdn.entities.*;
import ru.trader.store.imp.ImportDataError;
import ru.trader.store.imp.entities.ItemData;
import ru.trader.store.imp.entities.ShipData;
import ru.trader.store.imp.entities.StarSystemData;
import ru.trader.store.imp.entities.StationData;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Collection;

public class ParserTest extends Assert {
    private final static Logger LOG = LoggerFactory.getLogger(ParserTest.class);

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void testParseV1() throws Exception {
        EMDNParser parser = new EMDNParser();

        try (InputStream is = getClass().getResourceAsStream("/emdn/v1.json")) {
            String json = TestUtils.read(is);
            Message message = parser.parse(json);
            assertNotNull(message);
            assertEquals(SUPPORT_VERSIONS.V1, message.getVersion());
            Header header = message.getHeader();
            assertNotNull(header);
            assertEquals("abcdef0123456789", header.getUploaderId());
            assertEquals("My Awesome Market Uploader", header.getSoftwareName());
            assertEquals("v3.14", header.getSoftwareVersion());
            assertEquals(LocalDateTime.of(2014, 11, 17, 13, 35), header.getGatewayTimestamp());
            StarSystemData data = message.getImportData();
            assertNotNull(data);
            assertEquals("Eranin", data.getName());
            Collection<StationData> stations = data.getStations();
            assertNotNull(stations);
            assertEquals(1, stations.size());
            StationData station = stations.iterator().next();
            assertNotNull(station);
            assertEquals("Azeban Orbital", station.getName());
            assertEquals(LocalDateTime.of(2014, 11, 17, 12, 34, 56), station.getModifiedTime());
            Collection<ItemData> items = station.getCommodities();
            assertNotNull(items);
            assertEquals(1, items.size());
            ItemData item = items.iterator().next();
            assertNotNull(item);
            assertEquals("Gold", item.getName());
            assertEquals(1024, item.getSellOfferPrice());
            assertEquals(7, item.getSupply());
            assertEquals(1138, item.getBuyOfferPrice());
            assertEquals(42, item.getDemand());

            assertNull(data.getId());
            assertNull(data.getFaction());
            assertNull(data.getGovernment());
            assertNull(data.getPower());
            assertNull(data.getPowerState());
            assertNull(station.getId());
            assertNull(station.getType());
            assertNull(station.getFaction());
            assertNull(station.getGovernment());
            assertNull(station.getEconomic());
            assertNull(station.getSubEconomic());
            assertNull(station.getServices());
            assertNull(station.getShips());
            assertNull(station.getModules());
            assertNull(item.getId());
            assertNull(item.getGroup());
        }
    }


    @Test
    public void testParseV2() throws Exception {
        EMDNParser parser = new EMDNParser();

        try (InputStream is = getClass().getResourceAsStream("/emdn/v2.json")) {
            String json = TestUtils.read(is);
            Message message = parser.parse(json);
            assertNotNull(message);
            assertEquals(SUPPORT_VERSIONS.V2, message.getVersion());
            Header header = message.getHeader();
            assertNotNull(header);
            assertEquals("abcdef0123456789", header.getUploaderId());
            assertEquals("My Awesome Market Uploader", header.getSoftwareName());
            assertEquals("v3.14", header.getSoftwareVersion());
            assertEquals(LocalDateTime.of(2014, 11, 17, 13, 35), header.getGatewayTimestamp());
            StarSystemData data = message.getImportData();
            assertNotNull(data);
            assertEquals("Eranin", data.getName());
            Collection<StationData> stations = data.getStations();
            assertNotNull(stations);
            assertEquals(1, stations.size());
            StationData station = stations.iterator().next();
            assertNotNull(station);
            assertEquals("Azeban Orbital", station.getName());
            assertEquals(LocalDateTime.of(2014, 11, 17, 12, 34, 56), station.getModifiedTime());
            Collection<ItemData> items = station.getCommodities();
            assertNotNull(items);
            assertEquals(2, items.size());
            int found = 0;
            for (ItemData item : items) {
                assertNotNull(item);
                if ("Gold".equals(item.getName())){
                    found++;
                    assertEquals(1024, item.getSellOfferPrice());
                    assertEquals(7, item.getSupply());
                    assertEquals(1138, item.getBuyOfferPrice());
                    assertEquals(42, item.getDemand());
                    assertNull(item.getId());
                    assertNull(item.getGroup());
                } else
                if ("Explosives".equals(item.getName())){
                    found++;
                    assertEquals(999, item.getSellOfferPrice());
                    assertEquals(1500, item.getSupply());
                    assertEquals(0, item.getBuyOfferPrice());
                    assertEquals(0, item.getDemand());
                    assertNull(item.getId());
                    assertNull(item.getGroup());
                }
            }
            assertEquals("Expected items not found", 2, found);

            assertNull(data.getId());
            assertNull(data.getFaction());
            assertNull(data.getGovernment());
            assertNull(data.getPower());
            assertNull(data.getPowerState());
            assertNull(station.getId());
            assertNull(station.getType());
            assertNull(station.getFaction());
            assertNull(station.getGovernment());
            assertNull(station.getEconomic());
            assertNull(station.getSubEconomic());
            assertNull(station.getServices());
            assertNull(station.getShips());
            assertNull(station.getModules());
        }
    }

    @Test
    public void testParseV1Min() throws Exception {
        EMDNParser parser = new EMDNParser();

        try (InputStream is = getClass().getResourceAsStream("/emdn/v1m.json")) {
            String json = TestUtils.read(is);
            Message message = parser.parse(json);
            assertNotNull(message);
            assertEquals(SUPPORT_VERSIONS.V1, message.getVersion());
            Header header = message.getHeader();
            assertNotNull(header);
            assertEquals("abcdef0123456789", header.getUploaderId());
            assertEquals("My Awesome Market Uploader", header.getSoftwareName());
            assertEquals("v3.14", header.getSoftwareVersion());
            assertNull(header.getGatewayTimestamp());
            StarSystemData data = message.getImportData();
            assertNotNull(data);
            assertEquals("Eranin", data.getName());
            Collection<StationData> stations = data.getStations();
            assertNotNull(stations);
            assertEquals(1, stations.size());
            StationData station = stations.iterator().next();
            assertNotNull(station);
            assertEquals("Azeban Orbital", station.getName());
            assertEquals(LocalDateTime.of(2014, 11, 17, 12, 34, 56), station.getModifiedTime());
            Collection<ItemData> items = station.getCommodities();
            assertNotNull(items);
            assertEquals(1, items.size());
            ItemData item = items.iterator().next();
            assertNotNull(item);
            assertEquals("Gold", item.getName());
            assertNull(item.getId());
            assertEquals(1024, item.getSellOfferPrice());
            assertEquals(7, item.getSupply());
            assertEquals(1138, item.getBuyOfferPrice());
            assertEquals(42, item.getDemand());

            assertNull(data.getId());
            assertNull(data.getFaction());
            assertNull(data.getGovernment());
            assertNull(data.getPower());
            assertNull(data.getPowerState());
            assertNull(station.getId());
            assertNull(station.getType());
            assertNull(station.getFaction());
            assertNull(station.getGovernment());
            assertNull(station.getEconomic());
            assertNull(station.getSubEconomic());
            assertNull(station.getServices());
            assertNull(station.getShips());
            assertNull(station.getModules());
            assertNull(item.getId());
            assertNull(item.getGroup());
        }
    }


    @Test
    public void testParseV2Min() throws Exception {
        EMDNParser parser = new EMDNParser();

        try (InputStream is = getClass().getResourceAsStream("/emdn/v2m.json")) {
            String json = TestUtils.read(is);
            Message message = parser.parse(json);
            assertNotNull(message);
            assertEquals(SUPPORT_VERSIONS.V2, message.getVersion());
            Header header = message.getHeader();
            assertNotNull(header);
            assertEquals("abcdef0123456789", header.getUploaderId());
            assertEquals("My Awesome Market Uploader", header.getSoftwareName());
            assertEquals("v3.14", header.getSoftwareVersion());
            assertNull(header.getGatewayTimestamp());
            StarSystemData data = message.getImportData();
            assertNotNull(data);
            assertEquals("Eranin", data.getName());
            Collection<StationData> stations = data.getStations();
            assertNotNull(stations);
            assertEquals(1, stations.size());
            StationData station = stations.iterator().next();
            assertNotNull(station);
            assertEquals("Azeban Orbital", station.getName());
            assertEquals(LocalDateTime.of(2014, 11, 17, 12, 34, 56), station.getModifiedTime());
            Collection<ItemData> items = station.getCommodities();
            assertNotNull(items);
            assertEquals(1, items.size());
            for (ItemData item : items) {
                assertNotNull(item);
                assertEquals("Gold", item.getName());
                assertNull(item.getId());
                assertEquals(1024, item.getSellOfferPrice());
                assertEquals(7, item.getSupply());
                assertEquals(1138, item.getBuyOfferPrice());
                assertEquals(42, item.getDemand());
                assertNull(item.getId());
                assertNull(item.getGroup());
            }

            assertNull(data.getId());
            assertNull(data.getFaction());
            assertNull(data.getGovernment());
            assertNull(data.getPower());
            assertNull(data.getPowerState());
            assertNull(station.getId());
            assertNull(station.getType());
            assertNull(station.getFaction());
            assertNull(station.getGovernment());
            assertNull(station.getEconomic());
            assertNull(station.getSubEconomic());
            assertNull(station.getServices());
            assertNull(station.getShips());
            assertNull(station.getModules());
        }
    }

    @Test
    public void testParseV1Error() throws Exception {
        EMDNParser parser = new EMDNParser();
        try (InputStream is = getClass().getResourceAsStream("/emdn/v1e.json")) {
            String json = TestUtils.read(is);
            Message message = parser.parse(json);
            assertNull(message);
        }
    }

    @Test
    public void testParseV2Error() throws Exception {
        EMDNParser parser = new EMDNParser();
        try (InputStream is = getClass().getResourceAsStream("/emdn/v2e.json")) {
            String json = TestUtils.read(is);
            Message message = parser.parse(json);
            assertNotNull(message);
            StarSystemData data = message.getImportData();
            assertNotNull(data);
            assertEquals("Eranin", data.getName());
            Collection<StationData> stations = data.getStations();
            assertNotNull(stations);
            assertEquals(1, stations.size());
            StationData station = stations.iterator().next();
            assertNotNull(station);
            assertEquals("Azeban Orbital", station.getName());
            exception.expect(ImportDataError.class);
            station.getCommodities();
        }
    }

    @Test
    public void testParseError() throws Exception {
        EMDNParser parser = new EMDNParser();
        String json = "Eranin sdfe adsf";

        exception.expect(JsonParseException.class);
        parser.parse(json);
    }

    @Test
    public void testParseError2() throws Exception {
        EMDNParser parser = new EMDNParser();


        String json = "{\n" +
                "    \"$schemaRef\": \"http://schemas.elite-markets.net/eddn/commodity/2\",\n" +
                "    \"header\": {\n" +
                "        \"uploaderID\": \"abcdef0123456789\",\n" +
                "        \"softwareName\": \"My Aweso";

        exception.expect(JsonParseException.class);
        parser.parse(json);

    }

    @Test
    public void testParseShipV1() throws Exception {
        EMDNParser parser = new EMDNParser();

        try (InputStream is = getClass().getResourceAsStream("/emdn/ship_v1.json")) {
            String json = TestUtils.read(is);
            Message message = parser.parse(json);
            assertNotNull(message);
            assertEquals(SUPPORT_VERSIONS.V1_SHIPYARD, message.getVersion());
            Header header = message.getHeader();
            assertNotNull(header);
            assertEquals("Marek Ce'ex", header.getUploaderId());
            assertEquals("E:D Market Connector [Mac OS]", header.getSoftwareName());
            assertEquals("2.1.6.1", header.getSoftwareVersion());
            assertEquals(LocalDateTime.of(2016, 10, 5, 13, 53, 33, 930428000), header.getGatewayTimestamp());
            StarSystemData data = message.getImportData();
            assertNotNull(data);
            assertEquals("Venegana", data.getName());
            Collection<StationData> stations = data.getStations();
            assertNotNull(stations);
            assertEquals(1, stations.size());
            StationData station = stations.iterator().next();
            assertNotNull(station);
            assertEquals("Shull Ring", station.getName());
            assertEquals(LocalDateTime.of(2016, 10, 5, 13, 53, 25), station.getModifiedTime());
            Collection<ShipData> ships = station.getShips();
            assertNotNull(ships);
            assertEquals(8, ships.size());
            int found = 0;
            for (ShipData ship : ships) {
                assertNotNull(ship);
                if ("Sidewinder".equals(ship.getName())){
                    found++;
                    assertNull(ship.getId());
                    assertNull(ship.getPrice());
                } else
                if ("Type-7 Transporter".equals(ship.getName())){
                    found++;
                    assertNull(ship.getId());
                    assertNull(ship.getPrice());
                }
            }
            assertEquals("Expected ships not found", 2, found);

            assertNull(data.getId());
            assertNull(data.getFaction());
            assertNull(data.getGovernment());
            assertNull(data.getPower());
            assertNull(data.getPowerState());
            assertNull(station.getId());
            assertNull(station.getType());
            assertNull(station.getFaction());
            assertNull(station.getGovernment());
            assertNull(station.getEconomic());
            assertNull(station.getSubEconomic());
            assertNull(station.getServices());
            assertNull(station.getCommodities());
            assertNull(station.getModules());
        }
    }


    @Test
    public void testParseShipV2() throws Exception {
        EMDNParser parser = new EMDNParser();

        try (InputStream is = getClass().getResourceAsStream("/emdn/ship_v2.json")) {
            String json = TestUtils.read(is);
            Message message = parser.parse(json);
            assertNotNull(message);
            assertEquals(SUPPORT_VERSIONS.V2_SHIPYARD, message.getVersion());
            Header header = message.getHeader();
            assertNotNull(header);
            assertEquals("Amadeus Sheperd", header.getUploaderId());
            assertEquals("E:D Market Connector [Windows]", header.getSoftwareName());
            assertEquals("2.1.7.2", header.getSoftwareVersion());
            assertEquals(LocalDateTime.of(2016, 10, 5, 13, 16, 12, 490637000), header.getGatewayTimestamp());
            StarSystemData data = message.getImportData();
            assertNotNull(data);
            assertEquals("Sothis", data.getName());
            assertNull(data.getId());
            Collection<StationData> stations = data.getStations();
            assertNotNull(stations);
            assertEquals(1, stations.size());
            StationData station = stations.iterator().next();
            assertNotNull(station);
            assertEquals("Newholm Station", station.getName());
            assertNull(station.getId());
            assertEquals(LocalDateTime.of(2016, 10, 5, 13, 15, 51), station.getModifiedTime());
            Collection<ShipData> ships = station.getShips();
            assertNotNull(ships);
            assertEquals(5, ships.size());
            int found = 0;
            for (ShipData ship : ships) {
                assertNotNull(ship);
                if ("SideWinder".equals(ship.getName())){
                    found++;
                    assertNull(ship.getId());
                    assertNull(ship.getPrice());
                } else
                if ("Type7".equals(ship.getName())){
                    found++;
                    assertNull(ship.getId());
                    assertNull(ship.getPrice());
                }
            }
            assertEquals("Expected ships not found", 2, found);

            assertNull(data.getId());
            assertNull(data.getFaction());
            assertNull(data.getGovernment());
            assertNull(data.getPower());
            assertNull(data.getPowerState());
            assertNull(station.getId());
            assertNull(station.getType());
            assertNull(station.getFaction());
            assertNull(station.getGovernment());
            assertNull(station.getEconomic());
            assertNull(station.getSubEconomic());
            assertNull(station.getServices());
            assertNull(station.getCommodities());
            assertNull(station.getModules());
        }
    }


    @Test
    public void testParseV3() throws Exception {
        EMDNParser parser = new EMDNParser();

        try (InputStream is = getClass().getResourceAsStream("/emdn/v3.json")) {
            String json = TestUtils.read(is);
            Message message = parser.parse(json);
            assertNotNull(message);
            assertEquals(SUPPORT_VERSIONS.V3, message.getVersion());
            Header header = message.getHeader();
            assertNotNull(header);
            assertEquals("Gorthok", header.getUploaderId());
            assertEquals("E:D Market Connector [Windows]", header.getSoftwareName());
            assertEquals("2.1.7.2", header.getSoftwareVersion());
            assertEquals(LocalDateTime.of(2016, 10, 7, 12, 40, 4, 453634000), header.getGatewayTimestamp());
            StarSystemData data = message.getImportData();
            assertNotNull(data);
            assertEquals("LP 30-55", data.getName());
            Collection<StationData> stations = data.getStations();
            assertNotNull(stations);
            assertEquals(1, stations.size());
            StationData station = stations.iterator().next();
            assertNotNull(station);
            assertEquals("Crown Platform", station.getName());
            assertEquals(LocalDateTime.of(2016, 10, 7, 12, 40, 3), station.getModifiedTime());
            Collection<ItemData> items = station.getCommodities();
            assertNotNull(items);
            assertEquals(70, items.size());
            int found = 0;
            for (ItemData item : items) {
                assertNotNull(item);
                if ("Insulating Membrane".equals(item.getName())){
                    found++;
                    assertEquals(7234, item.getSellOfferPrice());
                    assertEquals(24, item.getSupply());
                    assertEquals(7156, item.getBuyOfferPrice());
                    assertEquals(1, item.getDemand());
                    assertNull(item.getId());
                    assertNull(item.getGroup());
                } else
                if ("Rutile".equals(item.getName())){
                    found++;
                    assertEquals(0, item.getSellOfferPrice());
                    assertEquals(0, item.getSupply());
                    assertEquals(328, item.getBuyOfferPrice());
                    assertEquals(11005, item.getDemand());
                    assertNull(item.getId());
                    assertNull(item.getGroup());
                }
            }
            assertEquals("Expected items not found", 2, found);

            assertNull(data.getId());
            assertNull(data.getFaction());
            assertNull(data.getGovernment());
            assertNull(data.getPower());
            assertNull(data.getPowerState());
            assertNull(station.getId());
            assertNull(station.getType());
            assertNull(station.getFaction());
            assertNull(station.getGovernment());
            assertNull(station.getEconomic());
            assertNull(station.getSubEconomic());
            assertNull(station.getServices());
            assertNull(station.getShips());
            assertNull(station.getModules());
        }
    }

}
