package ru.trader.model;

import javafx.application.Platform;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.World;
import ru.trader.analysis.CrawlerSpecificator;
import ru.trader.analysis.Route;
import ru.trader.controllers.MainController;
import ru.trader.controllers.ProgressController;
import ru.trader.controllers.Screeners;
import ru.trader.core.*;
import ru.trader.model.support.BindingsHelper;
import ru.trader.model.support.Notificator;
import ru.trader.services.OrdersSearchTask;
import ru.trader.services.RoutesSearchTask;
import ru.trader.view.support.Localization;
import ru.trader.view.support.autocomplete.StationsProvider;
import ru.trader.view.support.autocomplete.SystemsProvider;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;


public class MarketModel {
    private final static Logger LOG = LoggerFactory.getLogger(MarketModel.class);

    private final Market market;
    private final MarketAnalyzer analyzer;
    private final ModelFabric modeler;
    private final Notificator notificator;

    private final ObservableList<String> systemNames;
    private final ObservableList<String> stationNames;
    private final SystemsProvider systemsProvider;
    private final StationsProvider stationsProvider;
    private final ListProperty<GroupModel> groups;
    private final ListProperty<ItemModel> items;

    public MarketModel(Market market) {
        this.market = market;
        analyzer = World.buildAnalyzer(market);
        modeler = new ModelFabric(this);
        notificator = new Notificator();
        groups = new SimpleListProperty<>(BindingsHelper.observableList(market.getGroups(), modeler::get));
        items = new SimpleListProperty<>(BindingsHelper.observableList(market.getItems(), modeler::get));
        items.sort(ItemModel::compareTo);
        systemNames = new SimpleListProperty<>(FXCollections.observableArrayList(market.getPlaceNames()));
        stationNames = new SimpleListProperty<>(FXCollections.observableArrayList(market.getVendorNames()));
        systemsProvider = new SystemsProvider(this);
        stationsProvider = new StationsProvider(this);
    }

    public MarketAnalyzer getAnalyzer() {
        return analyzer;
    }

    public ModelFabric getModeler() {
        return modeler;
    }

    public Notificator getNotificator() {
        return notificator;
    }

    public ObservableList<String> getSystemNames() {
        return systemNames;
    }

    public ObservableList<String> getStationNames() {
        return stationNames;
    }

    public SystemsProvider getSystemsProvider() {
        return systemsProvider;
    }

    public StationsProvider getStationsProvider() {
        return stationsProvider;
    }

    public SystemModel get(String name){
        Place s = market.get(name);
        if (s == null){
            return ModelFabric.NONE_SYSTEM;
        }
        return modeler.get(s);
    }

    public SystemModel getNear(double x, double y, double z, double xlimit, double ylimit, double zlimit){
        Place s = market.getNear(x, y, z, xlimit, ylimit, zlimit);
        if (s == null){
            return ModelFabric.NONE_SYSTEM;
        }
        return modeler.get(s);
    }

    public SystemModel add(String name, double x, double y, double z) {
        SystemModel system = modeler.get(market.addPlace(name, x, y, z));
        LOG.info("Add system {} to market {}", system, this);
        notificator.sendAdd(system);
        systemNames.add(system.getName());
        stationNames.addAll(system.getStationFullNames());
        return system;
    }

    public void remove(SystemModel system) {
        LOG.info("Remove system {} from market {}", system, this);
        notificator.sendRemove(system);
        stationNames.removeAll(system.getStationFullNames());
        market.remove(ModelFabric.get(system));
        systemNames.remove(system.getName());
    }

    StationModel addStation(SystemModel system, String name) {
        StationModel station = modeler.get(ModelFabric.get(system).addVendor(name));
        LOG.info("Add station {} to system {}", station, system);
        stationNames.add(station.getFullName());
        notificator.sendAdd(station);
        return station;
    }

    void removeStation(StationModel station) {
        LOG.info("Remove station {} from system {}", station, station.getSystem());
        notificator.sendRemove(station);
        stationNames.remove(station.getFullName());
        ModelFabric.get(station.getSystem()).remove(ModelFabric.get(station));
    }

    public ReadOnlyListProperty<GroupModel> getGroups(){
        return groups;
    }

    public GroupModel addGroup(String name, GROUP_TYPE type){
        GroupModel group = modeler.get(market.addGroup(name, type));
        LOG.info("Add group {} to market {}", group, this);
        groups.add(group);
        return group;
    }

    public ReadOnlyListProperty<ItemModel> itemsProperty() {
        return items;
    }

    public Optional<ItemModel> getItem(String id){
        return Optional.ofNullable(modeler.get(market.getItem(id)));
    }

    public ItemModel add(String name, GroupModel group) {
        ItemModel item = modeler.get(market.addItem(name, ModelFabric.get(group)));
        LOG.info("Add item {} to market {}", item, this);
        notificator.sendAdd(item);
        items.add(item);
        return item;
    }

    public void remove(ItemModel item) {
        LOG.info("Remove item {} from market {}", item, this);
        market.remove(ModelFabric.get(item));
        notificator.sendRemove(item);
        items.remove(item);
    }

    ItemStat getStat(OFFER_TYPE type, Item item){
        return market.getStat(type, item);
    }

    public ObservableList<OfferModel> getOffers(OFFER_TYPE offerType, ItemModel item, MarketFilter filter){
        return BindingsHelper.observableList(analyzer.getOffers(offerType, ModelFabric.get(item), filter), modeler::get);
    }

    public ObservableList<StationModel> getStations(){
        return BindingsHelper.observableList(market.getVendors(), modeler::get);
    }

    public ObservableList<StationModel> getStations(MarketFilter filter){
        return BindingsHelper.observableList(analyzer.getVendors(filter), modeler::get);
    }

    public ObservableList<SystemModel> getSystems(){
        return BindingsHelper.observableList(market.get(), modeler::get);
    }

    public ObservableList<SystemModel> getSystems(MarketFilter filter){
        return BindingsHelper.observableList(analyzer.getSystems(filter), modeler::get);
    }

    public void getOrders(StationModel from, Profile profile, Consumer<ObservableList<OrderModel>> result) {
        getOrders(ModelFabric.NONE_SYSTEM, from, ModelFabric.NONE_SYSTEM, ModelFabric.NONE_STATION, profile, result);
    }

    public void getOrders(SystemModel from, StationModel stationFrom, SystemModel to, StationModel stationTo, Profile profile, Consumer<ObservableList<OrderModel>> result) {
        ProgressController progress = new ProgressController(Screeners.getMainScreen(), Localization.getString("analyzer.orders.title"));
        OrdersSearchTask task = new OrdersSearchTask(this,
                ModelFabric.get(from), ModelFabric.get(stationFrom), ModelFabric.get(to), ModelFabric.get(stationTo),
                profile
        );

        progress.run(task, order -> {
            ObservableList<OrderModel> res = BindingsHelper.observableList(order, modeler::get);
            if (Platform.isFxApplicationThread()) {
                result.accept(res);
            } else {
                Platform.runLater(() -> result.accept(res));
            }
        });
    }

    public void getOrders(StationModel seller, Collection<StationModel> buyers, Profile profile, Consumer<ObservableList<OrderModel>> result) {
        ProgressController progress = new ProgressController(Screeners.getMainScreen(), Localization.getString("analyzer.orders.title"));
        List<Vendor> vendors = buyers.stream().map(ModelFabric::get).collect(Collectors.toList());
        OrdersSearchTask task = new OrdersSearchTask(this, ModelFabric.get(seller), vendors, profile);
        progress.run(task, order -> {
            ObservableList<OrderModel> res = BindingsHelper.observableList(order, modeler::get);
            if (Platform.isFxApplicationThread()) {
                result.accept(res);
            } else {
                Platform.runLater(() -> result.accept(res));
            }
        });
    }

    public void getTop(double balance, Consumer<ObservableList<OrderModel>> result){
        Profile profile = Profile.clone(ModelFabric.get(MainController.getProfile()));
        profile.setBalance(balance);
        getOrders(ModelFabric.NONE_SYSTEM, ModelFabric.NONE_STATION, ModelFabric.NONE_SYSTEM, ModelFabric.NONE_STATION, profile, result);
    }

    public void getRoutes(StationModel stationFrom, StationModel stationTo, double balance, CrawlerSpecificator specificator, Consumer<ObservableList<RouteModel>> result) {
        getRoutes(stationFrom.getSystem(), stationFrom, stationTo.getSystem(), stationTo, balance, specificator, result);
    }


    public void getRoutes(SystemModel from, StationModel stationFrom, SystemModel to, StationModel stationTo, double balance, CrawlerSpecificator specificator, Consumer<ObservableList<RouteModel>> result) {
        Profile profile = Profile.clone(ModelFabric.get(MainController.getProfile()));
        profile.setBalance(balance);
        getRoutes(from, stationFrom, to, stationTo, profile, specificator, result);
    }

    public void getRoutes(SystemModel from, StationModel stationFrom, SystemModel to, StationModel stationTo, Profile profile, CrawlerSpecificator specificator, Consumer<ObservableList<RouteModel>> result) {
        ProgressController progress = new ProgressController(Screeners.getMainScreen(), Localization.getString("analyzer.routes.title"));
        RoutesSearchTask task = new RoutesSearchTask(this,
                ModelFabric.get(from), ModelFabric.get(stationFrom), ModelFabric.get(to), ModelFabric.get(stationTo),
                profile, specificator
        );

        progress.run(task, route -> {
            ObservableList<RouteModel> res = BindingsHelper.observableList(route, modeler::get);
            if (Platform.isFxApplicationThread()){
                result.accept(res);
            } else {
                Platform.runLater(() -> result.accept(res));
            }
        });
    }

    public void getTopRoutes(double balance, Consumer<ObservableList<RouteModel>> result){
        getRoutes(ModelFabric.NONE_SYSTEM, ModelFabric.NONE_STATION, ModelFabric.NONE_SYSTEM, ModelFabric.NONE_STATION, balance, new CrawlerSpecificator(), result);
    }

    public RouteModel getRoute(RouteModel path) {
        Route r = analyzer.getRoute(ModelFabric.get(path).getVendors());
        if (r == null) return null;
        return modeler.get(r);
    }

    Route _getPath(OrderModel order) {
        return analyzer.getPath(ModelFabric.get(order));
    }

    private RouteModel getPath(Vendor from, Vendor to) {
        Route p = analyzer.getPath(from, to);
        return modeler.get(p);
    }

    public RouteModel getPath(StationModel from, StationModel to) {
        return getPath(ModelFabric.get(from), ModelFabric.get(to));
    }

    public RouteModel getPath(SystemModel from, StationModel stationFrom, SystemModel to, StationModel stationTo){
        if (ModelFabric.isFake(stationFrom)){
            return getPath(ModelFabric.get(from).asTransit(), ModelFabric.isFake(stationTo) ? ModelFabric.get(to).asTransit() : ModelFabric.get(stationTo));
        } else {
            return getPath(ModelFabric.get(stationFrom), ModelFabric.isFake(stationTo) ? ModelFabric.get(to).asTransit() : ModelFabric.get(stationTo));
        }
    }

    public RouteModel getPath(OrderModel order) {
        Route p = analyzer.getPath(ModelFabric.get(order));
        return modeler.get(p);
    }

    public void clear(){
        LOG.info("Clear market");
        market.clear();
    }

    public void clearOffers(){
        LOG.info("Clear offers");
        market.clearOffers();
    }

    public void clearStations(){
        LOG.info("Clear stations");
        market.clearVendors();
    }

    public void clearSystems(){
        LOG.info("Clear systems");
        market.clearPlaces();
    }

    public void clearItems(){
        LOG.info("Clear items");
        market.clearItems();
    }

    public void clearGroups(){
        LOG.info("Clear groups");
        market.clearGroups();
    }

    public void refresh(){
        LOG.debug("Refresh names");
        groups.get().forEach(GroupModel::updateName);
        items.get().forEach(ItemModel::updateName);
        items.sort(ItemModel::compareTo);
    }
}
