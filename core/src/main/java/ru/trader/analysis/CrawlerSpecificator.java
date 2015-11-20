package ru.trader.analysis;

import ru.trader.analysis.graph.Edge;
import ru.trader.core.Offer;
import ru.trader.core.Vendor;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CrawlerSpecificator {
    private final Collection<TimeEntry<Vendor>> any;
    private final Collection<TimeEntry<Vendor>> containsAny;
    private final Collection<TimeEntry<Vendor>> all;
    private final Collection<TimeEntry<Offer>> offers;
    private int groupCount;
    private boolean byTime;
    private boolean fullScan;

    public CrawlerSpecificator() {
        any = new ArrayList<>();
        all = new ArrayList<>();
        containsAny = new ArrayList<>();
        offers = new ArrayList<>();
        byTime = false;
        fullScan = true;
    }

    private <T> void add(Collection<TimeEntry<T>> set, T obj){
        add(set, INFINITY_TIME, obj);
    }

    private <T> void add(Collection<TimeEntry<T>> set, Long time, T obj){
        TimeEntry<T> entry = new TimeEntry<>(obj, time);
        boolean add = true;
        for (Iterator<TimeEntry<T>> iterator = set.iterator(); iterator.hasNext(); ) {
            TimeEntry<T> e = iterator.next();
            if (entry.obj.equals(e.obj)){
                if (entry.compareTo(e) >= 0){
                    add = false;
                } else {
                    iterator.remove();
                }
                break;
            }
        }
        if (add){
            set.add(entry);
        }
    }

    private <T> void addAll(Collection<TimeEntry<T>> set, Collection<T> objs){
        addAll(set, INFINITY_TIME, objs);
    }

    private <T> void addAll(Collection<TimeEntry<T>> set, Long time, Collection<T> objs){
        for (T obj : objs) {
            add(set, time, obj);
        }
    }

    private <T> boolean contains(Collection<TimeEntry<T>> set, T obj){
        for (TimeEntry<T> entry : set) {
            if (entry.obj.equals(obj)){
                return true;
            }
        }
        return false;
    }

    private <T> Map<Long, List<T>> groupByTime(Collection<TimeEntry<T>> set){
        return set.stream().collect(
                Collectors.groupingBy(TimeEntry::getTime,
                                      Collectors.mapping((Function<TimeEntry<T>, T>)TimeEntry::getObj, Collectors.toList())));
    }


    public void setByTime(boolean byTime){
        this.byTime = byTime;
    }

    public void setFullScan(boolean fullScan) {
        this.fullScan = fullScan;
    }

    public boolean isFullScan() {
        return fullScan;
    }

    public void all(Collection<Vendor> vendors){
        addAll(all, vendors);
    }

    public void all(Collection<Vendor> vendors, long time){
        addAll(all, time, vendors);
    }

    public void any(Collection<Vendor> vendors){
        addAll(containsAny, vendors);
    }

    public void any(Collection<Vendor> vendors, long time){
        addAll(containsAny, time, vendors);
    }

    public void target(Vendor vendor){
        add(any, vendor);
    }

    public void target(Vendor vendor, long time){
        add(any, time, vendor);
    }

    public void targetAny(Collection<Vendor> vendors){
        addAll(any, vendors);
    }

    public void targetAny(Collection<Vendor> vendors, long time){
        addAll(any, time, vendors);
    }

    public void add(Vendor vendor, boolean required){
        if (required){
            add(all, vendor);
        } else {
            add(any, vendor);
        }
    }

    public void add(Vendor vendor, long time, boolean required){
        if (required){
            add(all, time, vendor);
        } else {
            add(any, time, vendor);
        }
    }

    public void buy(Offer offer){
        add(offers, offer);
    }

    public void buy(Offer offer, long time){
        add(offers, time, offer);
    }

    public void buy(Collection<Offer> offers){
        addAll(this.offers, offers);
    }

    public void buy(Collection<Offer> offers, long time){
        addAll(this.offers, time, offers);
    }

    public void setGroupCount(int groupCount) {
        this.groupCount = groupCount;
    }

    public int getMinHop(){
        return all.size() + (any.isEmpty() ? 0 : 1) + (containsAny.isEmpty() ? 0 : 1) + offers.size();
    }

    public boolean contains(Vendor vendor){
        boolean res = contains(all, vendor) || contains(any, vendor) || contains(containsAny, vendor);
        if (res) return true;
        for (TimeEntry<Offer> entry : offers){
            Offer offer = entry.obj;
            Offer sell = vendor.getSell(offer.getItem());
            res = sell != null && sell.getCount() >= offer.getCount();
            if (res) return true;
        }
        return false;
    }

    public Collection<Vendor> getVendors(Collection<Vendor> vendors){
        Set<Vendor> v = containsAny.stream().map(e -> e.obj).collect(Collectors.toSet());
        any.stream().map(e -> e.obj).forEach(v::add);
        all.stream().map(e -> e.obj).forEach(v::add);
        offers.stream().map(e -> e.obj.getVendor()).forEach(v::add);
        v.addAll(vendors);
        return v;
    }

    private RouteSpecification<Vendor> buildOffersSpec(Collection<Vendor> vendors){
        RouteSpecification<Vendor> res = null;
        for (TimeEntry<Offer> entry : offers) {
            Offer offer = entry.obj;
            List<Vendor> sellers = vendors.stream().filter(v -> {
                Offer sell = v.getSell(offer.getItem());
                return sell != null && sell.getCount() >= offer.getCount();
            }).collect(Collectors.toList());
            RouteSpecificationByPair<Vendor> spec = entry.time.equals(INFINITY_TIME) ?
                    new RouteSpecificationByPair<>(sellers, offer.getVendor()) :
                    new RouteSpecificationByPair<>(sellers, offer.getVendor(), entry.time);
            if (res != null){
                res = res.and(spec);
            } else {
                res = spec;
            }
        }
        return res;
    }

    public VendorsCrawlerSpecification build(Collection<Vendor> vendors, Consumer<List<Edge<Vendor>>> onFoundFunc, RouteSpecification<Vendor> andSpec, boolean loop){
        RouteSpecification<Vendor> spec;
        RouteSpecification<Vendor> res = null;
        if (!all.isEmpty()){
            for (Map.Entry<Long, List<Vendor>> entry : groupByTime(all).entrySet()) {
                spec = entry.getKey().equals(INFINITY_TIME) ?
                        RouteSpecificationByTargets.all(entry.getValue()) :
                        RouteSpecificationByTargets.all(entry.getValue(), entry.getKey());
                if (res != null){
                    res = res.and(spec);
                } else {
                    res = spec;
                }
            }
        }
        if (!any.isEmpty()){
            if (any.size() == 1){
                TimeEntry<Vendor> entry = any.iterator().next();
                spec = entry.time.equals(INFINITY_TIME) ?
                        new RouteSpecificationByTarget<>(entry.obj) :
                        new RouteSpecificationByTarget<>(entry.obj, entry.time);
                if (res != null){
                    res = res.and(spec);
                } else {
                    res = spec;
                }
            } else {
                for (Map.Entry<Long, List<Vendor>> entry : groupByTime(any).entrySet()) {
                    spec = entry.getKey().equals(INFINITY_TIME) ?
                            RouteSpecificationByTargets.any(entry.getValue()) :
                            RouteSpecificationByTargets.any(entry.getValue(), entry.getKey());
                    if (res != null){
                        res = res.and(spec);
                    } else {
                        res = spec;
                    }
                }
            }
        }
        if (!containsAny.isEmpty()){
            for (Map.Entry<Long, List<Vendor>> entry : groupByTime(containsAny).entrySet()) {
                spec = entry.getKey().equals(INFINITY_TIME) ?
                        RouteSpecificationByTargets.containAny(entry.getValue()) :
                        RouteSpecificationByTargets.containAny(entry.getValue(), entry.getKey());
                if (res != null){
                    res = res.and(spec);
                } else {
                    res = spec;
                }
            }
        }
        if (!offers.isEmpty()){
            spec = buildOffersSpec(vendors);
            if (res != null){
                res = res.and(spec);
            } else {
                res = spec;
            }
        }
        if (andSpec != null){
            if (res != null){
                res = res.and(andSpec);
            } else {
                res = andSpec;
            }
        }
        SimpleCrawlerSpecification crawlerSpecification;
        if (byTime){
            crawlerSpecification = new CrawlerSpecificationByTime(res, onFoundFunc, loop);
        } else {
            crawlerSpecification = new CrawlerSpecificationByProfit(res, onFoundFunc, loop);
        }
        crawlerSpecification.setGroupCount(groupCount);
        return (VendorsCrawlerSpecification) crawlerSpecification;
    }

    public VendorsCrawlerSpecification build(Collection<Vendor> vendors, Consumer<List<Edge<Vendor>>> onFoundFunc){
        return build(vendors, onFoundFunc, null, false);
    }

    private final static Long INFINITY_TIME = Long.MAX_VALUE;

    private class TimeEntry<T> implements Comparable<TimeEntry<T>> {
        private final T obj;
        private final Long time;

        private TimeEntry(T obj, Long time) {
            this.obj = obj;
            this.time = time;
        }

        private T getObj() {
            return obj;
        }

        private Long getTime() {
            return time;
        }

        @Override
        public int compareTo(TimeEntry<T> o) {
            return this.time.compareTo(o.time);
        }
    }
}
