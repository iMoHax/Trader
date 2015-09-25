package ru.trader.analysis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.core.Vendor;

import java.util.*;

public class Route implements Comparable<Route> {
    private final static Logger LOG = LoggerFactory.getLogger(Route.class);

    private final List<RouteEntry> entries;
    private double profit = 0;
    private double balance = 0;
    private double distance = 0;
    private double fuel = 0;
    private long time = 0;
    private int lands = 0;
    private int refills = 0;

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

    public RouteEntry get(int index) {
        return entries.get(index);
    }

    public boolean isEmpty(){
        return entries.isEmpty();
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

    public int getRefills() {
        return refills;
    }

    public long getTime() {
        return time;
    }

    public double getFuel() {
        return fuel;
    }

    public double getScore() {
        return profit / time;
    }

    public int getJumps(){
        return entries.size();
    }

    public boolean isLoop(){
        return !isEmpty() && entries.get(0).is(entries.get(entries.size()-1).getVendor());
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

    public int find(Vendor vendor, int offset){
        int size = entries.size();
        for (int i = 0; i < size; i++) {
            int index = i + offset;
            if (index >= size){
                if (isLoop()) index -= size;
                    else break;
            }
            RouteEntry entry = entries.get(index);
            if (entry.is(vendor)){
                return i;
            }
        }
        return -1;
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
            end.setRefill(end.getFuel());
        }
        entries.addAll(route.entries);
        updateStats();
    }

    public void dropTo(Vendor vendor){
        for (ListIterator<RouteEntry> iterator = entries.listIterator(entries.size()); iterator.hasPrevious(); ) {
            RouteEntry entry = iterator.previous();
            if (entry.is(vendor)){
                break;
            }
            iterator.remove();
        }
    }

    void updateStats(){
        LOG.trace("Update stats, old: profit={}, distance={}, lands={}, fuel={}, refills={}, time={}", profit, distance, lands, fuel, refills, time);
        profit = 0; distance = 0; lands = 0; fuel = 0; refills = 0; time = 0;
        if (entries.isEmpty()) return;
        RouteEntry entry = entries.get(0);
        for (int i = 1; i < entries.size(); i++) {
            RouteEntry next = entries.get(i);
            distance += entry.getVendor().getDistance(next.getVendor());
            profit += entry.getProfit();
            time += entry.getFullTime();
            fuel += entry.getFuel();
            if (entry.isLand()){
                lands++;
            }
            if (entry.isRefill()){
                refills++;
            }
            entry = next;
        }
        LOG.trace("new stats profit={}, distance={}, lands={}, fuel={}, time={}", profit, distance, lands, fuel, time);
    }

    @Override
    public int compareTo(Route o) {
        return Double.compare(getScore(), o.getScore());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Route)) return false;
        Route route = (Route) o;
        return (Double.compare(route.profit, profit) == 0 || Math.abs(profit - route.profit) < 0.1)&& entries.equals(route.entries);
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
                ", time=" + time +
                ", score=" + getScore() +
                ", fuel=" + fuel +
                ", lands=" + lands +
                '}';
    }
}
