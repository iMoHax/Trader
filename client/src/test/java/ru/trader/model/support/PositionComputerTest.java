package ru.trader.model.support;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.core.Market;
import ru.trader.model.MarketModel;
import ru.trader.model.SystemModel;
import ru.trader.store.simple.SimpleMarket;

public class PositionComputerTest extends Assert {
    private final static Logger LOG = LoggerFactory.getLogger(PositionComputerTest.class);
    private SystemModel system1;
    private SystemModel system2;
    private SystemModel system3;
    private SystemModel system4;
    private SystemModel system5;
    private SystemModel system6;
    private SystemModel system7;
    private SystemModel system8;

    @Before
    public void setUp() throws Exception {
        LOG.info("Set up test compute");
        Market market = new SimpleMarket();
        market.addPlace("System1", -13.3438, 53.7812, 12.5625);
        market.addPlace("System2", -7.9062, 34.7188, 2.125);
        market.addPlace("System3", -4.8438, 26.6562, -4.7812);
        market.addPlace("System4", -27.0938, 21.625, 0.7812);
        market.addPlace("System5", -22.6875, 25.8125, -6.6875);
        market.addPlace("System6", -106.25, 33.5938, -61);
        market.addPlace("System7",  -50.75, 50.0312, -13.2813);
        market.addPlace("Sol",  0, 0, 0);
        MarketModel marketModel = new MarketModel(market);
        system1 = marketModel.systemsProperty().get(0);
        system2 = marketModel.systemsProperty().get(1);
        system3 = marketModel.systemsProperty().get(2);
        system4 = marketModel.systemsProperty().get(3);
        system5 = marketModel.systemsProperty().get(4);
        system6 = marketModel.systemsProperty().get(5);
        system7 = marketModel.systemsProperty().get(6);
        system8 = marketModel.systemsProperty().get(7);
    }

    @Test
    public void testCompute() throws Exception {
        LOG.info("Start test compute");
        PositionComputer computer = new PositionComputer();
        computer.addLandMark(system1, system1.getDistance(system7));
        computer.addLandMark(system2, system2.getDistance(system7));
        computer.addLandMark(system3, system3.getDistance(system7));
        computer.addLandMark(system4, system4.getDistance(system7));
        computer.addLandMark(system5, system5.getDistance(system7));
        computer.addLandMark(system6, system6.getDistance(system7));
        PositionComputer.Coordinates coordinates = computer.compute();
        assertEquals(system7.getX(), coordinates.getX(), 0.0000001);
        assertEquals(system7.getY(), coordinates.getY(), 0.0000001);
        assertEquals(system7.getZ(), coordinates.getZ(), 0.0000001);
    }

    @Test
    public void testCompute2() throws Exception {
        LOG.info("Start test compute2");
        PositionComputer computer = new PositionComputer();
        computer.addLandMark(system1, system1.getDistance(system3));
        computer.addLandMark(system2, system2.getDistance(system3));
        computer.addLandMark(system7, system7.getDistance(system3));
        computer.addLandMark(system4, system4.getDistance(system3));
        computer.addLandMark(system5, system5.getDistance(system3));
        computer.addLandMark(system6, system6.getDistance(system3));
        PositionComputer.Coordinates coordinates = computer.compute();
        assertEquals(system3.getX(), coordinates.getX(), 0.0000001);
        assertEquals(system3.getY(), coordinates.getY(), 0.0000001);
        assertEquals(system3.getZ(), coordinates.getZ(), 0.0000001);
    }

    @Test
    public void testCompute3() throws Exception {
        LOG.info("Start test compute3");
        PositionComputer computer = new PositionComputer();
        computer.addLandMark(system1, system1.getDistance(system8));
        computer.addLandMark(system2, system2.getDistance(system8));
        computer.addLandMark(system7, system7.getDistance(system8));
        computer.addLandMark(system4, system4.getDistance(system8));
        computer.addLandMark(system5, system5.getDistance(system8));
        computer.addLandMark(system6, system6.getDistance(system8));
        PositionComputer.Coordinates coordinates = computer.compute();
        assertEquals(system8.getX(), coordinates.getX(), 0.0000001);
        assertEquals(system8.getY(), coordinates.getY(), 0.0000001);
        assertEquals(system8.getZ(), coordinates.getZ(), 0.0000001);
    }
}
