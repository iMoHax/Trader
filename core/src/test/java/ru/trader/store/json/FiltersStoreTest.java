package ru.trader.store.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import ru.trader.core.*;
import ru.trader.store.simple.Store;

import java.io.File;
import java.io.InputStream;
import java.util.Map;

public class FiltersStoreTest extends Assert {
    private static final File file = new File("filters-test.json");
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private Market world;
    private Place ithaca;
    private Place lhs3262;
    private Place morgor;
    private Place lhs3006;
    private Item gold;
    private Item tea;

    @Before
    public void setUp() throws Exception {
        InputStream is = getClass().getResourceAsStream("/world.xml");
        world = Store.loadFromFile(is);
        gold = world.getItem("gold");
        tea = world.getItem("tea");
        ithaca = world.get("Ithaca");
        lhs3262 = world.get("LHS 3262");
        morgor = world.get("Morgor");
        lhs3006 = world.get("LHS 3006");
    }

    private MarketFilter createFilter(){
        Vendor ithaca_st = ithaca.get().iterator().next();
        Vendor lhs3262_st = lhs3262.get().iterator().next();
        Vendor lhs3006_st = lhs3006.get().iterator().next();
        Vendor morgor_st = morgor.get().iterator().next();
        MarketFilter filter = new MarketFilter();
        filter.setCenter(ithaca);
        filter.setRadius(15.22);
        filter.setDistance(2456.44);
        filter.add(STATION_TYPE.CORIOLIS_STARPORT);
        filter.add(STATION_TYPE.ORBIS_STARPORT);
        filter.add(SERVICE_TYPE.MARKET);
        filter.add(SERVICE_TYPE.BLACK_MARKET);
        filter.add(FACTION.EMPIRE);
        filter.add(GOVERNMENT.ANARCHY);
        filter.add(GOVERNMENT.COMMUNISM);
        filter.addExclude(morgor_st);
        filter.addExclude(ithaca_st);
        VendorFilter vendorFilter = new VendorFilter();
        vendorFilter.setDisable(true);
        vendorFilter.setSkipIllegal(true);
        vendorFilter.setIllegalOnly(true);
        vendorFilter.dontBuy(true);
        vendorFilter.addSellExclude(gold);
        vendorFilter.addSellExclude(tea);
        vendorFilter.addBuyExclude(tea);
        filter.addFilter(lhs3006_st, vendorFilter);
        vendorFilter = filter.getDefaultVendorFilter();
        vendorFilter.dontSell(true);
        vendorFilter.addBuyExclude(gold);
        vendorFilter = new VendorFilter();
        vendorFilter.setSkipIllegal(true);
        filter.addFilter(lhs3262_st, vendorFilter);
        return filter;
    }

    @Test
    public void testSaveLoadFilter() throws Exception {
        MarketFilter filter = createFilter();
        FiltersStore store = new FiltersStore(objectMapper);
        store.write(filter, file);
        MarketFilter actual = store.read(world, file);
        assertFilter(filter, actual);
    }

    private void assertFilter(MarketFilter expected, MarketFilter actual){
        assertEquals(expected.getCenter(), actual.getCenter());
        assertEquals(expected.getRadius(), actual.getRadius(), 0.0001);
        assertEquals(expected.getDistance(), actual.getDistance(), 0.0001);
        assertEquals(expected.getTypes(), actual.getTypes());
        assertEquals(expected.getServices(), actual.getServices());
        assertEquals(expected.getFactions(), actual.getFactions());
        assertEquals(expected.getGovernments(), actual.getGovernments());
        assertEquals(expected.getExcludes(), actual.getExcludes());
        assertFilter(expected.getDefaultVendorFilter(), actual.getDefaultVendorFilter());
        Map<String, VendorFilter> expectedFilters = expected.getVendorFilters();
        Map<String, VendorFilter> actualFilters = actual.getVendorFilters();
        assertEquals(expectedFilters.size(), actualFilters.size());
        for (Map.Entry<String, VendorFilter> entry : expectedFilters.entrySet()) {
            VendorFilter act = actualFilters.get(entry.getKey());
            assertFilter(entry.getValue(), act);
        }

    }

    private void assertFilter(VendorFilter expected, VendorFilter actual){
        assertEquals(expected.isDisable(), actual.isDisable());
        assertEquals(expected.isIllegalOnly(), actual.isIllegalOnly());
        assertEquals(expected.isSkipIllegal(), actual.isSkipIllegal());
        assertEquals(expected.isDontBuy(), actual.isDontBuy());
        assertEquals(expected.isDontSell(), actual.isDontSell());
        assertEquals(expected.getBuyExcludes(), actual.getBuyExcludes());
        assertEquals(expected.getSellExcludes(), actual.getSellExcludes());
    }

    @After
    public void tearDown() throws Exception {
        file.delete();
    }
}