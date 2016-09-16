package ru.trader.emdn.entities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;

public class Body {
    private final StarSystem system;
    private final Station station;
    private LocalDateTime timestamp;
    private final Collection<Item> commodities;

    public Body(StarSystem system, Station station) {
        this.system = system;
        this.station = station;
        commodities = new ArrayList<>();
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
}
