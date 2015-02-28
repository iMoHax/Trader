package ru.trader.maddavo;

import org.junit.Assert;
import org.junit.Test;
import ru.trader.core.Market;
import ru.trader.core.Place;
import ru.trader.store.simple.SimpleMarket;

import java.util.Collection;

public class SystemImportTest extends Assert {

    private static final String CSVStrings =
            "'DJOWENET',37.6875,-23.96875,117.0,'Gamma','2014-11-27 11:33:44'\n" +
            "'DJUHTI',-6.8125,92.65625,24.8125,'Gamma','2014-11-27 11:33:44'\n" +
            "'DJUHTY',-17.84375,-131.375,-32.1875,'Gamma','2014-11-27 11:33:44'\n" +
            "'DK LEONIS',24.90625,61.15625,-36.1875,'Gamma','2014-11-27 11:33:44'\n" +
            "'DK URSAE MAJORIS',-48.96875,66.03125,-63.9375,'Gamma','2014-11-27 11:33:44'\n" +
            "'DN DRACONIS',-27.09375,21.625,0.78125,'Beta1','2014-10-21 17:16:31'\n" +
            "'DO CANUM VENATICORUM',-28.125,80.375,-22.03125,'Release 1.00-EDStar','2015-01-12 15:20:07'\n" +
            "'DOBUNN',-55.25,5.25,118.5625,'Gamma','2014-11-27 11:33:44'\n" +
            "'DOCLEACHI',76.96875,-2.9375,21.15625,'Gamma','2014-11-27 11:33:44'\n" +
            "'DOGONEJA',,,,'Gamma','2014-11-27 11:33:44'\n" +
            "'DOHKWIBUR',109.71875,16.875,-95.96875,'Gamma','2014-11-27 11:33:44'";

    private static final String[] lines = CSVStrings.split("\\n");

    private Market createMarket(){
        Market market = new SimpleMarket();
        market.addPlace("1 Hydrae",0,0,0);
        market.addPlace("Djuhty",15,20,30);
        market.addPlace("DOGONEJA",15,20,30);

        return market;
    }

    @Test
    public void testImport() throws Exception {
        Market market = createMarket();
        SystemHandler handler = new SystemHandler(market);
        for (String line : lines) {
            handler.parse(line);
        }

        Collection<Place> systems = market.get();
        assertEquals("Systems count distinct", 12, systems.size());

        Place system =  market.get("Djuhty");
        assertNotNull(system);
        assertEquals(system.getX(), -17.84375, 0.0);
        assertEquals(system.getY(), -131.375, 0.0);
        assertEquals(system.getZ(), -32.1875, 0.0);

        system =  market.get("DOGONEJA");
        assertNotNull(system);
        assertEquals(system.getX(), 15, 0.0);
        assertEquals(system.getY(), 20, 0.0);
        assertEquals(system.getZ(), 30, 0.0);

    }
}
