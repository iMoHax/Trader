package ru.trader.emdn;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class EMDNTest extends Assert {
    private final static EMDN  markettool = new EMDN("tcp://localhost:9050", true);
    
    private final static EMDNEmul server = new EMDNEmul("tcp://localhost:9050");

    @Before
    public void setUp() throws Exception {
        server.start();
        markettool.start();
    }

    @Test
    public void testGetData() throws Exception {
        // wait submit
        Thread.sleep(4000);
        Station station = markettool.getVendor("Eranin");
        assertNotNull(station);
        ItemData itemData = station.getData("cropharvesters");
        assertNotNull(itemData);
        assertEquals(0,itemData.getBuy(), 0.0001);
        assertEquals(0,itemData.getStock());
        assertEquals(2318,itemData.getSell(), 0.0001);
        assertEquals(16472,itemData.getDemand());
    }

    @After
    public void tearDown() throws Exception {
        markettool.shutdown();
        server.shutdown();

    }
}
