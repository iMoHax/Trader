package ru.trader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.controllers.MainController;
import ru.trader.emdn.EMDN;
import ru.trader.emdn.ItemData;
import ru.trader.emdn.Station;
import ru.trader.model.MarketModel;
import ru.trader.model.support.VendorUpdater;

import java.util.concurrent.*;

public class EMDNUpdater {
    private final static Logger LOG = LoggerFactory.getLogger(EMDNUpdater.class);
    private final static EMDN emdn = new EMDN();
    private static ScheduledExecutorService executor;
    private static ScheduledFuture<?> autoupdate;
    private static MarketModel market;
    private static EMDNUpdate emdnUpdater;
    private static long interval;

    public static void updateFromEMDN(VendorUpdater updater){
        Station emdnData = emdn.getVendor(updater.getName());
        LOG.debug("Update {} from EMDN", updater.getName());
        if (emdnData == null){
            LOG.trace("Not found in EMDN");
            return;
        }
        for (VendorUpdater.FakeOffer offer : updater.getOffers()) {
            if (offer.getItem().isMarketItem()){
                ItemData data = emdnData.getData(offer.getItem().getId());
                LOG.debug("Update item {} to {}", offer.getItem().getName(), data);
                if (data != null){
                    offer.setSprice(data.getBuy());
                    offer.setBprice(data.getSell());
                } else {
                    offer.setSprice(0);
                    offer.setBprice(0);
                }
            } else {
                LOG.trace("Is not market item, skip");
            }
        }
    }

    static void init(){
        setMarket(MainController.getMarket());
        setSub(Main.SETTINGS.getEMDNSub());
        setActivate(Main.SETTINGS.getEMDNActive());
        setUpdateOnly(Main.SETTINGS.getEMDNUpdateOnly());
        if (emdn.isActive())
            setInterval(Main.SETTINGS.getEMDNAutoUpdate());
    }

    public static void shutdown(){
        if (executor != null) {
            LOG.debug("Shutdown auto update");
            autoupdate.cancel(true);
            executor.shutdown();
        }
        emdn.shutdown();
    }

    public static void setMarket(MarketModel market) {
        EMDNUpdater.market = market;
        EMDNUpdate old = emdnUpdater;
        emdnUpdater = new EMDNUpdate();
        if (old != null){
            setUpdateOnly(old.updater.isUpdateOnly());
        }
        if (executor != null){
            setInterval(interval);
        }
    }

    public static void setSub(String subServer){
        emdn.connectTo(subServer);
    }

    public static void setActivate(boolean activate){
        if (activate) {
            emdn.start();
        }
        else {
            emdn.shutdown();
            setInterval(0);
        }
    }

    public static void setUpdateOnly(boolean updateOnly) {
        emdnUpdater.updater.setUpdateOnly(updateOnly);
    }

    public static void setInterval(long interval) {
        if (emdn.isActive()){
            if (interval > 0) {
                if (executor != null){
                    LOG.debug("Cancel previous auto update");
                    autoupdate.cancel(true);
                }
                if (executor == null) executor = Executors.newSingleThreadScheduledExecutor();
                LOG.debug("Start auto update each {} sec", interval);
                autoupdate = executor.scheduleAtFixedRate(emdnUpdater, interval, interval, TimeUnit.SECONDS);
            } else {
                if (executor != null){
                    LOG.debug("Stop auto update");
                    autoupdate.cancel(true);
                    executor.shutdown();
                    executor = null;
                }
            }
        }
        EMDNUpdater.interval = interval;
    }

    private static class EMDNUpdate implements Runnable {
        private final VendorUpdater updater;

        private EMDNUpdate() {
            updater = new VendorUpdater(market);
        }

        @Override
        public void run() {
            market.vendorsProperty().get().forEach((vendor) -> {
                LOG.trace("Auto update {}", vendor);
                updater.reset();
                updater.init(vendor);
                updateFromEMDN(updater);
                updater.commit();
            });
        }
    }


}
