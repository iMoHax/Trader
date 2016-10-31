package ru.trader.powerplay;

import org.junit.Assert;
import org.junit.Test;
import ru.trader.analysis.PowerPlayAnalyzator;
import ru.trader.core.Market;
import ru.trader.core.POWER;
import ru.trader.core.POWER_STATE;
import ru.trader.core.Place;
import ru.trader.store.simple.Store;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;

public class PPImportTest extends Assert {

    @Test
    public void testImportSystems() throws Exception {
        InputStream is = getClass().getResourceAsStream("/world.xml");
        Market market = Store.loadFromFile(is);
        Place opala = market.get("Opala");
        Place aulin = market.get("Aulin");
        Place draconis = market.get("26 Draconis");
        Place bolg = market.get("Bolg");
        Place aulis = market.get("Aulis");
        Place lhs2887 = market.get("LHS 2887");
        Place gd_319 = market.get("GD 319");

        aulin.setPower(POWER.GROM, POWER_STATE.CONTROL);
        draconis.setPower(POWER.PATREUS, POWER_STATE.EXPLOITED);

        PPParser parser = new PPParser(market);
        parser.parseSystems(new File(getClass().getResource("/pp.csv").getFile()));

        assertEquals(POWER_STATE.CONTROL, opala.getPowerState());
        assertEquals(POWER.MAHON, opala.getPower());
        assertEquals(POWER_STATE.CONTROL, bolg.getPowerState());
        assertEquals(POWER.DUVAL, bolg.getPower());

        PowerPlayAnalyzator analyzator = new PowerPlayAnalyzator(market);

        Collection<Place> intersectsMahon = analyzator.getIntersects(Arrays.asList(opala, gd_319));
        Collection<Place> intersectsDuval = analyzator.getIntersects(bolg, Arrays.asList(opala, gd_319));

        Collection<Place> exploitedOpala = analyzator.getControlling(opala);
        Collection<Place> exploitedBolg = analyzator.getControlling(bolg);

        for (Place place : intersectsMahon) {
            assertEquals(POWER_STATE.EXPLOITED, place.getPowerState());
            assertEquals(POWER.MAHON, place.getPower());
        }

        for (Place place : exploitedOpala) {
            if (intersectsDuval.contains(place)){
                assertEquals(POWER_STATE.CONTESTED, place.getPowerState());
            } else {
                assertEquals(POWER_STATE.EXPLOITED, place.getPowerState());
                assertEquals(POWER.MAHON, place.getPower());
            }
        }

        for (Place place : exploitedBolg) {
            if (intersectsDuval.contains(place)){
                assertEquals(POWER_STATE.CONTESTED, place.getPowerState());
            } else {
                assertEquals(POWER_STATE.EXPLOITED, place.getPowerState());
                assertEquals(POWER.DUVAL, place.getPower());
            }
        }

        assertEquals(POWER_STATE.CONTESTED, aulin.getPowerState());
        assertEquals(POWER.DUVAL, aulin.getPower());

        assertEquals(POWER_STATE.EXPLOITED, aulis.getPowerState());
        assertEquals(POWER.MAHON, aulis.getPower());

        assertEquals(POWER_STATE.EXPLOITED, lhs2887.getPowerState());
        assertEquals(POWER.DUVAL, lhs2887.getPower());

        assertEquals(POWER_STATE.EXPANSION, draconis.getPowerState());
        assertEquals(POWER.DELAINE, draconis.getPower());

    }

}
