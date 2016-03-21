package ru.trader.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.core.*;

import java.time.LocalDateTime;
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

    public STATION_TYPE getType() {return station.getType();}

    public void setType(STATION_TYPE type) {
        STATION_TYPE oldType = getType();
        if (oldType != null && oldType.equals(type) || type == null) return;
        LOG.info("Change type station {} to {}", station, type);
        station.setType(type);
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

    public ECONOMIC_TYPE getEconomic() {return station.getEconomic();}

    public void setEconomic(ECONOMIC_TYPE economic) {
        ECONOMIC_TYPE oldEconomic = getEconomic();
        if (oldEconomic != null && oldEconomic.equals(economic) || economic == null) return;
        LOG.info("Change economic of station {} to {}", station, economic);
        station.setEconomic(economic);
    }

    public ECONOMIC_TYPE getSubEconomic() {return station.getSubEconomic();}

    public void setSubEconomic(ECONOMIC_TYPE economic) {
        ECONOMIC_TYPE oldEconomic = getSubEconomic();
        if (oldEconomic != null && oldEconomic.equals(economic) || economic == null) return;
        LOG.info("Change sub economic of station {} to {}", station, economic);
        station.setSubEconomic(economic);
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

    public List<OfferModel> getAllSells() {
        return station.getAllSellOffers().stream().map(this::asModel).collect(Collectors.toList());
    }

    public List<OfferModel> getSells() {
        return station.getSellOffers().map(this::asModel).collect(Collectors.toList());
    }

    public List<OfferModel> getAllBuys() {
        return station.getAllBuyOffers().stream().map(this::asModel).collect(Collectors.toList());
    }

    public List<OfferModel> getBuys() {
        return station.getBuyOffers().map(this::asModel).collect(Collectors.toList());
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

    public LocalDateTime getModifiedTime(){
        return station.getModifiedTime();
    }

    public boolean isCorrect(){
        return !station.getName().isEmpty() && station.getType() != null && station.getDistance() > 0
               && station.getFaction() != null && station.getGovernment() != null
               && station.getEconomic() != null && station.getSubEconomic() != null;
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
