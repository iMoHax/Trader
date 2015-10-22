package ru.trader.model;

import javafx.beans.property.*;
import ru.trader.analysis.Route;
import ru.trader.analysis.RouteEntry;
import ru.trader.analysis.RouteFiller;
import ru.trader.analysis.RouteReserve;
import ru.trader.core.Offer;
import ru.trader.core.Order;
import ru.trader.model.support.BindingsHelper;

import java.util.*;
import java.util.stream.Collectors;

public class RouteModel {
    private final MarketModel market;
    private final Route _route;
    private final DoubleProperty profit;
    private final DoubleProperty profitByTime;
    private final List<RouteEntryModel> entries;
    private final IntegerProperty currentEntry;

    RouteModel(Route route, MarketModel market) {
        this.market = market;
        this._route = route;
        entries = _route.getEntries().stream().map(e -> new RouteEntryModel(e, market)).collect(Collectors.toList());
        profit = new SimpleDoubleProperty();
        profit.bind(BindingsHelper.group(Double::sum, RouteEntryModel::profitProperty, entries));
        profitByTime = new SimpleDoubleProperty();
        profitByTime.bind(profit.divide(_route.getTime()));
        fillSellOrders();
        currentEntry = new SimpleIntegerProperty(0);
    }

    private void fillSellOrders(){
        for (int i = 0; i < entries.size(); i++) {
            RouteEntryModel entry = entries.get(i);
            for (OrderModel order : entry.orders()) {
                for (int j = i+1; j < entries.size(); j++) {
                    RouteEntryModel buyEntry = entries.get(j);
                    if (buyEntry.getStation().equals(order.getBuyer())){
                        buyEntry.addSellOrder(order);
                        break;
                    }
                }
            }
        }
    }

    private RouteModel getCopy(){
        RouteModel res = new RouteModel(_route, market);
        res.setCurrentEntry(getCurrentEntry());
        int size = Math.min(entries.size(), res.entries.size());
        for (int i = 0; i < size; i++) {
            RouteEntryModel entry = entries.get(i);
            RouteEntryModel rEntry = res.entries.get(i);
            rEntry.addAll(entry.missions());
        }
        return res;
    }

    public RouteEntryModel get(int index){
        return entries.get(index);
    }

    public Collection<RouteEntryModel> getEntries(){
        return entries;
    }

    public double getDistance() {
        return _route.getDistance();
    }

    public int getJumps() {
        return entries.size();
    }

    public int getRefuels() {
        return _route.getRefills();
    }

    public long getTime(){
        return _route.getTime();
    }

    public Route getRoute() {
        return _route;
    }

    public boolean isLoop(){
        return _route.isLoop();
    }

    public int getLands() {
        return _route.getLands();
    }

    public double getProfit() {
        return profit.get();
    }

    public ReadOnlyDoubleProperty profitProperty() {
        return profit;
    }

    public double getProfitByTime(){
        return profitByTime.get();
    }

    public ReadOnlyDoubleProperty profitByTimeProperty(){
        return profitByTime;
    }

    public Collection<OrderModel> getOrders(){
        Collection<OrderModel> res = new ArrayList<>();
        for (RouteEntry entry : _route.getEntries()) {
            for (Order o : entry.getOrders()) {
                OrderModel order = market.getModeler().get(o);
                res.add(order);
            }
        }
        return res;
    }

    public RouteModel add(OrderModel order){
        Route path = market._getPath(order);
        if (path == null) return this;
        _route.join(path);
        return getCopy();
    }

    public RouteModel add(RouteModel route){
        _route.join(route.getRoute());
        return getCopy();
    }

    public RouteModel remove(OrderModel order) {
        _route.dropTo(order.getStation().getStation());
        return getCopy();
    }

    public void add(int offset, MissionModel mission){
        mission = mission.getCopy();
        int completeIndex = -1;
        Offer offer = mission.getOffer();
        if (offer != null){
            Collection<RouteReserve> reserves = RouteFiller.getReserves(_route, offset, offer);
            if (!reserves.isEmpty()) {
                _route.reserve(reserves);
                mission.setReserves(reserves);
                completeIndex = RouteReserve.getCompleteIndex(reserves, offset);
                for (RouteEntryModel entry : entries) {
                    entry.sellOrders().clear();
                    entry.refresh(market);
                }
                fillSellOrders();
            }
        } else
        if (mission.isDelivery()){
            RouteReserve reserve = RouteFiller.getReserves(_route, offset, mission.getTarget().getStation(), mission.getCount());
            if (reserve != null) {
                _route.reserve(reserve);
                mission.setReserves(Collections.singleton(reserve));
                completeIndex = reserve.getToIndex();
                for (RouteEntryModel entry : entries) {
                    entry.refresh(market);
                }
            }
        } else
        if (mission.isCourier()){
            completeIndex = _route.find(mission.getTarget().getStation(), offset+1);
        }
        if (completeIndex != -1){
            entries.get(completeIndex).add(mission);
        }
    }

    public void addAll(int offset, Collection<MissionModel> missions){
        for (MissionModel mission : missions) {
            mission = mission.getCopy();
            Offer offer = mission.getOffer();
            int completeIndex = -1;
            if (offer != null){
                Collection<RouteReserve> reserves = RouteFiller.getReserves(_route, offset, offer);
                if (!reserves.isEmpty()) {
                    _route.reserve(reserves);
                    mission.setReserves(reserves);
                    completeIndex = RouteReserve.getCompleteIndex(reserves, offset);
                }
            } else
            if (mission.isDelivery()){
                RouteReserve reserve = RouteFiller.getReserves(_route, offset, mission.getTarget().getStation(), mission.getCount());
                if (reserve != null) {
                    _route.reserve(reserve);
                    mission.setReserves(Collections.singleton(reserve));
                    completeIndex = reserve.getToIndex();
                }
            } else
            if (mission.isCourier()){
                completeIndex = _route.find(mission.getTarget().getStation(), offset+1);
            }
            if (completeIndex != -1){
                if (completeIndex == 0 && _route.isLoop()) completeIndex = _route.getJumps()-1;
                entries.get(completeIndex).add(mission);
            }
        }
        refresh();
    }

    public Collection<StationModel> getStations(int offset){
        Collection<StationModel> res = new HashSet<>();
        int startIndex = _route.isLoop() ? 1 : offset+1;
        if (startIndex >= entries.size()) return res;
        entries.subList(startIndex, entries.size()).stream()
                .filter(e -> !e.isTransit())
                .map(RouteEntryModel::getStation)
                .filter(station -> station != ModelFabric.NONE_STATION)
                .forEach(res::add);
        return res;
    }

    public Collection<OfferModel> getSellOffers(int offset){
        Map<ItemModel, OfferModel> res = new HashMap<>();
        for (StationModel station : getStations(offset)) {
            for (OfferModel offer : station.getSells()) {
                if (offer.getItem().isMarketItem()){
                    OfferModel old = res.get(offer.getItem());
                    if (old == null || old.getPrice() > offer.getPrice()){
                        res.put(offer.getItem(), offer);
                    }
                }
            }
        }
        return res.values();
    }

    public int getCurrentEntry() {
        return currentEntry.get();
    }

    public IntegerProperty currentEntryProperty() {
        return currentEntry;
    }

    public void setCurrentEntry(int currentEntry) {
        this.currentEntry.set(currentEntry);
    }


    public void updateCurrentEntry(SystemModel system) {
        updateCurrentEntry(system, null, false);
    }

    public void updateCurrentEntry(SystemModel system, StationModel station) {
        updateCurrentEntry(system, station, false);
    }

    public void updateCurrentEntry(SystemModel system, StationModel station, boolean undock) {
        if (undock){
            int index = getCurrentEntry();
            RouteEntryModel entry = entries.get(index);
            if (index < entries.size()-1 && system.equals(entry.getStation().getSystem()) && entry.getStation().equals(station)){
                setCurrentEntry(index+1);
            }
        } else {
            int index = getCurrentEntry();
            RouteEntryModel entry = entries.get(index);
            if (entry.isTransit() && index < entries.size()-1 && system.equals(entry.getStation().getSystem())){
                setCurrentEntry(index+1);
                return;
            }
            for (int i = index; i < entries.size(); i++) {
                entry = entries.get(i);
                if (system.equals(entry.getStation().getSystem())
                    && (ModelFabric.isFake(station) || station.equals(entry.getStation()))
                    )
                {
                    setCurrentEntry(i);
                    return;
                }
                if (!entry.isTransit()) return;
            }
            if (isLoop()){
                for (int i = 0; i < index-1; i++) {
                    entry = entries.get(i);
                    if (system.equals(entry.getStation().getSystem())
                            && (ModelFabric.isFake(station) || station.equals(entry.getStation()))
                        )
                    {
                        setCurrentEntry(i);
                        return;
                    }
                    if (!entry.isTransit()) return;
                }
            }
        }
    }

    public void complete(){
        int index = getCurrentEntry();
        RouteEntryModel entry = entries.get(index);
        Collection<OrderModel> orders = entry.orders();
        for (int i = index+1; i < entries.size(); i++) {
            RouteEntryModel e = entries.get(i);
            for (MissionModel mission : e.missions()) {
                mission.complete(orders);
            }
        }
        if (isLoop()){
            for (int i = 0; i < index; i++) {
                RouteEntryModel e = entries.get(i);
                for (MissionModel mission : e.missions()) {
                    mission.complete(orders);
                }
            }
        }
        Collection<MissionModel> missions = new ArrayList<>(entry.missions());
        boolean needRefresh = false;
        for (MissionModel mission : missions) {
            mission.complete(orders);
            if (mission.isCompleted()){
                Collection<RouteReserve> reserves = mission.getReserves();
                if (reserves != null) {
                    needRefresh = true;
                    _route.unreserve(reserves);
                }
                entry.remove(mission);
            }
        }
        if (needRefresh){
            refresh();
        }
        if (index == entries.size()-1){
            if (isLoop()) setCurrentEntry(0);
        }
    }

    public boolean isEnd(){
        return getCurrentEntry() == entries.size()-1;
    }

    private void refresh(){
        for (RouteEntryModel entry : entries) {
            entry.sellOrders().clear();
            entry.refresh(market);
        }
        fillSellOrders();
    }
}
