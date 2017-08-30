package ru.trader;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.core.FACTION;
import ru.trader.core.GOVERNMENT;
import ru.trader.core.Place;
import ru.trader.core.Vendor;
import ru.trader.edlog.EDJournalReader;
import ru.trader.edlog.EDLogReader;
import ru.trader.edlog.LogWatcher;
import ru.trader.edlog.entities.DockedEvent;
import ru.trader.edlog.entities.FSDJumpEvent;
import ru.trader.model.*;
import ru.trader.store.imp.SimpleImporter;
import ru.trader.store.imp.entities.StarSystemData;

import java.io.File;
import java.io.IOException;

public class EDLogWatcher {
    private final static Logger LOG = LoggerFactory.getLogger(EDLogWatcher.class);

    private final ProfileModel profile;
    private final MarketModel world;
    private final LogWatcher watcher;
    private final Settings.EDLogSettings settings;


    public EDLogWatcher(ProfileModel profile, MarketModel world) {
        this.profile = profile;
        this.world = world;
        this.watcher = new LogWatcher(new EDLogHandler());
        this.settings = Main.SETTINGS.edlog();
        settings.logDirProperty().addListener(dirListener);
        settings.activeProperty().addListener(activeListener);
        if (settings.activeProperty().get()){
            run();
        }
    }

    public boolean isActive(){
        return watcher.isRun();
    }

    public boolean run(){
        LOG.info("Start ED log watcher, log dir {}", settings.logDirProperty().get());
        try {
            File dir = new File(settings.logDirProperty().get());
            if (dir.exists()){
                watcher.start(dir.toPath());
                return true;
            }
        } catch (IOException e) {
            LOG.error("Error on start log watcher", e);
        }
        return false;
    }

    public boolean stop(){
        LOG.info("Stop ED log watcher");
        watcher.stop();
        return true;
    }

    public void restart(){
        stop();
        run();
    }

    public void shutdown(){
        LOG.debug("Shutdown ED log watcher");
        stop();
        settings.logDirProperty().removeListener(dirListener);
        settings.activeProperty().removeListener(activeListener);
    }

    private class EDLogHandler extends EDJournalReader {
        private final SimpleImporter importer;

        private EDLogHandler() {
            importer = new SimpleImporter();
        }

        @Override
        protected void docked(DockedEvent dockedEvent) {
            super.docked(dockedEvent);
            Vendor vendor = importer.importStation(World.getMarket(), dockedEvent.asImportData());
            if (vendor != null){
                StationModel sModel = world.getModeler().get(vendor);
                Platform.runLater(() -> {
                    profile.setStation(sModel);
                    profile.setDocked(true);
                });
            }
        }

        @Override
        protected void jump(FSDJumpEvent jumpEvent) {
            super.jump(jumpEvent);
            Place place = importer.importSystem(World.getMarket(), jumpEvent.asImportData());
            if (place != null){
                SystemModel sModel = world.getModeler().get(place);
                Platform.runLater(() -> profile.setSystem(sModel));
            }
        }

        @Override
        protected void undock() {
            super.undock();
            Platform.runLater(() -> {
                profile.setDocked(false);
                profile.setStation(ModelFabric.NONE_STATION);
            });
        }
    }

    private final ChangeListener<String> dirListener = (ov, o, n) -> {
        if (isActive()){
            restart();
        }
    };

    private final ChangeListener<Boolean> activeListener = (ov, o, n) -> {
        if (n) run();
         else stop();

    };

}
