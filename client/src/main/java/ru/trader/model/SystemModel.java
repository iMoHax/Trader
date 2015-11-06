package ru.trader.model;

import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.core.*;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class SystemModel {
    private final static Logger LOG = LoggerFactory.getLogger(SystemModel.class);
    private final Place system;
    private final MarketModel market;
    private StringProperty name;

    private StationModel asModel(Vendor station){
        return market.getModeler().get(station);
    }

    SystemModel() {
        this.system = null;
        this.market = null;
    }

    SystemModel(Place system, MarketModel market) {
        this.system = system;
        this.market = market;
    }

    Place getSystem() {
        return system;
    }

    MarketModel getMarket(){
        return market;
    }

    public String getName() {return name != null ? name.get() : system.getName();}

    public void setName(String value) {
        if (getName().equals(value)) return;
        LOG.info("Change name system {} to {}", system, value);
        system.setName(value);
        if (name != null) name.set(value);
    }

    public ReadOnlyStringProperty nameProperty() {
        if (name == null) {
            name = new SimpleStringProperty(system.getName());
        }
        return name;
    }

    public FACTION getFaction() {return system.getFaction();}

    public void setFaction(FACTION faction) {
        FACTION oldFaction = getFaction();
        if (oldFaction != null && oldFaction.equals(faction) || faction == null) return;
        LOG.info("Change faction station {} to {}", system, faction);
        system.setFaction(faction);
    }

    public GOVERNMENT getGovernment() {return system.getGovernment();}

    public void setGovernment(GOVERNMENT government) {
        GOVERNMENT oldGovernment = getGovernment();
        if (oldGovernment != null && oldGovernment.equals(government) || government == null) return;
        LOG.info("Change government station {} to {}", system, government);
        system.setGovernment(government);
    }

    public double getX(){
        return system.getX();
    }

    public double getY(){
        return system.getY();
    }

    public double getZ(){
        return system.getZ();
    }

    public void setPosition(double x, double y, double z){
        if (x == system.getX() && y == system.getY() && z == system.getZ()) return;
        LOG.info("Change position of system {} to ({};{};{})", this.system, x, y, z);
        system.setPosition(x, y, z);
    }

    public double getDistance(SystemModel other){
        return system.getDistance(other.getSystem());
    }

    public double getDistance(double x, double y, double z){
        return system.getDistance(x, y, z);
    }

    public List<StationModel> getStations() {
        return system.get().stream().map(this::asModel).collect(Collectors.toList());
    }

    public Collection<String> getStationNames() {
        return system.getVendorNames();
    }

    public List<String> getStationFullNames() {
        return system.get().stream().map(Vendor::getFullName).collect(Collectors.toList());
    }

    public ObservableList<String> getStationNamesList() {
        ObservableList<String> res = FXCollections.observableArrayList(ModelFabric.NONE_STATION.getName());
        res.addAll(getStationNames());
        return res;
    }

    public List<String> getStationNames(final SERVICE_TYPE service) {
        return system.get().stream().filter(v -> v.has(service)).map(Vendor::getName).collect(Collectors.toList());
    }

    public StationModel get(String name){
        if (name == null) return ModelFabric.NONE_STATION;
        return asModel(system.get(name));
    }

    public StationModel add(String name){
        return market.addStation(this, name);
    }

    public void remove(StationModel station) {
        market.removeStation(station);
    }

    public boolean isEmpty(){
        return system.isEmpty();
    }

    @Override
    public String toString() {
        if (LOG.isTraceEnabled()){
            final StringBuilder sb = new StringBuilder("SystemModel{");
            sb.append("nameProp=").append(name);
            sb.append(", system=").append(system.toString());
            sb.append('}');
            return sb.toString();
        }
        return system.toString();
    }

}
