package ru.trader.emdn;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EMDNTest extends Assert {
    private final static Logger LOG = LoggerFactory.getLogger(EMDNTest.class);

    private final static EMDN  markettool = new EMDN("tcp://localhost:9050", (m)->{LOG.debug("Receive message: {}", m);});
    
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
    }

    @After
    public void tearDown() throws Exception {
        markettool.shutdown();
        server.shutdown();

    }
}
