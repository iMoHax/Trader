package ru.trader.emdn;

import org.junit.Assert;
import org.junit.Test;

public class EMDNTest extends Assert {
    private final static EMDN  markettool = new EMDN("tcp://firehose.elite-market-data.net:9050");

    @Test
    public void testGetData() throws Exception {
        markettool.getData();

    }
}
