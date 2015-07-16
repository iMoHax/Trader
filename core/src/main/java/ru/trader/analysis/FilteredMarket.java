package ru.trader.analysis;

import ru.trader.core.*;

import java.util.Collection;
import java.util.NavigableSet;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class FilteredMarket {
    private final Market market;
    private final MarketFilter filter;
    private boolean disableFilter;

    public FilteredMarket(Market market, MarketFilter filter) {
        this.market = market;
        this.filter = filter;
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

    private boolean isTransit(Vendor vendor){
        return vendor instanceof TransitVendor;
    }

    public Stream<Vendor> getMarkets(boolean withTransit){
        Predicate<Vendor> transitOrMarket = v -> withTransit && isTransit(v) || isMarket(v);
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
        return res.filter(o -> !filter.isFiltered(o.getVendor(), true));
    }


}
