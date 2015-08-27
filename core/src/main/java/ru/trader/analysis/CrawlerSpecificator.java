package ru.trader.analysis;

import ru.trader.analysis.graph.Edge;
import ru.trader.core.Offer;
import ru.trader.core.Vendor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class CrawlerSpecificator {
    private final List<Vendor> any;
    private final List<Vendor> containsAny;
    private final List<Vendor> all;
    private final Collection<Offer> offers;
    private int groupCount;
    private boolean byTime;

    public CrawlerSpecificator() {
        any = new ArrayList<>();
        all = new ArrayList<>();
        containsAny = new ArrayList<>();
        offers = new ArrayList<>();
        byTime = false;
    }

    public void setByTime(boolean byTime){
        this.byTime = byTime;
    }

    public void all(Collection<Vendor> vendors){
        all.addAll(vendors);
    }

    public void any(Collection<Vendor> vendors){
        containsAny.addAll(vendors);
    }

    public void target(Vendor vendor){
        any.add(vendor);
    }

    public void targetAny(Collection<Vendor> vendors){
        any.addAll(vendors);
    }

    public void add(Vendor vendor, boolean required){
        if (required){
            all.add(vendor);
        } else {
            any.add(vendor);
        }
    }

    public void remove(Vendor vendor, boolean required){
        if (required){
            all.remove(vendor);
        } else {
            any.remove(vendor);
        }
    }

    public void buy(Collection<Offer> offers){
        this.offers.addAll(offers);
    }

    public void setGroupCount(int groupCount) {
        this.groupCount = groupCount;
    }

    private RouteSpecification<Vendor> buildOffersSpec(Collection<Vendor> vendors){
        RouteSpecification<Vendor> res = null;
        for (Offer offer : offers) {
            List<Vendor> sellers = vendors.stream().filter(v -> {
                Offer sell = v.getSell(offer.getItem());
                return sell != null && sell.getCount() >= offer.getCount();
            }).collect(Collectors.toList());
            if (res != null){
                res = res.and(RouteSpecificationByTargets.containAny(sellers));
            } else {
                res = RouteSpecificationByTargets.containAny(sellers);
            }
        }
        return res;
    }

    public VendorsCrawlerSpecification build(Collection<Vendor> vendors, Consumer<List<Edge<Vendor>>> onFoundFunc, RouteSpecification<Vendor> andSpec, boolean loop){
        RouteSpecification<Vendor> spec;
        RouteSpecification<Vendor> res = null;
        if (!all.isEmpty()){
            spec = RouteSpecificationByTargets.all(all);
            res = spec;
        }
        if (!any.isEmpty()){
            spec = any.size() > 1 ? RouteSpecificationByTargets.any(any) : new RouteSpecificationByTarget<>(any.get(0));
            if (res != null){
                res = res.and(spec);
            } else {
                res = spec;
            }
        }
        if (!containsAny.isEmpty()){
            spec = RouteSpecificationByTargets.containAny(containsAny);
            if (res != null){
                res = res.and(spec);
            } else {
                res = spec;
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

}
