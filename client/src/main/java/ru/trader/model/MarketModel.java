package ru.trader.model;

import javafx.beans.property.ListProperty;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.core.*;
import ru.trader.graph.PathRoute;
import ru.trader.model.support.BindingsHelper;
import ru.trader.model.support.ChangeMarketListener;

import java.util.ArrayList;
import java.util.Collection;


public class MarketModel {
    private final static Logger LOG = LoggerFactory.getLogger(MarketModel.class);

    private final Market market;
    private final MarketAnalyzer analyzer;

    private final Collection<ChangeMarketListener> listener = new ArrayList<>();

    private final ListProperty<VendorModel> vendors;
    private final ListProperty<ItemDescModel> items;

    private boolean alert = true;

    public ReadOnlyListProperty<VendorModel> vendorsProperty() {
        return vendors;
    }

    public ReadOnlyListProperty<ItemDescModel> itemsProperty() {
        return items;
    }

    public void setAlert(boolean alert) {
        this.alert = alert;
    }

    public MarketModel(Market market) {
        this.market = market;
        analyzer = new MarketAnalyzer(market);
        items = new SimpleListProperty<ItemDescModel>(BindingsHelper.observableList(market.getItems(), this::getItemDesc));
        vendors = new SimpleListProperty<VendorModel>(BindingsHelper.observableList(market.get(), this::asModel));
    }

    public void addListener(ChangeMarketListener listener){
        synchronized (this.listener){
            this.listener.add(listener);
        }
    }

    public void removeListeners() {
        synchronized (listener){
            listener.clear();
        }
    }

    public void addAllListener(Collection<? extends ChangeMarketListener> listener){
        synchronized (this.listener){
            this.listener.addAll(listener);
        }
    }

    public Collection<ChangeMarketListener> getListeners() {
        return listener;
    }

    void updatePosition(VendorModel model, double x, double y, double z) {
        Vendor vendor = model.getVendor();
        double oldX = vendor.getX();
        double oldY = vendor.getY();
        double oldZ = vendor.getZ();
        market.updatePosition(vendor, x, y, z);
        if (alert) listener.forEach((c) -> c.positionChange(model, oldX, oldY, oldZ, x, y, z));
    }

    void updateName(ItemModel model, String value) {
        Item item = model.getItem();
        String old = item.getName();
        market.updateName(item, value);
        if (alert) listener.forEach((c) -> c.nameChange(model, old, value));
    }

    void updateName(VendorModel model, String value) {
        Vendor vendor = model.getVendor();
        String old = vendor.getName();
        market.updateName(vendor, value);
        if (alert) listener.forEach((c) -> c.nameChange(model, old, value));
    }

    void updatePrice(OfferModel model, double value) {
        Offer offer = model.getOffer();
        double old = offer.getPrice();
        market.updatePrice(offer, value);
        if (alert) listener.forEach((c) -> c.priceChange(model, old, value));
    }

    void add(VendorModel vendor, OfferModel offer) {
        market.add(vendor.getVendor(), offer.getOffer());
        if (alert) listener.forEach((c) -> c.add(offer));
    }

    void remove(VendorModel vendor, OfferModel offer) {
        market.remove(vendor.getVendor(), offer.getOffer());
        if (alert) listener.forEach((c) -> c.remove(offer));
    }

    public void add(VendorModel vendor) {
        LOG.info("Add vendor {} to market {}", vendor, this);
        market.add(vendor.getVendor());
        if (alert) listener.forEach((c) -> c.add(vendor));
        vendors.add(vendor);
    }

    public void add(ItemModel item) {
        LOG.info("Add item {} to market {}", item, this);
        market.add(item.getItem());
        ItemDescModel model = getItemDesc(item);
        if (alert) listener.forEach((c) -> c.add(model));
        items.add(model);
    }

    public ItemModel newItem(String name){
        return ModelFabrica.buildItemModel(name, this);
    }

    public VendorModel newVendor(String name){
        return ModelFabrica.buildModel(name, this);
    }

    public OfferModel newOffer(OFFER_TYPE type, ItemModel item, double price) {
        return ModelFabrica.buildModel(type, item, price, this);
    }

    ItemDescModel getItemDesc(Item item){
        return getItemDesc(asModel(item));
    }

    ItemDescModel getItemDesc(ItemModel item){
        return ModelFabrica.buildModel(item, market.getStatSell(item.getItem()), market.getStatBuy(item.getItem()), this);
    }

    public OfferDescModel asOfferDescModel(Offer offer){
        return asOfferDescModel(asModel(offer));
    }

    public OfferDescModel asOfferDescModel(OfferModel offer){
        Item item = offer.getOffer().getItem();
        return ModelFabrica.buildModel(offer, market.getStatSell(item), market.getStatBuy(item), this);
    }

    ItemStat getStat(OFFER_TYPE type, Item item){
        return market.getStat(type, item);
    }

    public OfferModel asModel(Offer offer){
        return ModelFabrica.getModel(offer, this);
    }

    public ItemModel asModel(Item item){
        return ModelFabrica.getModel(item, this);
    }

    public VendorModel asModel(Vendor vendor) {
        return ModelFabrica.getModel(vendor, this);
    }

    public OrderModel asModel(Order order) {
        return new OrderModel(asOfferDescModel(order.getSell()), asModel(order.getBuy()), order.getCount());
    }

    public PathRouteModel asModel(PathRoute path) {
        return new PathRouteModel(path, this);
    }


    public void setCargo(int cargo){
        analyzer.setCargo(cargo);
    }

    public void setTank(double tank){
        analyzer.setTank(tank);
    }

    public void setJumps(int jumps){
        analyzer.setJumps(jumps);
    }

    public void setDistance(double distance){
        analyzer.setMaxDistance(distance);
    }

    public ObservableList<OrderModel> getOrders(VendorModel from, double balance) {
        return BindingsHelper.observableList(analyzer.getOrders(from.getVendor(), balance), this::asModel);
    }

    public ObservableList<OrderModel> getOrders(VendorModel from, VendorModel to, double balance) {
        return BindingsHelper.observableList(analyzer.getOrders(from.getVendor(), to.getVendor(), balance), this::asModel);
    }

    public ObservableList<OrderModel> getTop(int limit, double balance){
        return BindingsHelper.observableList(analyzer.getTop(limit, balance), this::asModel);
    }

    public ObservableList<PathRouteModel> getRoutes(VendorModel from, double balance){
        return BindingsHelper.observableList(analyzer.getPaths(from.getVendor(), balance), this::asModel);
    }

    public ObservableList<PathRouteModel> getRoutes(VendorModel from, VendorModel to, double balance){
        return BindingsHelper.observableList(analyzer.getPaths(from.getVendor(), to.getVendor(), balance), this::asModel);
    }

    public ObservableList<PathRouteModel> getTopRoutes(double balance){
        return BindingsHelper.observableList(analyzer.getTopPaths(100, balance), this::asModel);
    }

    PathRoute getPath(VendorModel from, VendorModel to) {
        return analyzer.getPath(from.getVendor(), to.getVendor());
    }

    public PathRouteModel getPath(OrderModel order) {
        PathRoute p = analyzer.getPath(order.getVendor().getVendor(), order.getBuyer().getVendor());
        if (p == null) return null;
        p.getRoot().getNext().setOrder(new Order(order.getOffer().getOffer(), order.getBuyOffer().getOffer(), order.getCount()));
        order.setPath(p);
        return asModel(p);
    }

}
