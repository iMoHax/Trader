package ru.trader.emdn;

import java.util.concurrent.ConcurrentHashMap;

public class Station {
    private final String name;
    private final ConcurrentHashMap<String, ItemData> items = new ConcurrentHashMap<>(15, 0.9f, 1);

    public Station(String name) {
        this.name = name;
    }

    public ItemData getData(String name){
        return items.get(name);
    }

    void update(ItemData item){
        items.put(item.getName(), item);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Station)) return false;
        Station station = (Station) o;
        return name.equals(station.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    public String getName() {
        return name;
    }
}
