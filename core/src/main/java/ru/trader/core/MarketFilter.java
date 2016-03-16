package ru.trader.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class MarketFilter {
    private final static Logger LOG = LoggerFactory.getLogger(MarketFilter.class);

    private Place center;
    private double radius;
    private double distance;
    private final EnumSet<STATION_TYPE> types;
    private final EnumSet<SERVICE_TYPE> services;
    private final EnumSet<FACTION> factions;
    private final EnumSet<GOVERNMENT> governments;
    private final Collection<Vendor> excludes;
    private final VendorFilter defaultVendorFilter;
    private final HashMap<String, VendorFilter> customFilteredVendors;

    public MarketFilter() {
        this(new VendorFilter());
    }

    public MarketFilter(VendorFilter defaultVendorFilter) {
        this.types = EnumSet.noneOf(STATION_TYPE.class);
        this.services = EnumSet.noneOf(SERVICE_TYPE.class);
        this.factions = EnumSet.noneOf(FACTION.class);
        this.governments = EnumSet.noneOf(GOVERNMENT.class);
        this.excludes = new ArrayList<>();
        this.customFilteredVendors = new HashMap<>();
        this.defaultVendorFilter = defaultVendorFilter;
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

    public void add(STATION_TYPE type){
        types.add(type);
    }

    public void remove(STATION_TYPE type){
        types.remove(type);
    }

    public void clearTypes(){
        types.clear();
    }

    public Collection<STATION_TYPE> getTypes(){
        return types;
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

    public void clearServices(){
        services.clear();
    }

    public Collection<SERVICE_TYPE> getServices(){
        return services;
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

    public void add(FACTION faction){
        factions.add(faction);
    }

    public void remove(FACTION faction){
        factions.remove(faction);
    }

    public void clearFactions(){
        factions.clear();
    }

    public Collection<FACTION> getFactions(){
        return factions;
    }

    public void add(GOVERNMENT government){
        governments.add(government);
    }

    public void remove(GOVERNMENT government){
        governments.remove(government);
    }

    public void clearGovernments(){
        governments.clear();
    }

    public Collection<GOVERNMENT> getGovernments(){
        return governments;
    }

    public static String getVendorKey(Vendor vendor){
        return vendor == null ? null : vendor.getFullName();
    }

    public VendorFilter getDefaultVendorFilter() {
        return defaultVendorFilter;
    }

    public Map<String, VendorFilter> getVendorFilters() {
        return customFilteredVendors;
    }

    public void addFilter(Vendor vendor, VendorFilter vendorFilter){
        customFilteredVendors.put(getVendorKey(vendor), vendorFilter);
    }

    public void addFilter(String key, VendorFilter vendorFilter){
        customFilteredVendors.put(key, vendorFilter);
    }

    public void removeFilter(Vendor vendor){
        customFilteredVendors.remove(getVendorKey(vendor));
    }

    public void removeFilter(String key){
        customFilteredVendors.remove(key);
    }

    public VendorFilter getFilter(Vendor vendor){
        VendorFilter filter = customFilteredVendors.get(getVendorKey(vendor));
        return filter != null ? filter : defaultVendorFilter;
    }

    public void clearVendorFilters(){
        customFilteredVendors.clear();
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
        STATION_TYPE stationType = vendor.getType();
        if (stationType != null && !types.isEmpty() && !types.contains(stationType)) return true;
        FACTION faction = vendor.getFaction();
        if (faction != null && !factions.isEmpty() && !factions.contains(faction)) return true;
        GOVERNMENT government = vendor.getGovernment();
        if (government != null && !governments.isEmpty() && !governments.contains(vendor.getGovernment())) return true;
        for (SERVICE_TYPE service : services) {
            if (!vendor.has(service)) return true;
        }
        return false;
    }

    public boolean isFiltered(Offer offer){
        if (isFiltered(offer.getVendor(), true)) return true;
        VendorFilter filter = getFilter(offer.getVendor());
        return filter.isFiltered(offer);
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
                ", types=" + types +
                ", services=" + services +
                ", factions=" + factions +
                ", governments=" + governments +
                ", excludes=" + excludes +
                '}';
    }
}
