package ru.trader.store.imp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.core.*;
import ru.trader.store.imp.entities.*;

import java.util.*;
import java.util.function.Predicate;

public abstract class AbstractImporter implements Importer {
    private final static Logger LOG = LoggerFactory.getLogger(AbstractImporter.class);
    private final EnumSet<IMPORT_FLAG> flags;

    protected AbstractImporter() {
        this.flags = EnumSet.copyOf(IMPORT_FLAG.ADD_AND_UPDATE);
    }

    @Override
    public void addFlag(IMPORT_FLAG flag) {
        flags.add(flag);
    }

    @Override
    public void removeFlag(IMPORT_FLAG flag) {
        flags.remove(flag);
    }

    @Override
    public void setFlags(EnumSet<IMPORT_FLAG> flags) {
        this.flags.clear();
        this.flags.addAll(flags);
    }

    @Override
    public void imp(Market market){
        while (next()){
            StarSystemData systemData = getSystem();
            Place system = impSystem(market, systemData);
            if (system != null){
                Collection<StationData> stations = systemData.getStations();
                impStations(market, system, stations);
            } else {
                LOG.warn("System {} not found", systemData.getName());
            }
        }
    }

    protected Place impSystem(Market market, StarSystemData data){
        Place system = market.get(data.getName());
        if (system == null){
            if (flags.contains(IMPORT_FLAG.ADD_STARSYSTEMS)){
                LOG.debug("{} - is new system, adding", data.getName());
                system = market.addPlace(data.getName(), data.getX(), data.getY(), data.getZ());
            }
        }
        if (flags.contains(IMPORT_FLAG.STARSYSTEMS)){
            updateSystem(system, data);
        }
        return system;
    }

    protected void updateSystem(Place system, StarSystemData data){
        if (!Double.isNaN(data.getX()) && !Double.isNaN(data.getY()) && !Double.isNaN(data.getZ()) &&
                (data.getX() != system.getX() || data.getY() != system.getY() || data.getZ() != system.getZ())){
            system.setPosition(data.getX(), data.getY(), data.getZ());
        }
        if (data.getFaction() != null){
            system.setFaction(data.getFaction());
        }
        if (data.getGovernment() != null){
            system.setGovernment(data.getGovernment());
        }
        if (data.getPower() != null && data.getPowerState() != null){
            system.setPower(data.getPower(), data.getPowerState());
        }
    }

    protected void impStations(Market market, Place system, Collection<StationData> stations){
        if (stations == null) return;
        Set<String> stationsList = new HashSet<>();
        if (flags.contains(IMPORT_FLAG.REMOVE_STATIONS)){
            stationsList.addAll(system.getVendorNames());
        }
        for (StationData s : stations) {
            Vendor station = impStation(system, s);
            if (station != null){
                stationsList.remove(station.getName());
                impItems(market, station, s.getCommodities(), s.getModules(), s.getShips());
            } else {
                LOG.warn("Station {} not found", s.getName());
            }
        }
        if (flags.contains(IMPORT_FLAG.REMOVE_STATIONS)){
            for (String s : stationsList) {
                LOG.debug("{} - is old station, remove", s);
                Vendor station = system.get(s);
                if (station != null){
                    system.remove(station);
                }
            }
        }
    }

    protected Vendor impStation(Place system, StationData data){
        Vendor station = system.get(data.getName());
        if (station == null){
            if (flags.contains(IMPORT_FLAG.ADD_STATIONS)){
                LOG.debug("{} - is new station, adding", data.getName());
                station = system.addVendor(data.getName());
            }
        }
        if (flags.contains(IMPORT_FLAG.STATIONS)){
            updateStation(station, data);
        }
        return station;
    }

    protected void updateStation(Vendor station, StationData data) {
        if (!Double.isNaN(data.getDistance())){
            station.setDistance(data.getDistance());
        }
        if (data.getType() != null){
            station.setType(data.getType());
        }
        if (data.getFaction() != null){
            station.setFaction(data.getFaction());
        }
        if (data.getGovernment() != null){
            station.setGovernment(data.getGovernment());
        }
        if (data.getEconomic() != null){
            station.setEconomic(data.getEconomic());
        }
        if (data.getSubEconomic() != null){
            station.setSubEconomic(data.getSubEconomic());
        }
        if (data.getServices() != null){
            Collection<SERVICE_TYPE> services = new ArrayList<>(station.getServices());
            services.removeAll(data.getServices());
            services.forEach(station::remove);
            data.getServices().forEach(station::add);
        }
        if (data.getModifiedTime() != null){
            station.setModifiedTime(data.getModifiedTime());
        }
    }


    protected void impItems(Market market, Vendor station, Collection<ItemData> commodities, Collection<ModuleData> modules, Collection<ShipData> ships){
        Set<Item> itemsList = new HashSet<>();
        if (flags.contains(IMPORT_FLAG.REMOVE_COMMODITY) || flags.contains(IMPORT_FLAG.REMOVE_MODULE) || flags.contains(IMPORT_FLAG.REMOVE_SHIP)){
            Predicate<Offer> isCanRemove = o ->
                           ships != null && o.getItem().getGroup().isShip() && flags.contains(IMPORT_FLAG.REMOVE_SHIP) ||
                           modules != null && o.getItem().getGroup().isOutfit() && flags.contains(IMPORT_FLAG.REMOVE_MODULE) ||
                           commodities != null && o.getItem().getGroup().isMarket() && flags.contains(IMPORT_FLAG.REMOVE_COMMODITY);
            station.getAllSellOffers().stream().filter(isCanRemove).map(Offer::getItem).forEach(itemsList::add);
            station.getAllBuyOffers().stream().filter(isCanRemove).map(Offer::getItem).forEach(itemsList::add);
        }
        if (commodities != null){
            for (ItemData c : commodities) {
                Item item = impItem(market, station, c);
                if (item != null){
                    itemsList.remove(item);
                } else {
                    LOG.warn("Item {}({}) not found", c.getId(), c.getName());
                }
            }
        }
        if (modules != null){
            for (ModuleData m : modules) {
                Item item = impModule(market, station, m);
                if (item != null){
                    itemsList.remove(item);
                } else {
                    LOG.warn("Item {}({}) not found", m.getId(), m.getName());
                }
            }
        }
        if (ships != null){
            for (ShipData s : ships) {
                Item item = impShip(market, station, s);
                if (item != null){
                    itemsList.remove(item);
                } else {
                    LOG.warn("Item {}({}) not found", s.getId(), s.getName());
                }
            }
        }

        if (flags.contains(IMPORT_FLAG.REMOVE_COMMODITY) || flags.contains(IMPORT_FLAG.REMOVE_MODULE) || flags.contains(IMPORT_FLAG.REMOVE_SHIP)){
            for (Item i : itemsList) {
                Offer o = station.get(OFFER_TYPE.SELL, i);
                if (o != null){
                    LOG.debug("{} - is old offer, remove", o);
                    station.remove(o);
                }
                o = station.get(OFFER_TYPE.BUY, i);
                if (o != null){
                    LOG.debug("{} - is old offer, remove", o);
                    station.remove(o);
                }
            }
        }
    }

    protected Item impItem(Market market, Vendor station, ItemData data) {
        Item item = market.getItem(data.getName());
        if (item == null){
            if (data.getGroup() != null && flags.contains(IMPORT_FLAG.ADD_COMMODITY)){
                LOG.debug("{} - is new commodity, adding", data.getName());
                Optional<Group> group = market.getGroups().stream().filter(g -> g.getName().equals(data.getGroup())).findAny();
                if (group.isPresent()) {
                    item = market.addItem(data.getName(), group.get());
                } else {
                    LOG.warn("Not found group, id={}, skip item", data.getGroup());
                }
            }
        }
        if (item != null && flags.contains(IMPORT_FLAG.ITEMS)){
            updateOffers(station, item, data);
        }
        return item;
    }

    protected void updateOffers(Vendor station, Item item, ItemData data){
        Offer sellOffer = station.getSell(item);
        if (data.getBuyOfferPrice() > 0){
            if (sellOffer != null){
                sellOffer.setPrice(data.getSellOfferPrice());
                sellOffer.setCount(data.getSupply());
            } else {
                station.addOffer(OFFER_TYPE.SELL, item, data.getBuyOfferPrice(), data.getSupply());
            }
        } else {
            if (sellOffer != null) station.remove(sellOffer);
        }
        Offer buyOffer = station.getBuy(item);
        if (data.getSellOfferPrice() > 0){
            if (buyOffer != null){
                buyOffer.setPrice(data.getBuyOfferPrice());
                buyOffer.setCount(data.getDemand());
            } else {
                station.addOffer(OFFER_TYPE.BUY, item, data.getSellOfferPrice(), data.getDemand());
            }
        } else {
            if (buyOffer != null) station.remove(buyOffer);
        }
    }

    protected Item impShip(Market market, Vendor station, ShipData data) {
        Item item = market.getItem(data.getName());
        if (item == null){
            if (flags.contains(IMPORT_FLAG.ADD_SHIP)){
                LOG.debug("{} - is new ship, adding", data.getName());
                Optional<Group> group = market.getGroups().stream().filter(Group::isShip).findAny();
                if (group.isPresent()) {
                    item = market.addItem(data.getName(), group.get());
                } else {
                    LOG.warn("Not found ship group, skip");
                }
            }
        }
        if (item != null && flags.contains(IMPORT_FLAG.ITEMS)){
            updateOffers(station, item, data);
        }
        return item;
    }

    protected void updateOffers(Vendor station, Item item, ShipData data){
        if (data.getPrice() == null) return;
        Offer sellOffer = station.getSell(item);
        if (data.getPrice() > 0){
            if (sellOffer != null){
                sellOffer.setPrice(data.getPrice());
                sellOffer.setCount(1);
            } else {
                station.addOffer(OFFER_TYPE.SELL, item, data.getPrice(), 1);
            }
        } else {
            if (sellOffer != null) station.remove(sellOffer);
        }
    }

    protected Item impModule(Market market, Vendor station, ModuleData data) {
        Item item = market.getItem(data.getName());
        if (item == null){
            if (data.getGroup() != null && flags.contains(IMPORT_FLAG.ADD_MODULE)){
                LOG.debug("{} - is new module, adding", data.getName());
                Optional<Group> group = market.getGroups().stream().filter(g -> g.getName().equals(data.getGroup())).findAny();
                if (group.isPresent()) {
                    item = market.addItem(data.getName(), group.get());
                } else {
                    LOG.warn("Not found outfit group, id={}, skip item", data.getGroup());
                }
            }
        }
        if (item != null && flags.contains(IMPORT_FLAG.ITEMS)){
            updateOffers(station, item, data);
        }
        return item;
    }

    protected void updateOffers(Vendor station, Item item, ModuleData data){
        Offer sellOffer = station.getSell(item);
        if (data.getPrice() > 0){
            if (sellOffer != null){
                sellOffer.setPrice(data.getPrice());
                sellOffer.setCount(1);
            } else {
                station.addOffer(OFFER_TYPE.SELL, item, data.getPrice(), 1);
            }
        } else {
            if (sellOffer != null) station.remove(sellOffer);
        }
    }

}
