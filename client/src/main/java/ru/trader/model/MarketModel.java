package ru.trader.model;

import javafx.application.Platform;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.World;
import ru.trader.analysis.AnalysisCallBack;
import ru.trader.analysis.Route;
import ru.trader.controllers.ProgressController;
import ru.trader.controllers.Screeners;
import ru.trader.core.*;
import ru.trader.model.support.BindingsHelper;
import ru.trader.model.support.Notificator;
import ru.trader.services.OrdersSearchTask;
import ru.trader.services.RoutesSearchTask;
import ru.trader.view.support.Localization;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;


public class MarketModel {
    private final static Logger LOG = LoggerFactory.getLogger(MarketModel.class);

    private final Market market;
    private final MarketAnalyzer analyzer;
    private final ModelFabric modeler;
    private final Notificator notificator;

    private final ListProperty<SystemModel> systems;
    // with NONE_SYSTEM
    private ListProperty<SystemModel> systemsList;
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
        systems = new SimpleListProperty<>(BindingsHelper.observableList(market.get(), modeler::get));
        systemsList = new SimpleListProperty<>(FXCollections.observableArrayList(ModelFabric.NONE_SYSTEM));
        systemsList.addAll(systems);
        systems.addListener(SYSTEMS_CHANGE_LISTENER);
    }

    private ListChangeListener<SystemModel> SYSTEMS_CHANGE_LISTENER = l -> {
        while (l.next()) {
            if (l.wasRemoved()) {
                systemsList.removeAll(l.getRemoved());
            }
            if (l.wasAdded()) {
                systemsList.addAll(l.getAddedSubList());
            }
        }
    };

    public MarketAnalyzer getAnalyzer() {
        return analyzer;
    }

    public MarketAnalyzer getAnalyzer(AnalysisCallBack callback) {
        return analyzer.changeCallBack(callback);
    }

    public ModelFabric getModeler() {
        return modeler;
    }

    public Notificator getNotificator() {
        return notificator;
    }

    public ReadOnlyListProperty<SystemModel> systemsProperty() {
        return systems;
    }
    public ReadOnlyListProperty<SystemModel> systemsListProperty() {
        return systemsList;
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
        systems.add(system);
        return system;
    }

    public void remove(SystemModel system) {
        LOG.info("Remove system {} from market {}", system, this);
        notificator.sendRemove(system);
        market.remove(system.getSystem());
        systems.remove(system);
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

    public Collection<StationModel> getStations(MarketFilter filter){
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
        OrdersSearchTask task = new OrdersSearchTask(this,
                from == null || from == ModelFabric.NONE_SYSTEM ? null : from.getSystem(),
                stationFrom == null || stationFrom == ModelFabric.NONE_STATION ? null : stationFrom.getStation(),
                to == null || to == ModelFabric.NONE_SYSTEM ? null : to.getSystem(),
                stationTo == null || stationTo == ModelFabric.NONE_STATION ? null : stationTo.getStation(),
                balance
        );

        progress.run(task, order -> {
            ObservableList<OrderModel> res = BindingsHelper.observableList(order, modeler::get);
            if (Platform.isFxApplicationThread()){
                result.accept(res);
            } else {
                Platform.runLater(() -> result.accept(res));
            }
        });
    }

    public void getTop(double balance, Consumer<ObservableList<OrderModel>> result){
        getOrders(ModelFabric.NONE_SYSTEM, ModelFabric.NONE_STATION, ModelFabric.NONE_SYSTEM, ModelFabric.NONE_STATION, balance, result);
    }

    public void getRoutes(SystemModel from, double balance, Consumer<ObservableList<RouteModel>> result){
        getRoutes(from, ModelFabric.NONE_STATION, ModelFabric.NONE_SYSTEM, ModelFabric.NONE_STATION, balance, result);
    }

    public void getRoutes(SystemModel from, SystemModel to, double balance, Consumer<ObservableList<RouteModel>> result){
        getRoutes(from, ModelFabric.NONE_STATION, to, ModelFabric.NONE_STATION, balance, result);
    }

    public void getRoutes(SystemModel from, StationModel stationFrom, SystemModel to, StationModel stationTo, double balance, Consumer<ObservableList<RouteModel>> result) {
        ProgressController progress = new ProgressController(Screeners.getMainScreen(), Localization.getString("analyzer.routes.title"));
        RoutesSearchTask task = new RoutesSearchTask(this,
                from == null || from == ModelFabric.NONE_SYSTEM ? null : from.getSystem(),
                stationFrom == null || stationFrom == ModelFabric.NONE_STATION ? null : stationFrom.getStation(),
                to == null || to == ModelFabric.NONE_SYSTEM ? null : to.getSystem(),
                stationTo == null || stationTo == ModelFabric.NONE_STATION ? null : stationTo.getStation(),
                balance
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
        getRoutes(ModelFabric.NONE_SYSTEM, ModelFabric.NONE_STATION, ModelFabric.NONE_SYSTEM, ModelFabric.NONE_STATION, balance, result);
    }

    public RouteModel getRoute(RouteModel path, double balance) {
        //TODO: implement
        /*Route r = analyzer.getRoute(path.getRoute());
        if (r == null) return null;
        return modeler.get(r);*/
        return null;
    }

    Route _getPath(OrderModel order) {
        return analyzer.getPath(order.getOrder());
    }

    public RouteModel getPath(StationModel from, StationModel to) {
        Route p = analyzer.getPath(from.getStation(), to.getStation());
        return modeler.get(p);
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
