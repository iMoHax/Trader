package ru.trader;

import javafx.application.Platform;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.controllers.Screeners;
import ru.trader.edce.Converter;
import ru.trader.edce.EDCEParser;
import ru.trader.edce.EDSession;
import ru.trader.edce.ED_SESSION_STATUS;
import ru.trader.edce.entities.*;
import ru.trader.edce.entities.System;
import ru.trader.model.*;
import ru.trader.model.support.StationUpdater;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class EDCE {
    private final static Logger LOG = LoggerFactory.getLogger(EMDNUpdater.class);
    private static ScheduledExecutorService executor;
    private static ScheduledFuture<?> checker;

    private final ProfileModel profile;
    private final MarketModel world;
    private final StationUpdater updater;
    private final EDSession session;
    private long interval;
    private boolean forceUpdate;

    public EDCE(ProfileModel profile, MarketModel world) throws IOException, ClassNotFoundException {
        this.profile = profile;
        this.world = world;
        this.session = new EDSession();
        this.updater = new StationUpdater(world);
        interval = 5;
        forceUpdate = true;
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
        boolean found = station != ModelFabric.NONE_STATION;
        if (!found){
            forceUpdate = false;
            LOG.info("Not found station {}, adding", starport.getName());
            updater.create(sModel);
            updater.setName(starport.getName());
            station = updateStation(starport);
        } else {
            if (!profile.getStation().equals(station) || forceUpdate){
                forceUpdate = false;
                updater.edit(station);
                updateStation(starport);
            }
        }
        profile.setStation(station);
    }

    private StationModel updateStation(Starport starport) {
        updater.setName(starport.getName());
        for (Commodity commodity : starport.getCommodities()) {
            Optional<ItemModel> item = world.getItem(Converter.getItemId(commodity.getId()));
            if (item.isPresent()){
                Optional<StationUpdater.FakeOffer> offer = updater.getOffer(item.get());
                if (offer.isPresent()){
                    fillOffers(offer.get(), commodity);
                } else {
                    LOG.error("Not found offer in updater, item: {}", item.get());
                }
            } else {
                LOG.warn("Not found item id: {}, name: {}, group: {}", commodity.getId(), commodity.getName(), commodity.getCategoryname());
            }
        }
        StationModel res = updater.commit();
        updater.reset();
        return res;
    }

    private void fillOffers(StationUpdater.FakeOffer offer, Commodity commodity){
        offer.setBprice(commodity.getBuyPrice());
        offer.setSprice(commodity.getSellPrice());
        offer.setDemand(commodity.getDemand());
        offer.setSupply(commodity.getStock());
    }

    private void checkShip(Ship ship){
        if (ship == null){
            LOG.warn("Don't read ship");
            return;
        }
        profile.setShipCargo(ship.getCargoCapacity());
        profile.setShipTank(ship.getFuelCapacity());
    }

    public void run(){
        if (executor == null) executor = Executors.newSingleThreadScheduledExecutor();
        LOG.debug("Start EDCE checker each {} sec", interval);
        checker = executor.scheduleAtFixedRate(new EDCEChecker(), interval, interval, TimeUnit.SECONDS);
    }

    public void shutdown() throws IOException {
        if (executor != null) {
            LOG.debug("Shutdown EDCE checker");
            if (checker != null) checker.cancel(true);
            executor.shutdownNow();
            executor = null;
            checker = null;
        }
        session.close();
    }

    private class EDCEChecker implements Runnable {
        private boolean waiting;

        @Override
        public void run() {
            LOG.trace("EDCE check, waiting {}", waiting);
            try {
                if (waiting) return;
                if (session.getLastStatus() == ED_SESSION_STATUS.OK) {
                    LOG.trace("Read profile from ED");
                    session.readProfile(EDCE.this::parseAndCheck);
                }
                if (session.getLastStatus() == ED_SESSION_STATUS.LOGIN_REQUIRED) {
                    waiting = true;
                    Platform.runLater(() -> {
                                Optional<Pair<String, String>> login = Screeners.showLogin();
                                if (login.isPresent()) {
                                    String email = login.get().getKey();
                                    String pass = login.get().getValue();
                                    session.login(email, pass);
                                    if (session.getLastStatus() == ED_SESSION_STATUS.VERIFICATION_REQUIRED) {
                                        LOG.trace("Verification required, send request");
                                        Optional<String> code = Screeners.showVerifyCodeDialog();
                                        if (code.isPresent()) {
                                            session.submitVerifyCode(code.get());
                                            session.login(email, pass);
                                        }
                                    }
                                    if (session.getLastStatus() == ED_SESSION_STATUS.OK) {
                                        LOG.trace("Read profile from ED");
                                        session.readProfile(EDCE.this::parseAndCheck);
                                    }
                                }
                                waiting = false;
                            }
                    );
                }
            } catch (Exception ex){
                LOG.error("",ex);
            }
        }
    }



}
