package ru.trader.analysis;

import ru.trader.core.*;

import java.util.Collection;
import java.util.NavigableSet;
import java.util.stream.Stream;

public class FilteredMarket {
    private final Market market;
    private final MarketFilter filter;

    public FilteredMarket(Market market, MarketFilter filter) {
        this.market = market;
        this.filter = filter;
    }

    public Stream<Place> get(){
        return market.get().stream()
                .filter(p -> !filter.isFiltered(p));
    }

    public Stream<Vendor> getVendors(){
        return get().flatMap(p -> p.get().stream())
                .filter(v -> !filter.isFiltered(v));
    }

    public Stream<Vendor> getMarkets(boolean withTransit){
        return get().flatMap(p -> p.get(true).stream())
                .filter(v -> {
                    if (withTransit && v instanceof TransitVendor) return true;
                    if (!v.has(SERVICE_TYPE.MARKET) && !v.has(SERVICE_TYPE.BLACK_MARKET)) return false;
                    return !filter.isFiltered(v);
                });
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
        return res.filter(o -> !filter.isFiltered(o.getVendor(), true));
    }


}
