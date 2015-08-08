package ru.trader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.controllers.MainController;

import java.io.IOException;

public class ServicesManager {
    private final static Logger LOG = LoggerFactory.getLogger(ServicesManager.class);
    private static EDCE edce;



    public static void runAll(){
        runEDCE();
        runEMDN();
    }

    public static void stopAll(){
        stopEDCE();
        stopEMDN();
    }

    private static void runEDCE() {
        try {
            edce = new EDCE(MainController.getProfile(), MainController.getWorld());
            edce.run();
        } catch (IOException | ClassNotFoundException e) {
            LOG.warn("Error on init EDCE", e);
        }
    }

    private static void stopEDCE(){
        if (edce != null){
            try {
                edce.shutdown();
            } catch (IOException e) {
                LOG.warn("Error on stop EDCE", e);
            }
        }
    }

    private static void runEMDN() {
        EMDNUpdater.init();
    }

    private static void stopEMDN(){
        EMDNUpdater.shutdown();
    }
}
