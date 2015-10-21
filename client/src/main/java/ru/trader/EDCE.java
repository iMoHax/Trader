package ru.trader;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
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
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class EDCE {
    private final static Logger LOG = LoggerFactory.getLogger(EMDNUpdater.class);
    private static ScheduledExecutorService executor;
    private static ScheduledFuture<?> checker;

    private final static int MAX_ERRORS = 5;
    private final static int CACHE_LIMIT = 8;
    private List<EDPacket> cache = new LinkedList<>();

    private final ProfileModel profile;
    private final MarketModel world;
    private final StationUpdater updater;
    private final EDSession session;
    private int errors;
    private final Settings.EDCESettings settings;
    private final BooleanProperty active;
    private boolean forceUpdate;

    public EDCE(ProfileModel profile, MarketModel world) throws IOException, ClassNotFoundException {
        this.profile = profile;
        this.world = world;
        this.session = new EDSession();
        this.updater = new StationUpdater(world);
        this.settings = Main.SETTINGS.getEdce();
        active = new SimpleBooleanProperty(settings.isActive());
        settings.activeProperty().addListener((ov, o, n) -> {
            if (n) run();
            else stop();
        });
        if (active.get()) run();
    }

    public ReadOnlyBooleanProperty activeProperty() {
        return active;
    }

    public void setForceUpdate(boolean forceUpdate) {
        this.forceUpdate = forceUpdate;
    }

    private void parseAndCheck(String json) {
        try {
            EDPacket packet = EDCEParser.parseJSON(json);
            if (cache.contains(packet)){
                if (!forceUpdate) {
                    LOG.debug("Is old packet, skip");
                    return;
                }
            } else {
                cache.add(packet);
            }
            if (cache.size() > CACHE_LIMIT){
                cache.remove(0);
            }
            Platform.runLater(() -> {
                checkCmd(packet.getCommander());
                if (checkSystem(packet.getLastSystem())){
                    if (packet.getCommander().isDocked()) {
                        checkStarport(packet.getLastStarport());
                        profile.setDocked(true);
                    } else {
                        profile.setDocked(false);
                        profile.setStation(ModelFabric.NONE_STATION);
                    }
                }
                checkShip(packet.getShip());
                forceUpdate = false;
            });
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
            sModel = world.add(system.getName(), 0,0,0);
        }
        profile.setSystem(sModel);
        return true;
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
            LOG.info("Not found station {}, adding", starport.getName());
            updater.create(sModel);
            updater.setName(starport.getName());
            station = updateStation(starport);
        } else {
            updater.edit(station);
            updateStation(starport);
        }
        profile.setStation(station);
    }

    private StationModel updateStation(Starport starport) {
        updater.setName(starport.getName());
        for (Commodity commodity : starport.getCommodities()) {
            String id = Converter.getItemId(commodity);
            if (id.isEmpty()){
                LOG.debug("{} is ignored, skip", commodity.getName());
                continue;
            }
            Optional<ItemModel> item = world.getItem(id);
            if (item.isPresent()){
                Optional<StationUpdater.FakeOffer> offer = updater.getOffer(item.get());
                if (offer.isPresent()){
                    fillOffers(offer.get(), commodity);
                } else {
                    LOG.error("Not found offer in updater, item: {}", item.get());
                }
            } else {
                LOG.warn("Not found {}, id={}", commodity, id);
            }
        }
        Shipyard shipyard = starport.getShips();
        if (shipyard != null){
            for (ShipyardItem ship : shipyard.getShips()) {
                String id = Converter.getShipId(ship);
                if (id.isEmpty()){
                    LOG.debug("{} is ignored, skip", ship.getName());
                    continue;
                }
                Optional<ItemModel> item = world.getItem(id);
                if (item.isPresent()){
                    Optional<StationUpdater.FakeOffer> offer = updater.getOffer(item.get());
                    if (offer.isPresent()){
                        fillShipOffer(offer.get(), ship);
                    } else {
                        LOG.error("Not found offer in updater, item: {}", item.get());
                    }
                } else {
                    LOG.warn("Not found {}, id={}", ship, id);
                }
            }
        }
        StationModel res = updater.commit();
        updater.reset();
        return res;
    }

    private void fillOffers(StationUpdater.FakeOffer offer, Commodity commodity){
        offer.setSprice(commodity.getBuyPrice());
        offer.setSupply(commodity.getStock());
        offer.setBprice(commodity.getSellPrice());
        offer.setDemand(commodity.getDemand());
    }

    private void fillShipOffer(StationUpdater.FakeOffer offer, ShipyardItem ship){
        offer.setSprice(ship.getBasevalue());
        offer.setSupply(1);
        offer.setBprice(0);
        offer.setDemand(0);
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
        LOG.info("Start EDCE checker each {} sec", settings.getInterval());
        active.set(true);
        checker = executor.scheduleAtFixedRate(new EDCEChecker(), 1, settings.getInterval(), TimeUnit.SECONDS);
    }
    public void stop(){
        LOG.info("Stop EDCE checker");
        active.set(false);
        if (checker != null){
            checker.cancel(false);
        }
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
                if (session.getLastStatus() == ED_SESSION_STATUS.OK || session.getLastStatus() == ED_SESSION_STATUS.ERROR) {
                    LOG.trace("Read profile from ED");
                    session.readProfile(EDCE.this::parseAndCheck);
                }
                if (session.getLastStatus() == ED_SESSION_STATUS.LOGIN_REQUIRED || session.getLastStatus() == ED_SESSION_STATUS.LOGIN_FAILED) {
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
                                } else {
                                    stop();
                                }
                                waiting = false;
                            }
                    );
                }
                if (session.getLastStatus() == ED_SESSION_STATUS.VERIFICATION_REQUIRED){
                    waiting = true;
                    Platform.runLater(() -> {
                        LOG.trace("Verification required, send request");
                        Optional<String> code = Screeners.showVerifyCodeDialog();
                        if (code.isPresent()) {
                            session.submitVerifyCode(code.get());
                        }
                        waiting = false;
                    });
                }
                if (session.getLastStatus() == ED_SESSION_STATUS.ERROR) {
                    errors++;
                    LOG.debug("Error on read response, errors count {}", errors);
                    if (errors >= MAX_ERRORS) {
                        stop();
                    }
                } else {
                    errors = 0;
                }
            } catch (Exception ex){
                LOG.error("",ex);
            }
        }
    }



}
