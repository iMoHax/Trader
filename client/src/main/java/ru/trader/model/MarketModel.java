package ru.trader.model;

import javafx.beans.property.ListProperty;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.World;
import ru.trader.core.*;
import ru.trader.graph.PathRoute;
import ru.trader.model.support.BindingsHelper;
import ru.trader.model.support.Notificator;

import java.util.Collection;


public class MarketModel {
    private final static Logger LOG = LoggerFactory.getLogger(MarketModel.class);

    private final Market market;
    private final MarketAnalyzer analyzer;
    private final ModelFabric modeler;
    private final Notificator notificator;

    private final ListProperty<SystemModel> systems;
    // with NONE_SYSTEM
    private ListProperty<SystemModel> systemsList;
    private final ListProperty<ItemModel> items;

    public MarketModel(Market market) {
        this.market = market;
        analyzer = World.buildAnalyzer(market);
        modeler = new ModelFabric(this);
        notificator = new Notificator();
        items = new SimpleListProperty<>(BindingsHelper.observableList(market.getItems(), modeler::get));
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

    public SystemModel add(String name, double x, double y, double z) {
        SystemModel system = modeler.get(market.addPlace(name, x, y, z));
        LOG.info("Add system {} to market {}", system, this);
        notificator.sendAdd(system);
        systems.add(system);
        return system;
    }

    public ReadOnlyListProperty<ItemModel> itemsProperty() {
        return items;
    }

    public ItemModel add(String name, Group group) {
        ItemModel item = modeler.get(market.addItem(name, group));
        LOG.info("Add item {} to market {}", item, this);
        notificator.sendAdd(item);
        items.add(item);
        return item;
    }

    ItemStat getStat(OFFER_TYPE type, Item item){
        return market.getStat(type, item);
    }

    public ObservableList<OrderModel> getOrders(SystemModel from, double balance) {
        return getOrders(from, ModelFabric.NONE_STATION, ModelFabric.NONE_SYSTEM, ModelFabric.NONE_STATION, balance);
    }

    public ObservableList<OrderModel> getOrders(SystemModel from, SystemModel to, double balance) {
        return getOrders(from, ModelFabric.NONE_STATION, to, ModelFabric.NONE_STATION, balance);
    }

    public ObservableList<OrderModel> getOrders(StationModel from, StationModel to, double balance) {
        return getOrders(from.getSystem(), from, to.getSystem(), to, balance);
    }

    public ObservableList<OrderModel> getOrders(SystemModel from, StationModel stationFrom, SystemModel to, StationModel stationTo, double balance) {
        Collection<Order> orders;
        if (stationFrom != null && stationFrom != ModelFabric.NONE_STATION){
            if (stationTo != null && stationTo != ModelFabric.NONE_STATION){
                orders = analyzer.getOrders(stationFrom.getStation(), stationTo.getStation(), balance);
            } else {
                if (to != null && to != ModelFabric.NONE_SYSTEM){
                    orders = analyzer.getOrders(stationFrom.getStation(), to.getSystem(), balance);
                } else {
                    orders = analyzer.getOrders(stationFrom.getStation(), balance);
                }
            }
        } else {
            if (stationTo != null && stationTo != ModelFabric.NONE_STATION){
                orders = analyzer.getOrders(from.getSystem(), stationTo.getStation(), balance);
            } else {
                if (to != null && to != ModelFabric.NONE_SYSTEM){
                    orders = analyzer.getOrders(from.getSystem(), to.getSystem(), balance);
                } else {
                    orders = analyzer.getOrders(from.getSystem(), balance);
                }
            }
        }
        return BindingsHelper.observableList(orders, modeler::get);
    }

    public ObservableList<OrderModel> getTop(double balance){
        return BindingsHelper.observableList(analyzer.getTop(balance), modeler::get);
    }

    public ObservableList<PathRouteModel> getRoutes(SystemModel from, double balance){
        return getRoutes(from, ModelFabric.NONE_STATION, ModelFabric.NONE_SYSTEM, ModelFabric.NONE_STATION, balance);
    }

    public ObservableList<PathRouteModel> getRoutes(SystemModel from, SystemModel to, double balance){
        return getRoutes(from, ModelFabric.NONE_STATION, to, ModelFabric.NONE_STATION, balance);
    }

    public ObservableList<PathRouteModel> getRoutes(SystemModel from, StationModel stationFrom, SystemModel to, StationModel stationTo, double balance) {
        Collection<PathRoute> routes;
        if (stationFrom != null && stationFrom != ModelFabric.NONE_STATION){
            if (stationTo != null && stationTo != ModelFabric.NONE_STATION){
                routes = analyzer.getPaths(stationFrom.getStation(), stationTo.getStation(), balance);
            } else {
                if (to != null && to != ModelFabric.NONE_SYSTEM){
                    routes = analyzer.getPaths(stationFrom.getStation(), to.getSystem(), balance);
                } else {
                    routes = analyzer.getPaths(stationFrom.getStation(), balance);
                }
            }
        } else {
            if (stationTo != null && stationTo != ModelFabric.NONE_STATION){
                routes = analyzer.getPaths(from.getSystem(), stationTo.getStation(), balance);
            } else {
                if (to != null && to != ModelFabric.NONE_SYSTEM){
                    routes = analyzer.getPaths(from.getSystem(), to.getSystem(), balance);
                } else {
                    routes = analyzer.getPaths(from.getSystem(), balance);
                }
            }
        }
        return BindingsHelper.observableList(routes, modeler::get);
    }



    public ObservableList<PathRouteModel> getTopRoutes(double balance){
        return BindingsHelper.observableList(analyzer.getTopPaths(balance), modeler::get);
    }

    PathRoute getPath(StationModel from, StationModel to) {
        return analyzer.getPath(from.getStation(), to.getStation());
    }

    public PathRouteModel getPath(OrderModel order) {
        PathRoute p = analyzer.getPath(order.getStation().getStation(), order.getBuyer().getStation());
        if (p == null) return null;
        p.getRoot().getNext().setOrder(new Order(order.getOffer().getOffer(), order.getBuyOffer().getOffer(), order.getCount()));
        return modeler.get(p);
    }

}
