package ru.trader.model;

import javafx.beans.property.ListProperty;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.World;
import ru.trader.core.*;
import ru.trader.graph.PathRoute;
import ru.trader.model.support.BindingsHelper;
import ru.trader.model.support.Notificator;


public class MarketModel {
    private final static Logger LOG = LoggerFactory.getLogger(MarketModel.class);

    private final Market market;
    private final MarketAnalyzer analyzer;
    private final ModelFabric modeler;
    private final Notificator notificator;

    private final ListProperty<SystemModel> systems;
    private final ListProperty<ItemModel> items;

    public MarketModel(Market market) {
        this.market = market;
        analyzer = World.buildAnalyzer(market);
        modeler = new ModelFabric(this);
        notificator = new Notificator();
        items = new SimpleListProperty<>(BindingsHelper.observableList(market.getItems(), modeler::get));
        systems = new SimpleListProperty<>(BindingsHelper.observableList(market.get(), modeler::get));
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

    public ReadOnlyListProperty<SystemModel> systemsProperty() {
        return systems;
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
        return BindingsHelper.observableList(analyzer.getOrders(from.getSystem(), balance), modeler::get);
    }

    public ObservableList<OrderModel> getOrders(SystemModel from, SystemModel to, double balance) {
        return BindingsHelper.observableList(analyzer.getOrders(from.getSystem(), to.getSystem(), balance), modeler::get);
    }

    public ObservableList<OrderModel> getOrders(StationModel from, StationModel to, double balance) {
        return BindingsHelper.observableList(analyzer.getOrders(from.getStation(), to.getStation(), balance), modeler::get);
    }

    public ObservableList<OrderModel> getTop(double balance){
        return BindingsHelper.observableList(analyzer.getTop(balance), modeler::get);
    }

    public ObservableList<PathRouteModel> getRoutes(SystemModel from, double balance){
        return BindingsHelper.observableList(analyzer.getPaths(from.getSystem(), balance), modeler::get);
    }

    public ObservableList<PathRouteModel> getRoutes(SystemModel from, SystemModel to, double balance){
        return BindingsHelper.observableList(analyzer.getPaths(from.getSystem(), to.getSystem(), balance), modeler::get);
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
