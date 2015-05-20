package ru.trader.analysis;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.TestUtil;
import ru.trader.core.Vendor;
import ru.trader.store.simple.SimpleVendor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class RouteTest extends Assert {
    private final static Logger LOG = LoggerFactory.getLogger(RouteTest.class);

    private Vendor v1;
    private Vendor v2;
    private Vendor v3;
    private Vendor v4;

    @Test
    public void testVendors() throws Exception {
        LOG.info("Start test get vendors");
        v1 = new SimpleVendor("v1",0,0,0);
        v2 = new SimpleVendor("v2",0,0,0);
        v3 = new SimpleVendor("v3",0,0,0);
        v4 = new SimpleVendor("v4",0,0,0);

        Route path = new Route(new RouteEntry(v1, false, 0));
        path.add(new RouteEntry(v2, false, 0));
        path.add(new RouteEntry(v3, false, 0));
        TestUtil.assertCollectionContainAll(path.getVendors(), v1, v2, v3);
    }

    @Test
    public void testContains() throws Exception {
        LOG.info("Start test get entries");
        v1 = new SimpleVendor("v1",0,0,0);
        v2 = new SimpleVendor("v2",0,0,0);
        v3 = new SimpleVendor("v3",0,0,0);
        v4 = new SimpleVendor("v4",0,0,0);

        Route path = new Route(new RouteEntry(v1, false, 0));
        path.add(new RouteEntry(v2, false, 0));
        path.add(new RouteEntry(v3, false, 0));
        Collection<Vendor> vendors = new ArrayList<>();
        Collections.addAll(vendors, v1, v2, v3);
        assertTrue(path.contains(vendors));
        vendors.clear();
        Collections.addAll(vendors, v2);
        assertTrue(path.contains(vendors));
        vendors.clear();
        Collections.addAll(vendors, v4);
        assertFalse(path.contains(vendors));
        vendors.clear();
        Collections.addAll(vendors, v3, v2, v4, v1);
        assertFalse(path.contains(vendors));
        vendors.clear();
        Collections.addAll(vendors, v1, v2, v3, v4);
        assertFalse(path.contains(vendors));

    }

}
