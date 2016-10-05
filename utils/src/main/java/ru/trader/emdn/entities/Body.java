package ru.trader.emdn.entities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;

public class Body {
    private final StarSystem system;
    private final Station station;
    private LocalDateTime timestamp;
    private final Collection<Item> commodities;
    private final Collection<Ship> ships;

    public Body(StarSystem system, Station station) {
        this.system = system;
        this.station = station;
        commodities = new ArrayList<>();
        ships = new ArrayList<>();
    }

    public StarSystem getSystem() {
        return system;
    }

    public Station getStation() {
        return station;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Collection<Item> getCommodities() {
        return commodities;
    }

    public void add(Item item){
        commodities.add(item);
    }

    public void addAll(Collection<Item> items){
        commodities.addAll(items);
    }

    public Collection<Ship> getShips() {
        return ships;
    }

    public void add(Ship ship){
        ships.add(ship);
    }

    public void addShips(Collection<Ship> ships){
        this.ships.addAll(ships);
    }

    @Override
    public String toString() {
        return "Body{" +
                "system=" + system +
                ", station=" + station +
                ", timestamp=" + timestamp +
                ", commodities=" + commodities +
                ", ships=" + ships +
                '}';
    }
}
