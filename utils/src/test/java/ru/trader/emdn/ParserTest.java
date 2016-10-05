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
            assertEquals("http://schemas.elite-markets.net/eddn/commodity/1", message.getSchemaRef());
            Header header = message.getHeader();
            assertNotNull(header);
            assertEquals("abcdef0123456789", header.getUploaderId());
            assertEquals("My Awesome Market Uploader", header.getSoftwareName());
            assertEquals("v3.14", header.getSoftwareVersion());
            assertEquals(LocalDateTime.of(2014, 11, 17, 13, 35), header.getGatewayTimestamp());
            Body body = message.getBody();
            assertNotNull(body);
            assertEquals(LocalDateTime.of(2014, 11, 17, 12, 34, 56), body.getTimestamp());
            StarSystem system = body.getSystem();
            assertNotNull(system);
            assertEquals("Eranin", system.getName());
            assertNull(system.getId());
            assertNull(system.getAddress());
            Station station = body.getStation();
            assertNotNull(station);
            assertEquals("Azeban Orbital", station.getName());
            assertNull(station.getId());
            Collection<Item> items = body.getCommodities();
            assertNotNull(items);
            assertEquals(1, items.size());
            Item item = items.iterator().next();
            assertNotNull(item);
            assertEquals("Gold", item.getName());
            assertNull(item.getId());
            assertEquals(1024, item.getBuyPrice());
            assertEquals(7, item.getSupply());
            assertEquals(LEVEL_TYPE.LOW, item.getSupplyLevel());
            assertEquals(1138, item.getSellPrice());
            assertEquals(42, item.getDemand());
            assertEquals(LEVEL_TYPE.MEDIUM, item.getDemandLevel());
        }
    }


    @Test
    public void testParseV2() throws Exception {
        EMDNParser parser = new EMDNParser();

        try (InputStream is = getClass().getResourceAsStream("/emdn/v2.json")) {
            String json = TestUtils.read(is);
            Message message = parser.parse(json);
            assertNotNull(message);
            assertEquals("http://schemas.elite-markets.net/eddn/commodity/2", message.getSchemaRef());
            Header header = message.getHeader();
            assertNotNull(header);
            assertEquals("abcdef0123456789", header.getUploaderId());
            assertEquals("My Awesome Market Uploader", header.getSoftwareName());
            assertEquals("v3.14", header.getSoftwareVersion());
            assertEquals(LocalDateTime.of(2014, 11, 17, 13, 35), header.getGatewayTimestamp());
            Body body = message.getBody();
            assertNotNull(body);
            assertEquals(LocalDateTime.of(2014, 11, 17, 12, 34, 56), body.getTimestamp());
            StarSystem system = body.getSystem();
            assertNotNull(system);
            assertEquals("Eranin", system.getName());
            assertNull(system.getId());
            assertNull(system.getAddress());
            Station station = body.getStation();
            assertNotNull(station);
            assertEquals("Azeban Orbital", station.getName());
            assertNull(station.getId());
            Collection<Item> items = body.getCommodities();
            assertNotNull(items);
            assertEquals(2, items.size());
            int found = 0;
            for (Item item : items) {
                assertNotNull(item);
                if ("Gold".equals(item.getName())){
                    found++;
                    assertNull(item.getId());
                    assertEquals(1024, item.getBuyPrice());
                    assertEquals(7, item.getSupply());
                    assertEquals(LEVEL_TYPE.LOW, item.getSupplyLevel());
                    assertEquals(1138, item.getSellPrice());
                    assertEquals(42, item.getDemand());
                    assertEquals(LEVEL_TYPE.MEDIUM, item.getDemandLevel());
                } else
                if ("Explosives".equals(item.getName())){
                    found++;
                    assertNull(item.getId());
                    assertEquals(999, item.getBuyPrice());
                    assertEquals(1500, item.getSupply());
                    assertEquals(LEVEL_TYPE.LOW, item.getSupplyLevel());
                    assertEquals(0, item.getSellPrice());
                    assertEquals(0, item.getDemand());
                    assertNull(item.getDemandLevel());
                }
            }
            assertEquals("Expected items not found", 2, found);
        }
    }

    @Test
    public void testParseV1Min() throws Exception {
        EMDNParser parser = new EMDNParser();

        try (InputStream is = getClass().getResourceAsStream("/emdn/v1m.json")) {
            String json = TestUtils.read(is);
            Message message = parser.parse(json);
            assertNotNull(message);
            assertEquals("http://schemas.elite-markets.net/eddn/commodity/1", message.getSchemaRef());
            Header header = message.getHeader();
            assertNotNull(header);
            assertEquals("abcdef0123456789", header.getUploaderId());
            assertEquals("My Awesome Market Uploader", header.getSoftwareName());
            assertEquals("v3.14", header.getSoftwareVersion());
            assertNull(header.getGatewayTimestamp());
            Body body = message.getBody();
            assertNotNull(body);
            assertEquals(LocalDateTime.of(2014, 11, 17, 12, 34, 56), body.getTimestamp());
            StarSystem system = body.getSystem();
            assertNotNull(system);
            assertEquals("Eranin", system.getName());
            assertNull(system.getId());
            assertNull(system.getAddress());
            Station station = body.getStation();
            assertNotNull(station);
            assertEquals("Azeban Orbital", station.getName());
            assertNull(station.getId());
            Collection<Item> items = body.getCommodities();
            assertNotNull(items);
            assertEquals(1, items.size());
            Item item = items.iterator().next();
            assertNotNull(item);
            assertEquals("Gold", item.getName());
            assertNull(item.getId());
            assertEquals(1024, item.getBuyPrice());
            assertEquals(7, item.getSupply());
            assertNull(item.getSupplyLevel());
            assertEquals(1138, item.getSellPrice());
            assertEquals(42, item.getDemand());
            assertNull(item.getDemandLevel());
        }
    }


    @Test
    public void testParseV2Min() throws Exception {
        EMDNParser parser = new EMDNParser();

        try (InputStream is = getClass().getResourceAsStream("/emdn/v2m.json")) {
            String json = TestUtils.read(is);
            Message message = parser.parse(json);
            assertNotNull(message);
            assertEquals("http://schemas.elite-markets.net/eddn/commodity/2", message.getSchemaRef());
            Header header = message.getHeader();
            assertNotNull(header);
            assertEquals("abcdef0123456789", header.getUploaderId());
            assertEquals("My Awesome Market Uploader", header.getSoftwareName());
            assertEquals("v3.14", header.getSoftwareVersion());
            assertNull(header.getGatewayTimestamp());
            Body body = message.getBody();
            assertNotNull(body);
            assertEquals(LocalDateTime.of(2014, 11, 17, 12, 34, 56), body.getTimestamp());
            StarSystem system = body.getSystem();
            assertNotNull(system);
            assertEquals("Eranin", system.getName());
            assertNull(system.getId());
            assertNull(system.getAddress());
            Station station = body.getStation();
            assertNotNull(station);
            assertEquals("Azeban Orbital", station.getName());
            assertNull(station.getId());
            Collection<Item> items = body.getCommodities();
            assertNotNull(items);
            assertEquals(1, items.size());
            for (Item item : items) {
                assertNotNull(item);
                assertEquals("Gold", item.getName());
                assertNull(item.getId());
                assertEquals(1024, item.getBuyPrice());
                assertEquals(7, item.getSupply());
                assertNull(item.getSupplyLevel());
                assertEquals(1138, item.getSellPrice());
                assertEquals(42, item.getDemand());
                assertNull(item.getDemandLevel());
            }
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
            assertNull(message);
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

}