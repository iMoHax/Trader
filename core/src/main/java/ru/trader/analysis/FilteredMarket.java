package ru.trader.analysis;

import ru.trader.core.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.NavigableSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FilteredMarket {
    private final Market market;
    private final MarketFilter filter;
    private boolean disableFilter;

    public FilteredMarket(Market market, MarketFilter filter) {
        this.market = market;
        this.filter = filter;
    }

    public MarketFilter getFilter() {
        return filter;
    }

    public void disableFilter(boolean disableFilter) {
        this.disableFilter = disableFilter;
    }

    public Stream<Place> get(){
        Stream<Place> places = market.get().stream();
        if (disableFilter){
            return places;
        }
        return places.filter(p -> !filter.isFiltered(p));
    }

    public Stream<Vendor> getVendors(){
        if (disableFilter){
            return market.getVendors().stream();
        }
        return get().flatMap(p -> p.get().stream()).filter(v -> !filter.isFiltered(v));
    }

    public Stream<Vendor> getVendors(Place place){
        Stream<Vendor> vendors = place.get().stream();
        if (disableFilter){
            return vendors;
        }
        return vendors.filter(v -> !filter.isFiltered(v));
    }

    private boolean isMarket(Vendor vendor){
        return vendor.has(SERVICE_TYPE.MARKET) || vendor.has(SERVICE_TYPE.BLACK_MARKET);
    }

    public Stream<Vendor> getMarkets(){
        return getMarkets(false);
    }

    public Stream<Vendor> getMarkets(boolean withTransit){
        Predicate<Vendor> transitOrMarket = v -> withTransit && v.isTransit() || isMarket(v);
        if (disableFilter){
            return market.getVendors().stream().filter(transitOrMarket);
        }
        return get().flatMap(p -> p.get(withTransit).stream())
                .filter(transitOrMarket).filter(v -> !filter.isFiltered(v));
    }

    public Collection<Item> getItems(){
        return market.getItems();
    }

    public Stream<Offer> getSell(Item item){
        return getOffers(OFFER_TYPE.SELL, item);
    }

    public Stream<Offer> getBuy(Item item){
        return getOffers(OFFER_TYPE.BUY, item);
    }

    public Stream<Offer> getOffers(OFFER_TYPE offerType, Item item){
        NavigableSet<Offer> offers = market.getStat(offerType, item).getOffers();
        Stream<Offer> res;
        if (offerType.getOrder() > 0)
            res = offers.stream();
        else
            res = offers.descendingSet().stream();
        if (disableFilter){
            return res;
        }
        return res.filter(o -> !MarketUtils.isIncorrect(o) && !filter.isFiltered(o));
    }

    public Stream<Offer> getOffers(OFFER_TYPE offerType, Collection<Item> items){
        final Set<Vendor> vendors = new HashSet<>();
        Stream<Offer> res = null;
        for (Item item : items) {
            NavigableSet<Offer> offers = market.getStat(offerType, item).getOffers();
            Stream<Offer> s;
            if (offerType.getOrder() > 0)
                s = offers.stream().filter(o -> vendors.contains(o.getVendor()));
            else
                s = offers.descendingSet().stream().filter(o -> vendors.contains(o.getVendor()));

            Collection<Vendor> v = offers.stream().map(Offer::getVendor).collect(Collectors.toList());
            if (res == null){
                res = s;
                vendors.addAll(v);
            } else {
                res = Stream.concat(res, s);
                vendors.retainAll(v);
            }
        }
        if (res == null) return Stream.empty();
        if (disableFilter){
            return res;
        }
        return res.filter(o -> !MarketUtils.isIncorrect(o) && !filter.isFiltered(o));
    }

    public FilteredVendor getFiltered(Vendor vendor){
        return new FilteredVendor(vendor, filter.getFilter(vendor));
    }

}
