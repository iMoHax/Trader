package ru.trader.controllers;


import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableView;
import ru.trader.Main;
import ru.trader.model.*;
import ru.trader.model.support.ChangeMarketListener;
import ru.trader.view.support.NumberField;
import ru.trader.view.support.RouteNode;

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
    private ComboBox<SystemModel> source;

    @FXML
    private ComboBox<StationModel> sStation;

    @FXML
    private ComboBox<SystemModel> target;

    @FXML
    private ComboBox<StationModel> tStation;

    @FXML
    private TableView<OrderModel> tblOrders;

    @FXML
    private NumberField totalProfit;

    @FXML
    private NumberField totalBalance;

    private MarketModel market;
    private PathRouteModel route;
    private final ObservableList<OrderModel> orders = FXCollections.observableArrayList();

    @FXML
    private void initialize(){
        init();
        balance.numberProperty().addListener((ov, o, n) -> {
            totalBalance.setValue(n);
            Main.SETTINGS.setBalance(n.doubleValue());
        });
        cargo.numberProperty().addListener((ov, o, n) -> {
            market.getAnalyzer().setCargo(n.intValue());
            Main.SETTINGS.setCargo(n.intValue());
        });
        tank.numberProperty().addListener((ov, o, n) -> {
            market.getAnalyzer().setTank(n.doubleValue());
            Main.SETTINGS.setTank(n.doubleValue());
        });
        distance.numberProperty().addListener((ov, o, n) -> {
            market.getAnalyzer().setMaxDistance(n.doubleValue());
            Main.SETTINGS.setDistance(n.doubleValue());
        });
        jumps.numberProperty().addListener((ov, o, n) -> {
            market.getAnalyzer().setJumps(n.intValue());
            Main.SETTINGS.setJumps(n.intValue());
        });
        source.valueProperty().addListener((ov, o, n) -> {
            if (n != null) {
                sStation.setItems(n.getStationsList());
            } else {
                sStation.setItems(FXCollections.emptyObservableList());
            }
        });
        target.valueProperty().addListener((ov, o, n) -> {
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
        distance.setValue(Main.SETTINGS.getDistance());
        jumps.setValue(Main.SETTINGS.getJumps());

        addBtn.disableProperty().bind(Bindings.createBooleanBinding(()-> {
            StationModel st = tStation.getValue();
            return st == null || st == ModelFabric.NONE_STATION;
        }, tStation.valueProperty()));

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
        source.setItems(market.systemsProperty());
        source.getSelectionModel().selectFirst();
        target.setItems(market.systemsListProperty());
        target.getSelectionModel().selectFirst();
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
            source.setDisable(false);
        }
    }

    public void addStationToRoute(){
        StationModel sS = sStation.getValue();
        StationModel tS = tStation.getValue();
        PathRouteModel r = market.getPath(sS, tS);
        if (r == null) return;
        if (route != null){
            route = route.add(r);
        } else {
            route = r;
        }
        refreshPath();
        source.setValue(tS.getSystem());
        sStation.setValue(tS);
    }

    public void editOrders(){
        OrderModel sel = tblOrders.getSelectionModel().getSelectedItem();
        int index = tblOrders.getSelectionModel().getSelectedIndex();
        market.getOrders(sel.getStation(), sel.getBuyer(), sel.getBalance(), result -> {
            Optional<OrderModel> order = Screeners.showOrders(result);
            if (order.isPresent()){
                orders.set(index, order.get());
            }

        });
    }

    public void removeSelected(){
        TableView.TableViewSelectionModel<OrderModel> select = tblOrders.getSelectionModel();
        if (!select.isEmpty()){
            int index = select.getSelectedIndex();
            if (index > 0){
                route = route.remove(select.getSelectedItem());
            } else {
                route = null;
            }
            orders.remove(index);
            refreshPath();
        }
    }

    public void recompute(){
        if (route != null){
            route.recompute(balance.getValue().doubleValue(), cargo.getValue().longValue());
            orders.clear();
            orders.addAll(route.getOrders());
            refreshPath();
        }
    }

    public void rebuild(){
        if (route != null){
            PathRouteModel r = market.getRoute(route, balance.getValue().doubleValue());
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
        SystemModel s = source.getValue();
        SystemModel t = target.getValue();
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
        SystemModel s = source.getValue();
        SystemModel t = target.getValue();
        StationModel sS = sStation.getValue();
        StationModel tS = tStation.getValue();
        market.getRoutes(s, sS, t, tS, totalBalance.getValue().doubleValue(), routes -> {
            Optional<PathRouteModel> path = Screeners.showRouters(routes);
            if (path.isPresent()){
                orders.addAll(path.get().getOrders());
                addRouteToPath(path.get());
            }
        });
    }

    public void showTopRoutes(){
        market.getTopRoutes(totalBalance.getValue().doubleValue(), routes -> {
            Optional<PathRouteModel> path = Screeners.showRouters(routes);
            if (path.isPresent()){
                orders.addAll(path.get().getOrders());
                addRouteToPath(path.get());
            }
        });
    }

    private void addRouteToPath(PathRouteModel route){
        if (this.route == null){
            this.route = route;
        } else {
            this.route = this.route.add(route);
        }
        refreshPath();
    }

    private void addOrderToPath(OrderModel order){
        if (route != null){
            route = route.add(order);
        } else {
            route = market.getPath(order);
        }
        refreshPath();
    }

    private void refreshPath(){
        if (route != null)
            path.setContent(new RouteNode(route).getNode());
        else
            path.setContent(null);
    }

    private class RouterChangeListener extends ChangeMarketListener {

        @Override
        public void add(StationModel station) {
            if (station.getSystem().equals(source.getValue())){
                sStation.getItems().add(station);
            }
            if (station.getSystem().equals(target.getValue())){
                tStation.getItems().add(station);
            }
        }

        @Override
        public void remove(StationModel station) {
            if (station.getSystem().equals(source.getValue())){
                sStation.getItems().remove(station);
            }
            if (station.getSystem().equals(target.getValue())){
                tStation.getItems().remove(station);
            }
        }
    }
}
