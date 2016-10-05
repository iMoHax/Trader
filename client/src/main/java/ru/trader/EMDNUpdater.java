package ru.trader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.controllers.MainController;
import ru.trader.emdn.EMDN;
import ru.trader.emdn.entities.Item;
import ru.trader.emdn.entities.Message;
import ru.trader.model.*;
import ru.trader.model.support.StationUpdater;

import java.util.Optional;
import java.util.function.Consumer;

public class EMDNUpdater {
    private final static Logger LOG = LoggerFactory.getLogger(EMDNUpdater.class);
    private static EMDN emdn;
    private static MarketUpdater updater;

    static void init(){
        updater = new MarketUpdater(MainController.getWorld());
        emdn = new EMDN(Main.SETTINGS.getEMDNSub(), updater);
        setActivate(Main.SETTINGS.getEMDNActive());
    }

    public static void shutdown(){
        emdn.shutdown();
    }

    public static void setSub(String subServer){
        emdn.connectTo(subServer);
    }

    public static void setActivate(boolean activate){
        if (activate) {
            emdn.start();
        }  else {
            emdn.stop();
        }
    }

    public static void setWorld(MarketModel world){
        if (updater != null){
            updater.setWorld(world);
        }
    }


    private static class MarketUpdater implements Consumer<Message> {
        private StationUpdater updater;
        private MarketModel world;

        public MarketUpdater(MarketModel world) {
            this.world = world;
            this.updater = new StationUpdater(world);
        }

        public void setWorld(MarketModel world){
            this.updater = new StationUpdater(world);
            this.world = world;
        }

        @Override
        public void accept(Message message) {
            if (world == null || message == null) return;
            LOG.trace("Update station from EDDN: {}", message);
            SystemModel system = world.get(message.getBody().getSystem().getName());
            if (!ModelFabric.isFake(system)) {
                StationModel station = system.get(message.getBody().getStation().getName());
                if (!ModelFabric.isFake(station)){
                    updater.edit(station);
                    for (Item commodity : message.getBody().getCommodities()) {
                        String id = commodity.getName().toLowerCase().replace(" ","_");
                        Optional<ItemModel> item = world.getItem(id);
                        if (item.isPresent()) {
                            Optional<StationUpdater.FakeOffer> offer = updater.getOffer(item.get());
                            if (offer.isPresent()){
                                fillOffers(offer.get(), commodity);
                            } else {
                                LOG.error("Not found offer in updater, item: {}", item.get());
                            }
                        } else {
                            LOG.warn("Not found item {}, id={}", commodity, id);
                        }
                    }
                    updater.commit();
                    updater.reset();
                } else {
                    LOG.trace("Station not found");
                }
            } else {
                LOG.trace("System not found");
            }
        }

        private void fillOffers(StationUpdater.FakeOffer offer, Item commodity) {
            offer.setSprice(commodity.getBuyPrice());
            offer.setSupply(commodity.getSupply());
            offer.setBprice(commodity.getSellPrice());
            offer.setDemand(commodity.getDemand());
        }

    }

}
