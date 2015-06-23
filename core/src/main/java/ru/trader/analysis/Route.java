package ru.trader.analysis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.core.Vendor;

import java.util.*;

public class Route {
    private final static Logger LOG = LoggerFactory.getLogger(Route.class);

    private final List<RouteEntry> entries;
    private double profit = 0;
    private double balance = 0;
    private double distance = 0;
    private double score = 0;
    private double fuel = 0;
    private int lands = 0;

    public Route(RouteEntry root) {
        entries = new ArrayList<>();
        entries.add(root);
    }

    public Route(List<RouteEntry> entries) {
        this.entries = new ArrayList<>(entries);
        updateStats();
    }

    public List<RouteEntry> getEntries() {
        return entries;
    }

    void setBalance(double balance){
        this.balance = balance;
    }

    public double getBalance() {
        return balance;
    }

    public double getProfit() {
        return profit;
    }

    public double getDistance() {
        return distance;
    }

    public int getLands() {
        return lands;
    }

    public void add(RouteEntry entry){
        LOG.trace("Add entry {} to route {}", entry, this);
        entries.add(entry);
        updateStats();
    }

    public void addAll(Collection<RouteEntry> entries){
        LOG.trace("Add {} entries {} to route {}", entries, this);
        entries.addAll(entries);
        updateStats();
    }

    public Collection<Vendor> getVendors() {
        return getVendors(0);
    }

    public Collection<Vendor> getVendors(int index){
        if (index < 0 || index >= entries.size()) return Collections.emptyList();
        Collection<Vendor> vendors = new HashSet<>();
        for (int i = index; i < entries.size(); i++) {
            RouteEntry entry = entries.get(i);
            vendors.add(entry.getVendor());
        }
        return vendors;
    }

    public boolean contains(Collection<Vendor> vendors){
        return vendors.isEmpty()
               || vendors.size() <= entries.size()
               && vendors.stream().allMatch(v -> entries.stream().anyMatch(e -> v.equals(e.getVendor())));
    }

    public void join(Route route){
        LOG.trace("Join route {}", route);
        RouteEntry end = entries.get(entries.size()-1);
        if (route.entries.get(0).is(end.getVendor())){
            entries.remove(entries.size()-1);
        } else {
            LOG.trace("Is not connected route, set refill");
            end.setRefill(true);
        }
        entries.addAll(route.entries);
        updateStats();
    }

    void updateStats(){
        LOG.trace("Update stats, old: profit={}, distance={}, lands={}, fuel={}, score={}", profit, distance, lands, fuel, score);
        profit = 0; distance = 0; lands = 0; fuel = 0;
        if (entries.isEmpty()) return;
        RouteEntry entry = entries.get(0);
        for (int i = 1; i < entries.size(); i++) {
            RouteEntry next = entries.get(i);
            distance += entry.getVendor().getDistance(next.getVendor());
            profit += entry.getProfit();
            score += entry.getScore();
            fuel += entry.getFuel();
            if (entry.isLand()){
                lands++;
            }
            entry = next;
        }
        LOG.trace("new stats profit={}, distance={}, lands={}, fuel={}, score={}", profit, distance, lands, fuel, score);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Route)) return false;
        Route route = (Route) o;
        return Double.compare(route.profit, profit) == 0 && entries.equals(route.entries);
    }

    @Override
    public int hashCode() {
        return entries.hashCode();
    }

    @Override
    public String toString() {
        return "Route{" +
                "entries=" + entries +
                ", profit=" + profit +
                ", balance=" + balance +
                ", distance=" + distance +
                ", score=" + score +
                ", fuel=" + fuel +
                ", lands=" + lands +
                '}';
    }
}
