package ru.trader.edce;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.edce.entities.*;
import ru.trader.edce.entities.System;

import java.io.*;
import java.util.List;

public class ParseTest extends Assert {
    private final static Logger LOG = LoggerFactory.getLogger(EDSessionDemo.class);

    private String read(InputStream is){
        StringBuilder builder = new StringBuilder();
        String line;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))){
            while ((line = reader.readLine()) != null){
                if (builder.length() > 0){
                    builder.append("\n");
                }
                builder.append(line);
           }
        } catch (IOException e) {
            LOG.error("Error on read file", e);
        }
        return builder.toString();
    }

    @Test
    public void testParse() throws Exception {
        LOG.info("Test parse json");
        InputStream is = getClass().getResourceAsStream("/edce/edce.json");
        String json = read(is);
        LOG.trace("Parse json:");
        LOG.trace("{}", json);
        EDPacket packet = EDCEParser.parseJSON(json);
        Commander commander = packet.getCommander();
        assertNotNull(commander);
        assertEquals("MoHax", commander.getName());
        assertEquals(13882052, commander.getCredits());
        assertTrue(commander.isDocked());
        System system = packet.getLastSystem();
        assertNotNull(system);
        assertEquals(65179, system.getId());
        assertEquals("Ennead", system.getName());
        assertEquals("Federation", system.getFaction());
        Starport starport = packet.getLastStarport();
        assertNotNull(starport);
        assertEquals(3223840768L, starport.getId());
        assertEquals("Watt Ring", starport.getName());
        assertEquals("Federation", starport.getFaction());
        List<Commodity> commodities = starport.getCommodities();
        assertTrue(!commodities.isEmpty());
        Commodity commodity = commodities.get(0);
        assertEquals(128049202L, commodity.getId());
        assertEquals("Hydrogen Fuel", commodity.getName());
        assertEquals(0, commodity.getBuyPrice());
        assertEquals(152, commodity.getSellPrice());
        assertEquals(0, commodity.getStockBracket());
        assertEquals(2, commodity.getDemandBracket());
        assertEquals(0, commodity.getStock());
        assertEquals(659426, commodity.getDemand());
        assertEquals("Chemicals", commodity.getCategoryname());
        Ship ship = packet.getShip();
        assertNotNull(ship);
        assertEquals("CobraMkIII", ship.getName());
        assertEquals(16, ship.getFuelCapacity(), 0.0001);
        assertEquals(16, ship.getFuelLvl(), 0.0001);
        assertEquals(12, ship.getCargoCapacity());
        assertEquals(8, ship.getCargoLimit());
        Module fsd = ship.getFSD();
        assertNotNull(fsd);
        assertEquals(128064117L, fsd.getId());
        assertEquals("Int_Hyperdrive_Size4_Class5", fsd.getName());
    }

    @Test
    public void testParse2() throws Exception {
        LOG.info("Test parse json in cruise");
        InputStream is = getClass().getResourceAsStream("/edce/edce_cruise.json");
        String json = read(is);
        LOG.trace("Parse json:");
        LOG.trace("{}", json);
        EDPacket packet = EDCEParser.parseJSON(json);
        Commander commander = packet.getCommander();
        assertNotNull(commander);
        assertEquals("MoHax", commander.getName());
        assertEquals(23883052, commander.getCredits());
        assertFalse(commander.isDocked());
    }

    @Test
    public void testParse3() throws Exception {
        LOG.info("Test parse json3");
        InputStream is = getClass().getResourceAsStream("/edce/edce3.json");
        String json = read(is);
        LOG.trace("Parse json:");
        LOG.trace("{}", json);
        EDPacket packet = EDCEParser.parseJSON(json);
        System system = packet.getLastSystem();
        assertNotNull(system);
        assertEquals(63318, system.getId());
        assertEquals("CD-58 538", system.getName());
        assertEquals("Federation", system.getFaction());
        Starport starport = packet.getLastStarport();
        assertNotNull(starport);
        assertEquals(3223873536L, starport.getId());
        assertEquals("Haberlandt Orbital", starport.getName());
        assertEquals("Federation", starport.getFaction());
        List<Commodity> commodities = starport.getCommodities();
        assertTrue(!commodities.isEmpty());
        Commodity commodity = commodities.stream().filter(c -> "Explosives".equals(c.getName())).findFirst().get();
        assertEquals(182, commodity.getBuyPrice());
        assertEquals(169, commodity.getSellPrice());
        assertEquals(3, commodity.getStockBracket());
        assertEquals(0, commodity.getDemandBracket());
        assertEquals(115475, commodity.getStock());
        assertEquals(0, commodity.getDemand());
        assertEquals("Chemicals", commodity.getCategoryname());
        commodity = commodities.stream().filter(c -> "Coffee".equals(c.getName())).findFirst().get();
        assertEquals(0, commodity.getBuyPrice());
        assertEquals(1286, commodity.getSellPrice());
        assertEquals(0, commodity.getStockBracket());
        assertEquals(1, commodity.getDemandBracket());
        assertEquals(0, commodity.getStock());
        assertEquals(1441, commodity.getDemand());
        assertEquals("Foods", commodity.getCategoryname());
    }

    @Test
    public void testParse6() throws Exception {
        LOG.info("Test parse json6");
        InputStream is = getClass().getResourceAsStream("/edce/edce6.json");
        String json = read(is);
        LOG.trace("Parse json:");
        LOG.trace("{}", json);
        EDPacket packet = EDCEParser.parseJSON(json);
        Commander commander = packet.getCommander();
        assertNotNull(commander);
        assertEquals("MoHax", commander.getName());
        assertEquals(23317276, commander.getCredits());
        assertTrue(commander.isDocked());
        System system = packet.getLastSystem();
        assertNotNull(system);
        assertEquals(7034, system.getId());
        assertEquals("Iota Horologii", system.getName());
        assertEquals("Federation", system.getFaction());
        Starport starport = packet.getLastStarport();
        assertNotNull(starport);
        assertEquals(3223721472L, starport.getId());
        assertEquals("Bessemer Station", starport.getName());
        assertEquals("Federation", starport.getFaction());
        List<Commodity> commodities = starport.getCommodities();
        assertTrue(commodities.isEmpty());
        Ship ship = packet.getShip();
        assertNotNull(ship);
        assertEquals("CobraMkIII", ship.getName());
        assertEquals(16, ship.getFuelCapacity(), 0.0001);
        assertEquals(12.47372, ship.getFuelLvl(), 0.0001);
        assertEquals(0, ship.getCargoCapacity());
        assertEquals(0, ship.getCargoLimit());
        Module fsd = ship.getFSD();
        assertNotNull(fsd);
        assertEquals(128064117L, fsd.getId());
        assertEquals("Int_Hyperdrive_Size4_Class5", fsd.getName());
    }

    @Test
    public void testParse8() throws Exception {
        LOG.info("Test parse json8");
        InputStream is = getClass().getResourceAsStream("/edce/edce8.json");
        String json = read(is);
        LOG.trace("Parse json:");
        LOG.trace("{}", json);
        EDPacket packet = EDCEParser.parseJSON(json);
        Commander commander = packet.getCommander();
        assertNotNull(commander);
        System system = packet.getLastSystem();
        assertNotNull(system);
        Starport starport = packet.getLastStarport();
        assertNotNull(starport);
        Ship ship = packet.getShip();
        assertNotNull(ship);
    }

}
