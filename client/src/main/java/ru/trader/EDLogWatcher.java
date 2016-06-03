package ru.trader;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.edlog.EDLogReader;
import ru.trader.edlog.LogWatcher;
import ru.trader.model.MarketModel;
import ru.trader.model.ModelFabric;
import ru.trader.model.ProfileModel;
import ru.trader.model.SystemModel;

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

    private class EDLogHandler extends EDLogReader {
        @Override
        protected void changeSystem(String name, double x, double y, double z) {
            super.changeSystem(name, x, y, z);
            Platform.runLater(() -> {
                SystemModel sModel = world.get(name);
                boolean found = !ModelFabric.isFake(sModel);
                if (!found) {
                    LOG.warn("Not found system {}", name);
                    sModel = world.add(name, x, y, z);
                } else {
                    if (Double.compare(sModel.getX(), x) != 0 || Double.compare(sModel.getY(), y) != 0 || Double.compare(sModel.getZ(), z) != 0) {
                        LOG.warn("Wrong coordinates of system {} ({},{},{}), change to ({},{},{})", sModel.getName(), sModel.getX(), sModel.getY(), sModel.getZ(), x, y, z);
                        sModel.setPosition(x, y, z);
                    }
                }
                profile.setSystem(sModel);

            });
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
