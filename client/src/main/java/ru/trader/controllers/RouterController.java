package ru.trader.controllers;


import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import ru.trader.Main;
import ru.trader.analysis.CrawlerSpecificator;
import ru.trader.model.*;
import ru.trader.model.support.ChangeMarketListener;
import ru.trader.view.support.NumberField;
import ru.trader.view.support.RouteNode;
import ru.trader.view.support.autocomplete.AutoCompletion;
import ru.trader.view.support.autocomplete.SystemsProvider;

import java.util.Optional;


public class RouterController {

    @FXML
    private NumberField balance;
    @FXML
    private NumberField cargo;
    @FXML
    private NumberField distance;
    @FXML
    private NumberField tank;
    @FXML
    private NumberField jumps;

    @FXML
    private ScrollPane path;

    @FXML
    private Button addBtn;

    @FXML
    private Button editBtn;

    @FXML
    private Button removeBtn;

    @FXML
    private TextField sourceText;
    private AutoCompletion<SystemModel> source;

    @FXML
    private ComboBox<StationModel> sStation;

    @FXML
    private TextField targetText;
    private AutoCompletion<SystemModel> target;

    @FXML
    private ComboBox<StationModel> tStation;

    @FXML
    private TableView<OrderModel> tblOrders;

    @FXML
    private NumberField totalProfit;

    @FXML
    private NumberField totalBalance;

    private MarketModel market;
    private RouteModel route;
    private final ObservableList<OrderModel> orders = FXCollections.observableArrayList();

    @FXML
    private void initialize(){
        init();
        balance.numberProperty().addListener((ov, o, n) -> {
            totalBalance.setValue(n);
            Main.SETTINGS.setBalance(n.doubleValue());
        });
        cargo.numberProperty().addListener((ov, o, n) -> Main.SETTINGS.setCargo(n.intValue()));
        tank.numberProperty().addListener((ov, o, n) -> Main.SETTINGS.setTank(n.doubleValue()));
        jumps.numberProperty().addListener((ov, o, n) -> Main.SETTINGS.setJumps(n.intValue()));
        source.completionProperty().addListener((ov, o, n) -> {
            if (n != null) {
                sStation.setItems(n.getStationsList());
            } else {
                sStation.setItems(FXCollections.emptyObservableList());
            }
        });
        target.completionProperty().addListener((ov, o, n) -> {
            if (n != null) {
                tStation.setItems(n.getStationsList());
            } else {
                tStation.setItems(FXCollections.emptyObservableList());
            }
        });


        balance.setOnAction((v)->cargo.requestFocus());
        cargo.setOnAction((v) -> tank.requestFocus());
        tank.setOnAction((v) -> distance.requestFocus());
        distance.setOnAction((v)->jumps.requestFocus());
        jumps.setOnAction((v)->balance.requestFocus());

        balance.setValue(Main.SETTINGS.getBalance());
        cargo.setValue(Main.SETTINGS.getCargo());
        tank.setValue(Main.SETTINGS.getTank());
        jumps.setValue(Main.SETTINGS.getJumps());

        addBtn.disableProperty().bind(Bindings.createBooleanBinding(()-> {
            SystemModel system = target.getCompletion();
            return ModelFabric.isFake(system);
        }, target.completionProperty()));

        editBtn.disableProperty().bind(tblOrders.getSelectionModel().selectedIndexProperty().isEqualTo(-1));
        removeBtn.disableProperty().bind(Bindings.createBooleanBinding(()-> {
            int sel = tblOrders.getSelectionModel().getSelectedIndex();
            return sel == -1 || sel != orders.size()-1;
        }, tblOrders.getSelectionModel().selectedIndexProperty()));

        tblOrders.setItems(orders);
        orders.addListener((ListChangeListener<OrderModel>) c -> {
            while (c.next()) {
                if (c.wasRemoved()) {
                    c.getRemoved().forEach(this::onRemove);
                }
                if (c.wasAdded()) {
                    c.getAddedSubList().forEach(this::onAdd);
                }
            }
        });
    }


    void init(){
        market = MainController.getMarket();
        market.getNotificator().add(new RouterChangeListener());
        SystemsProvider provider = new SystemsProvider(market);
        if (source != null) source.dispose();
        source = new AutoCompletion<>(sourceText, provider, ModelFabric.NONE_SYSTEM, provider.getConverter());
        if (target != null) target.dispose();
        provider = new SystemsProvider(market);
        target = new AutoCompletion<>(targetText, provider, ModelFabric.NONE_SYSTEM, provider.getConverter());
        orders.clear();
        totalBalance.setValue(balance.getValue());
        totalProfit.setValue(0);
    }


    private void onAdd(OrderModel order){
        totalProfit.add(order.getProfit());
        totalBalance.add(order.getProfit());
        source.setValue(order.getBuyer().getSystem());
        sStation.setValue(order.getBuyer());
        target.setValue(ModelFabric.NONE_SYSTEM);
        balance.setDisable(true);
    }

    private void onRemove(OrderModel order) {
        totalProfit.sub(order.getProfit());
        totalBalance.sub(order.getProfit());
        source.setValue(order.getSystem());
        sStation.setValue(order.getStation());
        target.setValue(ModelFabric.NONE_SYSTEM);
        if (orders.isEmpty()) {
            balance.setDisable(false);
        }
    }

    public void addStationToRoute(){
        SystemModel s = source.getCompletion();
        SystemModel t = target.getCompletion();
        StationModel sS = sStation.getValue();
        StationModel tS = tStation.getValue();
        RouteModel r = market.getPath(s, sS, t, tS);
        if (r == null) return;
        if (route != null){
            route.add(r);
        } else {
            route = r;
        }
        refreshPath();
        source.setValue(target.getCompletion());
        sStation.setValue(tS);
    }

    public void editOrders(){
        OrderModel sel = tblOrders.getSelectionModel().getSelectedItem();
        int index = tblOrders.getSelectionModel().getSelectedIndex();
        //TODO: implement
/*      market.getOrders(sel.getStation(), sel.getBuyer(), sel.getBalance(), result -> {
            Optional<OrderModel> order = Screeners.showOrders(result);
            if (order.isPresent()){
                orders.set(index, order.get());
            }

        });*/
    }

    public void removeSelected(){
        TableView.TableViewSelectionModel<OrderModel> select = tblOrders.getSelectionModel();
        if (!select.isEmpty()){
            int index = select.getSelectedIndex();
            if (index > 0){
                route.remove(select.getSelectedItem());
            } else {
                route = null;
            }
            orders.remove(index);
            refreshPath();
        }
    }

    public void recompute(){
        if (route != null){
            //TODO: implement
///            route.recompute(balance.getValue().doubleValue(), cargo.getValue().longValue());
            orders.clear();
            orders.addAll(route.getOrders());
            refreshPath();
        }
    }

    public void rebuild(){
        if (route != null){
            RouteModel r = market.getRoute(route);
            if (r != null){
                route = r;
                orders.clear();
                orders.addAll(route.getOrders());
                refreshPath();
            } else {
                recompute();
            }
        }
    }

    public void removeAll(){
        orders.clear();
        totalBalance.setValue(balance.getValue());
        totalProfit.setValue(0);
        route = null;
        refreshPath();
    }


    public void showTopOrders(){
        market.getTop(totalBalance.getValue().doubleValue(), result -> {
            Optional<OrderModel> order = Screeners.showOrders(result);
            if (order.isPresent()){
                orders.add(order.get());
                addOrderToPath(order.get());
            }
        });
    }

    public void showOrders(){
        SystemModel s = source.getCompletion();
        SystemModel t = target.getCompletion();
        StationModel sS = sStation.getValue();
        StationModel tS = tStation.getValue();
        market.getOrders(s, sS, t, tS, totalBalance.getValue().doubleValue(), result -> {
            Optional<OrderModel> order = Screeners.showOrders(result);
            if (order.isPresent()){
                orders.add(order.get());
                addOrderToPath(order.get());
            }
        });
    }

    public void showRoutes(){
        SystemModel s = source.getCompletion();
        SystemModel t = target.getCompletion();
        StationModel sS = sStation.getValue();
        StationModel tS = tStation.getValue();
        market.getRoutes(s, sS, t, tS, totalBalance.getValue().doubleValue(), new CrawlerSpecificator(), routes -> {
            Optional<RouteModel> path = Screeners.showRouters(routes);
            if (path.isPresent()){
                orders.addAll(path.get().getOrders());
                addRouteToPath(path.get());
                Screeners.showHelper();
            }
        });
    }

    public void showTopRoutes(){
        market.getTopRoutes(totalBalance.getValue().doubleValue(), routes -> {
            Optional<RouteModel> path = Screeners.showRouters(routes);
            if (path.isPresent()){
                orders.addAll(path.get().getOrders());
                addRouteToPath(path.get());
            }
        });
    }

    private void addRouteToPath(RouteModel route){
        if (this.route == null){
            this.route = route;
        } else {
            this.route.add(route);
        }
        refreshPath();
    }

    private void addOrderToPath(OrderModel order){
        if (route != null){
            route.add(order);
        } else {
            route = market.getPath(order);
        }
        refreshPath();
    }

    private void refreshPath(){
        MainController.getProfile().setRoute(route);
        if (route != null)
            path.setContent(new RouteNode(route).getNode());
        else
            path.setContent(null);
    }

    private class RouterChangeListener extends ChangeMarketListener {

        @Override
        public void add(StationModel station) {
            if (station.getSystem().equals(source.getCompletion())){
                sStation.getItems().add(station);
            }
            if (station.getSystem().equals(target.getCompletion())){
                tStation.getItems().add(station);
            }
        }

        @Override
        public void remove(StationModel station) {
            if (station.getSystem().equals(source.getCompletion())){
                sStation.getItems().remove(station);
            }
            if (station.getSystem().equals(target.getCompletion())){
                tStation.getItems().remove(station);
            }
        }
    }
}
