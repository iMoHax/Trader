package ru.trader.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.core.*;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class StationModel {
    private final static Logger LOG = LoggerFactory.getLogger(StationModel.class);
    private final Vendor station;
    private final MarketModel market;

    private OfferModel asModel(Offer offer){
        return market.getModeler().get(offer);
    }

    private OfferModel asModel(Offer offer, ItemModel item){
        return market.getModeler().get(offer, item);
    }

    StationModel() {
        this.station = null;
        this.market = null;
    }

    StationModel(Vendor station, MarketModel market) {
        this.station = station;
        this.market = market;
    }

    Vendor getStation() {
        return station;
    }

    MarketModel getMarket(){
        return market;
    }

    public String getName() {return station.getName();}

    public void setName(String value) {
        if (getName().equals(value)) return;
        LOG.info("Change name station {} to {}", station, value);
        station.setName(value);
    }

    public String getFullName(){
        return station.getFullName();
    }

    public FACTION getFaction() {return station.getFaction();}

    public void setFaction(FACTION faction) {
        FACTION oldFaction = getFaction();
        if (oldFaction != null && oldFaction.equals(faction) || faction == null) return;
        LOG.info("Change faction station {} to {}", station, faction);
        station.setFaction(faction);
    }

    public GOVERNMENT getGovernment() {return station.getGovernment();}

    public void setGovernment(GOVERNMENT government) {
        GOVERNMENT oldGovernment = getGovernment();
        if (oldGovernment != null && oldGovernment.equals(government) || government == null) return;
        LOG.info("Change government station {} to {}", station, government);
        station.setGovernment(government);
    }

    public double getDistance(){
        return station.getDistance();
    }

    public void setDistance(double value){
        if (getDistance() == value) return;
        LOG.info("Change distance station {} to {}", station, value);
        station.setDistance(value);
    }

    public boolean hasService(SERVICE_TYPE service){
        return station.has(service);
    }

    public Collection<SERVICE_TYPE> getServices(){
        return station.getServices();
    }

    public void addService(SERVICE_TYPE service){
        if (station.has(service)) return;
        LOG.info("Add service {} to station {}",  service, station);
        station.add(service);
    }

    public void removeService(SERVICE_TYPE service){
        if (!station.has(service)) return;
        LOG.info("Remove service {} from station {}",  service, station);
        station.remove(service);
    }

    public SystemModel getSystem(){
        return market.getModeler().get(station.getPlace());
    }

    public List<OfferModel> getSells() {
        return station.getAllSellOffers().stream().map(this::asModel).collect(Collectors.toList());
    }

    public List<OfferModel> getBuys() {
        return station.getAllBuyOffers().stream().map(this::asModel).collect(Collectors.toList());
    }

    public OfferModel add(OFFER_TYPE type, ItemModel item, double price, long count){
        OfferModel offer = asModel(station.addOffer(type, ModelFabric.get(item), price, count), item);
        LOG.info("Add offer {} to station {}", offer, station);
        offer.refresh();
        market.getNotificator().sendAdd(offer);
        return offer;
    }

    public void remove(OfferModel offer) {
        LOG.info("Remove offer {} from station {}", offer, station);
        station.remove(ModelFabric.get(offer));
        offer.refresh();
        market.getNotificator().sendRemove(offer);
    }

    public boolean hasSell(ItemModel item) {
        return station.hasSell(ModelFabric.get(item));
    }

    public boolean hasBuy(ItemModel item) {
        return station.hasBuy(ModelFabric.get(item));
    }

    public double getDistance(StationModel other){
        return station.getDistance(other.station);
    }

    @Override
    public String toString() {
        if (LOG.isTraceEnabled()){
            final StringBuilder sb = new StringBuilder("StationModel{");
            sb.append(station.toString());
            sb.append('}');
            return sb.toString();
        }
        return station.toString();
    }

}
