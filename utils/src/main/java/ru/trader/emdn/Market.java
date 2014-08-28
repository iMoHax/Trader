package ru.trader.emdn;

import java.util.concurrent.ConcurrentHashMap;

public class Market {
    private final ConcurrentHashMap<String, Station> vendors = new ConcurrentHashMap<>(40, 0.9f, 1);

    public Station getVendor(String name){
        return vendors.get(name);
    }

    public void addVendor(Station vendor){
        vendors.put(vendor.getName(), vendor);
    }

    public void clear(){
        vendors.clear();
    }
}
