package ru.trader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.edce.EDCEParser;
import ru.trader.edce.EDSession;
import ru.trader.edce.entities.*;
import ru.trader.edce.entities.System;
import ru.trader.model.*;
import ru.trader.model.support.StationUpdater;

import java.io.IOException;

public class EDCE {
    private final static Logger LOG = LoggerFactory.getLogger(EMDNUpdater.class);
    private final ProfileModel profile;
    private final MarketModel world;
    private final StationUpdater updater;
    private final EDSession session;

    public EDCE(ProfileModel profile, MarketModel world) throws IOException, ClassNotFoundException {
        this.profile = profile;
        this.world = world;
        this.session = new EDSession();
        this.updater = new StationUpdater(world);
    }

    private void parseAndCheck(String json) {
        try {
            EDPacket packet = EDCEParser.parseJSON(json);
            checkCmd(packet.getCommander());
            if (checkSystem(packet.getLastSystem())){
                checkStarport(packet.getLastStarport());
            }
            checkShip(packet.getShip());
        } catch (IOException e) {
            LOG.warn("Error on parse json:");
            LOG.warn("{}", json);
        }
    }

    private void checkCmd(Commander commander){
        if (commander == null){
            LOG.warn("Don't read commander info");
            return;
        }
        profile.setName(commander.getName());
        profile.setBalance(commander.getCredits());
        profile.setDocked(commander.isDocked());
    }

    private boolean checkSystem(System system){
        if (system == null){
            LOG.warn("Don't read last system");
            return false;
        }
        SystemModel sModel = world.get(system.getName());
        boolean found = sModel != ModelFabric.NONE_SYSTEM;
        if (!found){
            LOG.warn("Not found system {}", system.getName());
        }
        profile.setSystem(sModel);
        return found;
    }

    private void checkStarport(Starport starport){
        if (starport == null){
            LOG.warn("Don't read last star port");
            return;
        }
        SystemModel sModel = profile.getSystem();
        StationModel station = sModel.get(starport.getName());
        boolean found = station == ModelFabric.NONE_STATION;
        if (!found){
            LOG.info("Not found station {}, adding", starport.getName());
            station = sModel.add(starport.getName());
        }
        profile.setStation(station);
    }

    private void checkShip(Ship ship){
        if (ship == null){
            LOG.warn("Don't read ship");
            return;
        }
        profile.setShipCargo(ship.getCargoCapacity());
        profile.setShipTank(ship.getFuelCapacity());
    }

    private class EDCEChecker implements Runnable {

        @Override
        public void run() {
            session.readProfile(EDCE.this::parseAndCheck);
        }
    }



}
