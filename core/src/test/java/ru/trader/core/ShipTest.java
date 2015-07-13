package ru.trader.core;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShipTest extends Assert {
    private final static Logger LOG = LoggerFactory.getLogger(ShipTest.class);

    @Test
    public void testShip() throws Exception {
        Ship ship = new Ship();
        ship.setCargo(440); ship.setTank(15);
        ship.setEngine(5, 'A'); ship.setMass(466);

        assertEquals(906, ship.getLadenMass(0), 0.0000001);
        assertEquals(913, ship.getLadenMass(7), 0.0000001);
        assertEquals(921, ship.getLadenMass(15), 0.0000001);
        assertEquals(921, ship.getLadenMass(), 0.0000001);

        assertEquals(13.373, ship.getJumpRange(), 0.001);
        assertEquals(13.373, ship.getJumpRange(15), 0.001);
        assertEquals(13.446, ship.getJumpRange(10), 0.001);
        assertEquals(13.519, ship.getJumpRange(5), 0.001);
        assertEquals(9.332, ship.getJumpRange(2), 0.001);
        assertEquals(0, ship.getJumpRange(0), 0.001);
        assertEquals(13.519, ship.getMaxJumpRange(), 0.001);
        assertEquals(40.339, ship.getFullTankJumpRange(), 0.001);

        assertEquals(0, ship.getMinFuel(0), 0.01);
        assertEquals(0.44, ship.getMinFuel(5), 0.01);
        assertEquals(2.38, ship.getMinFuel(10), 0.01);
        assertEquals(4.54, ship.getMinFuel(13), 0.01);
        assertEquals(4.94, ship.getMinFuel(13.45), 0.01);
        assertEquals(5, ship.getMinFuel(13.519), 0.01);
        assertEquals(0, ship.getMinFuel(14), 0.01);
        assertEquals(0, ship.getMinFuel(20), 0.01);

        assertEquals(15, ship.getMaxFuel(0), 0.01);
        assertEquals(15, ship.getMaxFuel(5), 0.01);
        assertEquals(15, ship.getMaxFuel(10), 0.01);
        assertEquals(15, ship.getMaxFuel(13), 0.01);
        assertEquals(9.73, ship.getMaxFuel(13.45), 0.01);
        assertEquals(5.05, ship.getMaxFuel(13.519), 0.01);
        assertEquals(0, ship.getMaxFuel(14), 0.01);
        assertEquals(0, ship.getMaxFuel(20), 0.01);

        assertEquals(15, ship.getRoundMaxFuel(0), 0.01);
        assertEquals(15, ship.getRoundMaxFuel(5), 0.01);
        assertEquals(15, ship.getRoundMaxFuel(10), 0.01);
        assertEquals(15, ship.getRoundMaxFuel(13), 0.01);
        assertEquals(9, ship.getRoundMaxFuel(13.45), 0.01);
        assertEquals(0, ship.getRoundMaxFuel(13.519), 0.01);
        assertEquals(0, ship.getRoundMaxFuel(14), 0.01);
        assertEquals(0, ship.getRoundMaxFuel(20), 0.01);

        assertEquals(0, ship.getFuelCost(15, 0), 0.001);
        assertEquals(0.448, ship.getFuelCost(15, 5), 0.001);
        assertEquals(2.453, ship.getFuelCost(15, 10), 0.001);
        assertEquals(4.665, ship.getFuelCost(15, 13), 0.001);
        assertEquals(5.070, ship.getFuelCost(15, 13.45), 0.001);
        assertEquals(5.134, ship.getFuelCost(15, 13.519), 0.001);
        assertEquals(5.593, ship.getFuelCost(15, 14), 0.001);
        assertEquals(13.403, ship.getFuelCost(15, 20), 0.001);

        assertEquals(0, ship.getFuelCost(5, 0), 0.001);
        assertEquals(0.437, ship.getFuelCost(5, 5), 0.001);
        assertEquals(2.388, ship.getFuelCost(5, 10), 0.001);
        assertEquals(4.542, ship.getFuelCost(5, 13), 0.001);
        assertEquals(4.936, ship.getFuelCost(5, 13.45), 0.001);
        assertEquals(4.999, ship.getFuelCost(5, 13.519), 0.001);
        assertEquals(5.446, ship.getFuelCost(5, 14), 0.001);
        assertEquals(13.050, ship.getFuelCost(5, 20), 0.001);

    }
}
