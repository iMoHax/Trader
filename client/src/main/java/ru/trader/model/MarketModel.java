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

import java.util.Optional;
import java.util.function.Consumer;


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
        market.remove(system.getSystem());
        systemNames.remove(system.getName());
    }

    StationModel addStation(SystemModel system, String name) {
        StationModel station = modeler.get(system.getSystem().addVendor(name));
        LOG.info("Add station {} to system {}", station, system);
        stationNames.add(station.getFullName());
        notificator.sendAdd(station);
        return station;
    }

    void removeStation(StationModel station) {
        LOG.info("Remove station {} from system {}", station, station.getSystem());
        notificator.sendRemove(station);
        stationNames.remove(station.getFullName());
        station.getSystem().getSystem().remove(station.getStation());
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
        ItemModel item = modeler.get(market.addItem(name, group.getGroup()));
        LOG.info("Add item {} to market {}", item, this);
        notificator.sendAdd(item);
        items.add(item);
        return item;
    }

    ItemStat getStat(OFFER_TYPE type, Item item){
        return market.getStat(type, item);
    }

    public ObservableList<OfferModel> getOffers(OFFER_TYPE offerType, ItemModel item, MarketFilter filter){
        return BindingsHelper.observableList(analyzer.getOffers(offerType, item.getItem(), filter), modeler::get);
    }

    public ObservableList<StationModel> getStations(MarketFilter filter){
        return BindingsHelper.observableList(analyzer.getVendors(filter), modeler::get);
    }

    public void getOrders(SystemModel from, double balance, Consumer<ObservableList<OrderModel>> result) {
        getOrders(from, ModelFabric.NONE_STATION, ModelFabric.NONE_SYSTEM, ModelFabric.NONE_STATION, balance, result);
    }

    public void getOrders(SystemModel from, SystemModel to, double balance, Consumer<ObservableList<OrderModel>> result) {
        getOrders(from, ModelFabric.NONE_STATION, to, ModelFabric.NONE_STATION, balance, result);
    }

    public void getOrders(StationModel from, StationModel to, double balance, Consumer<ObservableList<OrderModel>> result) {
        getOrders(from.getSystem(), from, to.getSystem(), to, balance, result);
    }

    public void getOrders(SystemModel from, StationModel stationFrom, SystemModel to, StationModel stationTo, double balance, Consumer<ObservableList<OrderModel>> result) {
        ProgressController progress = new ProgressController(Screeners.getMainScreen(), Localization.getString("analyzer.orders.title"));
        Profile profile = MainController.getProfile().getProfile().copy();
        profile.setBalance(balance);
        OrdersSearchTask task = new OrdersSearchTask(this,
                from == null || from == ModelFabric.NONE_SYSTEM ? null : from.getSystem(),
                stationFrom == null || stationFrom == ModelFabric.NONE_STATION ? null : stationFrom.getStation(),
                to == null || to == ModelFabric.NONE_SYSTEM ? null : to.getSystem(),
                stationTo == null || stationTo == ModelFabric.NONE_STATION ? null : stationTo.getStation(),
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

    public void getTop(double balance, Consumer<ObservableList<OrderModel>> result){
        getOrders(ModelFabric.NONE_SYSTEM, ModelFabric.NONE_STATION, ModelFabric.NONE_SYSTEM, ModelFabric.NONE_STATION, balance, result);
    }

    public void getRoutes(SystemModel from, StationModel stationFrom, SystemModel to, StationModel stationTo, double balance, CrawlerSpecificator specificator, Consumer<ObservableList<RouteModel>> result) {
        ProgressController progress = new ProgressController(Screeners.getMainScreen(), Localization.getString("analyzer.routes.title"));
        Profile profile = MainController.getProfile().getProfile().copy();
        profile.setBalance(balance);
        RoutesSearchTask task = new RoutesSearchTask(this,
                from == null || from == ModelFabric.NONE_SYSTEM ? null : from.getSystem(),
                stationFrom == null || stationFrom == ModelFabric.NONE_STATION ? null : stationFrom.getStation(),
                to == null || to == ModelFabric.NONE_SYSTEM ? null : to.getSystem(),
                stationTo == null || stationTo == ModelFabric.NONE_STATION ? null : stationTo.getStation(),
                profile,
                specificator
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
        Route r = analyzer.getRoute(path.getRoute().getVendors());
        if (r == null) return null;
        return modeler.get(r);
    }

    Route _getPath(OrderModel order) {
        return analyzer.getPath(order.getOrder());
    }

    private RouteModel getPath(Vendor from, Vendor to) {
        Route p = analyzer.getPath(from, to);
        return modeler.get(p);
    }

    public RouteModel getPath(StationModel from, StationModel to) {
        return getPath(from.getStation(), to.getStation());
    }

    public RouteModel getPath(SystemModel from, StationModel stationFrom, SystemModel to, StationModel stationTo){
        if (ModelFabric.isFake(stationFrom)){
            return getPath(from.getSystem().asTransit(), ModelFabric.isFake(stationTo) ? to.getSystem().asTransit() : stationTo.getStation());
        } else {
            return getPath(stationFrom.getStation(), ModelFabric.isFake(stationTo) ? to.getSystem().asTransit() : stationTo.getStation());
        }
    }

    public RouteModel getPath(OrderModel order) {
        Route p = analyzer.getPath(order.getOrder());
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
