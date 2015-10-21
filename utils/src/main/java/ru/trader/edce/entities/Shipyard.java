package ru.trader.edce.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.*;

public class Shipyard {
    @JsonProperty("shipyard_list")
    private Map<String, ShipyardItem> items = new HashMap<>();
    @JsonProperty("unavailable_list")
    private List<ShipyardItem> unavailables = new ArrayList<>();


    public Map<String, ShipyardItem> getItems() {
        return items;
    }

    public void setItems(Map<String, ShipyardItem> items) {
        this.items = items;
    }

    public List<ShipyardItem> getUnavailables() {
        return unavailables;
    }

    public void setUnavailables(List<ShipyardItem> unavailables) {
        this.unavailables = unavailables;
    }

    public Collection<ShipyardItem> getShips(){
        Collection<ShipyardItem> ships = new ArrayList<>(items.values());
        ships.addAll(unavailables);
        return ships;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Shipyard shipyard = (Shipyard) o;
        return Objects.equals(items, shipyard.items) &&
                Objects.equals(unavailables, shipyard.unavailables);
    }

    @Override
    public int hashCode() {
        return Objects.hash(items, unavailables);
    }
}
