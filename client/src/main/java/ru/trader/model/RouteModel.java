package ru.trader.model;

import javafx.beans.property.*;
import ru.trader.analysis.Route;
import ru.trader.analysis.RouteEntry;
import ru.trader.analysis.RouteFiller;
import ru.trader.analysis.RouteReserve;
import ru.trader.core.Offer;
import ru.trader.core.Order;
import ru.trader.model.support.BindingsHelper;
import ru.trader.view.support.Localization;
import ru.trader.view.support.ViewUtils;

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
        profitByTime = new SimpleDoubleProperty();
        currentEntry = new SimpleIntegerProperty(0);
        fill();
    }

    private void fill(){
        profit.bind(BindingsHelper.group(Double::sum, RouteEntryModel::profitProperty, entries));
        profitByTime.bind(profit.divide(_route.getTime()));
        long time = 0;
        for (int i = 0; i < entries.size()-1; i++) {
            RouteEntryModel entry = entries.get(i);
            RouteEntryModel entry2 = entries.get(i+1);
            time += entry.getTime();
            entry2.setDistance(entry.getStation().getDistance(entry2.getStation()));
            entry2.setFullTime(time);
        }
        fillSellOrders();
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

    public MarketModel getMarket(){
        return market;
    }

    Route getRoute() {
        return _route;
    }


    private RouteModel copyFill(Route route){
        return copyFill(route, entries.size()-1);
    }

    private RouteModel copyFill(Route route, int index){
        RouteModel res = new RouteModel(route, market);
        res.setCurrentEntry(getCurrentEntry());
        int size = Math.min(index+1, res.entries.size());
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

    public RouteEntryModel getLast(){
        if (entries.size() == 1) return entries.get(0);
        return entries.get(entries.size()-1);
    }

    public double getDistance() {
        return _route.getDistance();
    }

    public int getJumps() {
        return entries.size()-1;
    }

    public int getRefuels() {
        return _route.getRefills();
    }

    public long getTime(){
        return _route.getTime();
    }

    public boolean isLoop(){
        return _route.isLoop();
    }

    public int getLands() {
        return _route.getLands();
    }

    public double getProfitByTonne(){
        return _route.getProfit()/_route.getCargo();
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
        Route route = Route.clone(_route);
        route.join(path);
        return copyFill(route);
    }

    public void add(int offset, OrderModel order){
        _route.add(offset, ModelFabric.get(order));
        refresh(offset);
    }

    public void remove(int offset, OrderModel order){
        _route.remove(offset, ModelFabric.get(order));
        refresh(offset);
    }

    public void clearOrders(int offset){
        _route.removeAllOrders(offset);
        refresh(offset);
    }

    public RouteModel add(SystemModel system){
        RouteEntryModel last = entries.get(entries.size()-1);
        StationModel fromStation = last.getStation();
        RouteModel path = market.getPath(fromStation.getSystem(), fromStation, system, ModelFabric.NONE_STATION);
        return add(path);
    }

    public RouteModel add(StationModel station){
        RouteEntryModel last = entries.get(entries.size()-1);
        StationModel fromStation = last.getStation();
        RouteModel path = market.getPath(fromStation.getSystem(), fromStation, station.getSystem(), station);
        return add(path);
    }

    public RouteModel add(RouteModel route){
        Route res = Route.clone(_route);
        res.join(ModelFabric.get(route));
        return copyFill(res);
    }

    public RouteModel set(int offset, RouteModel route){
        Route res = Route.clone(_route);
        res.dropTo(offset);
        res.join(ModelFabric.get(route));
        return copyFill(res, offset);
    }

    public RouteModel dropLast(){
        Route res = Route.clone(_route);
        res.dropTo(entries.size()-2);
        return copyFill(res, entries.size()-2);
    }

    public RouteModel remove(OrderModel order) {
        Route res = Route.clone(_route);
        res.dropTo(ModelFabric.get(order.getStation()));
        return copyFill(res);
    }

    public boolean add(int offset, MissionModel mission){
        mission = MissionModel.copy(mission);
        int completeIndex = -1;
        Offer offer = mission.getOffer();
        if (offer != null){
            Collection<RouteReserve> reserves = RouteFiller.getReserves(_route, offset, offer);
            if (!reserves.isEmpty()) {
                _route.reserve(reserves);
                mission.setReserves(reserves);
                completeIndex = RouteReserve.getCompleteIndex(reserves, offset);
                refresh();
            }
        } else
        if (mission.isDelivery()){
            RouteReserve reserve = RouteFiller.getReserves(_route, offset, ModelFabric.get(mission.getTarget()), mission.getCount());
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
            completeIndex = _route.find(ModelFabric.get(mission.getTarget()), offset+1);
        }
        if (completeIndex != -1){
            entries.get(completeIndex).add(mission);
            return true;
        }
        return false;
    }

    public Collection<MissionModel> addAll(int offset, Collection<MissionModel> missions){
        Collection<MissionModel> notAdded = new ArrayList<>();
        for (MissionModel m : missions) {
            MissionModel mission = MissionModel.copy(m);
            Offer offer = mission.getOffer();
            int completeIndex = -1;
            if (offer != null){
                Collection<RouteReserve> reserves;
                if (m.getReserves() != null){
                    reserves = RouteFiller.changeReserves(_route, offset, offer, m.getReserves());
                } else {
                    reserves = RouteFiller.getReserves(_route, offset, offer);
                }
                if (!reserves.isEmpty()) {
                    _route.reserve(reserves);
                    mission.setReserves(reserves);
                    completeIndex = RouteReserve.getCompleteIndex(reserves, offset);
                }
            } else
            if (mission.isDelivery()){
                RouteReserve reserve = RouteFiller.getReserves(_route, offset, ModelFabric.get(mission.getTarget()), mission.getCount());
                if (reserve != null) {
                    _route.reserve(reserve);
                    mission.setReserves(Collections.singleton(reserve));
                    completeIndex = reserve.getToIndex();
                }
            } else
            if (mission.isCourier()){
                completeIndex = _route.find(ModelFabric.get(mission.getTarget()), offset+1);
            }
            if (completeIndex != -1){
                if (completeIndex == 0 && _route.isLoop()) completeIndex = _route.getJumps()-1;
                entries.get(completeIndex).add(mission);
            } else {
                notAdded.add(mission);
            }
        }
        refresh();
        return notAdded;
    }

    public void remove(MissionModel mission){
        Collection<RouteReserve> reserves = mission.getReserves();
        if (reserves != null) {
            _route.unreserve(reserves);
        }
        for (RouteEntryModel entry : entries) {
            entry.remove(mission);
        }
        refresh();
    }


    public void removeAll(Collection<MissionModel> missions){
        for (MissionModel mission : missions) {
            Collection<RouteReserve> reserves = mission.getReserves();
            if (reserves != null) {
                _route.unreserve(reserves);
            }
            for (RouteEntryModel entry : entries) {
                entry.remove(mission);
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
                .filter(station -> !ModelFabric.isFake(station))
                .forEach(res::add);
        return res;
    }

    public Collection<MissionModel> getMissions(int offset){
        int startIndex = offset+1;
        if (startIndex >= entries.size()) return Collections.emptyList();
        List<MissionModel> res = entries.subList(startIndex, entries.size()).stream()
                .flatMap(e -> e.missions().stream())
                .collect(Collectors.toList());
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

    public double getBalance(int index){
        int endIndex = index+1;
        if (endIndex > entries.size()) endIndex = entries.size();
        double balance = _route.getBalance();
        balance -= entries.subList(0, endIndex).stream().flatMap(e -> e.orders().stream()).mapToDouble(OrderModel::getCredit).sum();
        balance += entries.subList(0, endIndex).stream().flatMap(e -> e.sellOrders().stream()).mapToDouble(OrderModel::getDebet).sum();
        return balance;
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
            complete();
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
        for (int i = index; i < entries.size(); i++) {
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
        if (index == entries.size()-1){
            removeCompletedMissions();
            if (isLoop()) setCurrentEntry(0);
        }
    }

    private void removeCompletedMissions(){
        boolean needRefresh = false;
        for (RouteEntryModel entry : entries) {
            Collection<MissionModel> missions = new ArrayList<>(entry.missions());
            for (MissionModel mission : missions) {
                if (mission.isCompleted()) {
                    Collection<RouteReserve> reserves = mission.getReserves();
                    if (reserves != null) {
                        needRefresh = true;
                        _route.unreserve(reserves);
                    }
                    entry.remove(mission);
                }
            }
        }
        if (needRefresh){
            refresh();
        }
    }

    public boolean isEnd(){
        return getCurrentEntry() == entries.size()-1;
    }

    private void refresh(int index){
        RouteEntryModel entry = get(index);
        entry.refresh(market);
        for (RouteEntryModel e : entries) {
            e.sellOrders().clear();
        }
        fillSellOrders();
    }

    private void refresh(){
        for (RouteEntryModel entry : entries) {
            entry.sellOrders().clear();
            entry.refresh(market);
        }
        fillSellOrders();
    }

    public static RouteModel asRoute(SystemModel system, ProfileModel profile){
        Route route = Route.singletone(ModelFabric.get(system).asTransit(), profile.getBalance(), profile.getShipCargo());
        return new RouteModel(route, system.getMarket());
    }

    public static RouteModel asRoute(StationModel station, ProfileModel profile){
        Route route = Route.singletone(ModelFabric.get(station), profile.getBalance(), profile.getShipCargo());
        return new RouteModel(route, station.getMarket());
    }

    public String asString(){
        StringBuilder builder = new StringBuilder();
        for (RouteEntryModel entry : entries) {
            for (OrderModel order : entry.orders()) {
                if (builder.length()>0) builder.append("\n");
                builder.append(order.asString());
            }
        }
        builder.append("\n");
        builder.append(String.format(Localization.getString("routes.text.format"), getProfitByTonne(), ViewUtils.timeToString(getTime())));
        return builder.toString();
    }

}
