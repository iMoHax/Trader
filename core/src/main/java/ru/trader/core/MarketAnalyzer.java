package ru.trader.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.analysis.*;
import ru.trader.analysis.graph.ConnectibleGraph;
import ru.trader.analysis.graph.Edge;

import java.util.*;
import java.util.stream.Collectors;

public class MarketAnalyzer {
    private final static Logger LOG = LoggerFactory.getLogger(MarketAnalyzer.class);

    private final FilteredMarket market;
    private final Profile profile;
    private final RouteSearcher searcher;
    private MarketAnalyzerCallBack callback;


    private final static Comparator<Order> orderComparator = (o1, o2) -> o2.compareTo(o1);

    public MarketAnalyzer(FilteredMarket market, Profile profile) {
        this(market, profile, new MarketAnalyzerCallBack());
    }

    public MarketAnalyzer(FilteredMarket market, Profile profile, MarketAnalyzerCallBack callback) {
        this.market = market;
        this.callback = callback;
        this.profile = profile;
        this.searcher =  new RouteSearcher(new Scorer(market, profile));
    }

    public void setCallback(MarketAnalyzerCallBack callback) {
        this.callback = callback;
    }

    public Profile getProfile() {
        return profile;
    }

    public List<Offer> getOffers(OFFER_TYPE offerType, Item item, MarketFilter filter){
        return market.getOffers(offerType, item).filter(o -> !filter.isFiltered(o.getVendor())).collect(Collectors.toList());
    }

    public List<Vendor> getVendors(MarketFilter filter){
        return market.getVendors().filter(v -> !filter.isFiltered(v)).collect(Collectors.toList());
    }

    public Collection<Order> getTop(int limit){
        LOG.debug("Get top {}", limit);
        Collection<Place> places = getPlaces();
        LimitedQueue<Order> top = new LimitedQueue<>(limit, orderComparator);
        callback.setMax(places.size());
        for (Place place : places) {
            if (callback.isCancel()) break;
            LOG.trace("Check place {}", place);
            Collection<Order> orders = getOrders(place, top.isEmpty() ? 0 : top.last().getProfit());
            top.addAll(orders);
            callback.inc();
        }
        callback.onEnd();
        return top;
    }

    public Collection<Order> getOrders(Place place) {
        return getOrders(place, 0);
    }

    public Collection<Order> getOrders(Place from, Place to) {
        if (isInaccessible(from, to)){
            return Collections.emptyList();
        }
        return getOrders(getVendors(from), getVendors(to), 0);
    }

    public Collection<Order> getOrders(Place from, Vendor to) {
        if (isInaccessible(from, to.getPlace())){
            return Collections.emptyList();
        }
        return getOrders(getVendors(from), Collections.singleton(to), 0);
    }

    public Collection<Order> getOrders(Vendor vendor) {
        return getOrders(vendor.getPlace(), Collections.singleton(vendor), 0);
    }

    public Collection<Order> getOrders(Vendor from, Place to) {
        if (isInaccessible(from.getPlace(), to)){
            return Collections.emptyList();
        }
        return getOrders(Collections.singleton(from), to.get(), 0);
    }

    public Collection<Order> getOrders(Vendor from, Vendor to) {
        if (isInaccessible(from.getPlace(), to.getPlace())){
            return Collections.emptyList();
        }
        return getOrders(Collections.singleton(from), Collections.singleton(to), 0);
    }


    private Collection<Order> getOrders(Place place, double lowProfit) {
        return getOrders(place, getVendors(place), lowProfit);
    }

    private Collection<Order> getOrders(Place place, Collection<Vendor> sellers, double lowProfit) {
        ConnectibleGraph<Place> graph = new ConnectibleGraph<>(profile);
        graph.build(place, getPlaces());
        List<Order> res = new ArrayList<>(20);
        callback.setMax(sellers.size());
        for (Vendor vendor : sellers) {
            if (callback.isCancel()) break;
            for (Offer sell : vendor.getAllSellOffers()) {
                if (callback.isCancel()) break;
                LOG.trace("Sell offer {}", sell);
                if (sell.getCount() == 0) continue;
                long count = Order.getMaxCount(sell, profile.getBalance(), profile.getShip().getCargo());
                LOG.trace("count = {}", count);
                if (count == 0) continue;
                Iterator<Offer> buyers = market.getBuy(sell.getItem()).iterator();
                while (buyers.hasNext()){
                    if (callback.isCancel()) break;
                    Offer buy = buyers.next();
                    if (!graph.isAccessible(buy.getVendor().getPlace())){
                        LOG.trace("Is inaccessible buyer, skip");
                        continue;
                    }
                    Order order = new Order(sell, buy, count);
                    LOG.trace("Buy offer {} profit = {}", buy, order.getProfit());
                    if (order.getProfit() <= 0 && order.getCount() > 0) break;
                    if (order.getProfit() < lowProfit && order.getCount() == count) {
                        LOG.trace("Is low profit, skip");
                        break;
                    }
                    res.add(order);
                }
            }
            callback.inc();
        }
        res.sort(orderComparator);
        return res;
    }

    private Collection<Order> getOrders(Collection<Vendor> sellers, Collection<Vendor> buyers, double lowProfit) {
        List<Order> res = new ArrayList<>();
        callback.setMax(sellers.size());
        for (Vendor seller : sellers) {
            if (callback.isCancel()) break;
            for (Offer sell : seller.getAllSellOffers()) {
                if (callback.isCancel()) break;
                if (sell.getCount() == 0) continue;
                long count = Order.getMaxCount(sell, profile.getBalance(), profile.getShip().getCargo());
                LOG.trace("Sell offer {}, count = {}", sell, count);
                if (count == 0) continue;
                for (Vendor buyer : buyers) {
                    if (callback.isCancel()) break;
                    Offer buy = buyer.getBuy(sell.getItem());
                    if (buy != null){
                        Order order = new Order(sell, buy, count);
                        LOG.trace("Buy offer {} profit = {}", buy, order.getProfit());
                        if (order.getProfit() < lowProfit) {
                            LOG.trace("Is low profit, skip");
                            continue;
                        }
                        res.add(order);
                    }
                }
            }
            callback.inc();
        }
        res.sort(orderComparator);
        return res;
    }

    public Collection<List<Edge<Place>>> getPaths(Place from, Place to){
        return searcher.getPaths(from, to, getPlaces());
    }

    public Route getPath(Vendor from, Vendor to){
        return RouteSearcher.toRoute(from, to, searcher.getPath(from.getPlace(), to.getPlace(), getPlaces()));
    }

    public Route getPath(Order order){
        return RouteSearcher.toRoute(order, searcher.getPath(order.getSeller().getPlace(), order.getBuyer().getPlace(), getPlaces()));
    }

    public Collection<Route> getTopRoutes(int limit){
        LOG.debug("Get top {}", limit);
        LimitedQueue<Route> top = new LimitedQueue<>(limit);
        Collection<Vendor> vendors = getVendors();
        callback.setMax(vendors.size());
        Iterator<Vendor> iterator = market.getMarkets(false).iterator();
        while (iterator.hasNext()){
            Vendor vendor = iterator.next();
            if (callback.isCancel()) break;
            Collection<Route> paths = searcher.getRoutes(vendor, vendor, vendors, 3);
            top.addAll(paths);
            callback.inc();
        }
        callback.onEndSearch();
        return top;
    }

    public Collection<Route> getLoops(Vendor vendor, int limit){
        return searcher.getLoops(vendor, getVendors(), limit);
    }

    public Collection<Route> getRoutes(Place from){
        return searcher.getRoutes(getVendors(from), getVendors());
    }

    public Collection<Route> getRoutes(Place from, Place to){
        return searcher.getRoutes(getVendors(from), getVendors(to), getVendors());
    }

    public Collection<Route> getRoutes(Place from, Vendor to){
        return searcher.getRoutes(getVendors(from), Collections.singleton(to), getVendors());
    }

    public Collection<Route> getRoutes(Vendor from){
        return searcher.getRoutes(from, getVendors());
    }

    public Collection<Route> getRoutes(Vendor from, Place to){
        return searcher.getRoutes(Collections.singleton(from), getVendors(to), getVendors());
    }

    public Collection<Route> getRoutes(Vendor from, Vendor to){
        return searcher.getRoutes(from, to, getVendors());
    }


    public Route getRoute(Collection<Vendor> vendors) {
        Route res = null;
        callback.setMax(vendors.size());
        for (Vendor from : vendors) {
            //TODO: implement search with constant length
            Collection<Route> paths = searcher.getRoutes(from, vendors);
            Optional<Route> route = paths.stream().filter(p -> p.contains(vendors)).findFirst();
            if (route.isPresent() && (res == null || res.compareTo(route.get()) > 0)){
                res = route.get();
            }
            callback.inc();
        }
        callback.onEndSearch();
        return res;
    }

    private boolean isInaccessible(Place from, Place to){
        ConnectibleGraph<Place> graph = new ConnectibleGraph<>(profile);
        graph.build(from, getPlaces());
        if (!graph.isAccessible(to)){
            LOG.trace("Is inaccessible buyer");
            return true;
        }
        return false;
    }

    private List<Place> getPlaces(){
        return market.get().collect(Collectors.toList());
    }

    private List<Vendor> getVendors(){
        return market.getMarkets(true).collect(Collectors.toList());
    }

    private List<Vendor> getVendors(Place place){
        return market.getVendors(place).collect(Collectors.toList());
    }
}
