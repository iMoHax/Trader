package ru.trader.model;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.core.Place;
import ru.trader.core.SERVICE_TYPE;
import ru.trader.core.Vendor;

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

    public ObservableList<StationModel> getStationsList() {
        ObservableList<StationModel> res = FXCollections.observableArrayList(ModelFabric.NONE_STATION);
        res.addAll(getStations());
        return res;
    }

    public List<StationModel> getStations(final SERVICE_TYPE service) {
        return system.get().stream().filter(v -> v.has(service)).map(this::asModel).collect(Collectors.toList());
    }

    public StationModel add(String name){
        StationModel station = market.getModeler().get(system.addVendor(name));
        LOG.info("Add station {} to system {}", station, this);
        market.getNotificator().sendAdd(station);
        return station;
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
