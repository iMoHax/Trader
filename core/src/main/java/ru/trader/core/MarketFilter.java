package ru.trader.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Properties;
import java.util.stream.Collectors;

public class MarketFilter {
    private final static Logger LOG = LoggerFactory.getLogger(MarketFilter.class);

    private Place center;
    private double radius;
    private double distance;
    private final EnumSet<SERVICE_TYPE> services;
    private final Collection<Vendor> excludes;

    public MarketFilter() {
        services = EnumSet.noneOf(SERVICE_TYPE.class);
        excludes = new ArrayList<>();
    }

    public Place getCenter() {
        return center;
    }

    public void setCenter(Place center) {
        this.center = center;
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public void add(SERVICE_TYPE service){
        services.add(service);
    }

    public void addAll(Collection<SERVICE_TYPE> service){
        services.addAll(service);
    }

    public void remove(SERVICE_TYPE service){
        services.remove(service);
    }

    public boolean has(SERVICE_TYPE service){
        return services.contains(service);
    }

    public void addExclude(Vendor vendor){
        excludes.add(vendor);
    }

    public void removeExclude(Vendor vendor){
        excludes.remove(vendor);
    }

    public void clearExcludes(){
        excludes.clear();
    }

    public Collection<Vendor> getExcludes(){
        return excludes;
    }

    public boolean isFiltered(Place place){
        return center != null && center.getDistance(place) > radius;
    }

    public boolean isFiltered(Vendor vendor){
        return isFiltered(vendor, false);
    }

    public boolean isFiltered(Vendor vendor, boolean checkPlace){
        if (checkPlace && isFiltered(vendor.getPlace())) return true;
        if (distance > 0 && vendor.getDistance() > distance) return true;
        if (excludes.contains(vendor)) return true;
        for (SERVICE_TYPE service : services) {
            if (!vendor.has(service)) return true;
        }
        return false;
    }

    public Collection<Place> filtered(Collection<Place> places){
        return places.parallelStream().filter(p -> !isFiltered(p)).collect(Collectors.toList());
    }

    public Collection<Vendor> filteredVendors(Collection<Vendor> vendors){
        return vendors.parallelStream().filter(v -> !isFiltered(v)).collect(Collectors.toList());
    }


    public static MarketFilter buildFilter(Properties values, Market market){
        MarketFilter filter = new MarketFilter();
        String v = values.getProperty("filter.center", null);
        if (v != null){
            filter.setCenter(market.get(v));
        }
        filter.setRadius(Double.valueOf(values.getProperty("filter.radius","0")));
        filter.setDistance(Double.valueOf(values.getProperty("filter.distance", "0")));
        v = values.getProperty("filter.services", "");
        if (v.length() > 0){
            for (String s : v.split(",")) {
                filter.add(SERVICE_TYPE.valueOf(s));
            }
        }
        v = values.getProperty("filter.excludes", "");
        if (v.length() > 0){
            for (String s : v.split(",")) {
                String[] st = s.split("\\|");
                Place place = market.get(st[0]);
                if (place != null) {
                    Vendor vendor = place.get(st[1]);
                    if (vendor != null) {
                        filter.addExclude(vendor);
                    } else {
                        LOG.warn("Not found vendor {}", st[1]);
                    }
                } else {
                    LOG.warn("Not found place {}", st[0]);
                }
            }
        }
        return filter;
    }

    public void writeTo(Properties properties){
        properties.setProperty("filter.center", center != null ? center.getName() : "");
        properties.setProperty("filter.radius", String.valueOf(radius));
        properties.setProperty("filter.distance", String.valueOf(distance));

        StringBuilder s = new StringBuilder();
        for (SERVICE_TYPE service: services) {
            if (s.length() > 0) s.append(",");
            s.append(service);
        }
        properties.setProperty("filter.services", s.toString());
        s = new StringBuilder();
        for (Vendor vendor : excludes) {
            if (s.length() > 0) s.append(",");
            s.append(vendor.getPlace().getName());
            s.append("|");
            s.append(vendor.getName());
        }
        properties.setProperty("filter.excludes", s.toString());
    }

    @Override
    public String toString() {
        return "{" +
                "center=" + center +
                ", radius=" + radius +
                ", distance=" + distance +
                ", services=" + services +
                ", excludes=" + excludes +
                '}';
    }
}
