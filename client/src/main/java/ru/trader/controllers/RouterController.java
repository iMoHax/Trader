package ru.trader.controllers;


import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableView;
import ru.trader.Main;
import ru.trader.model.*;
import ru.trader.model.support.BindingsHelper;
import ru.trader.view.support.NumberField;
import ru.trader.view.support.RouteNode;

import java.util.List;


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
    private Button editBtn;

    @FXML
    private Button removeBtn;

    @FXML
    private ComboBox<SystemModel> source;
    @FXML
    private ComboBox<SystemModel> target;

    @FXML
    private TableView<OrderModel> tblOrders;

    @FXML
    private NumberField totalProfit;

    @FXML
    private NumberField totalBalance;

    private MarketModel market;
    private PathRouteModel route;
    private final List<OrderModel> orders = FXCollections.observableArrayList();

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

        editBtn.disableProperty().bind(tblOrders.getSelectionModel().selectedIndexProperty().isEqualTo(-1));
        removeBtn.disableProperty().bind(Bindings.createBooleanBinding(()-> {
            int sel = tblOrders.getSelectionModel().getSelectedIndex();
            return sel == -1 || sel != tblOrders.getItems().size()-1;
        }, tblOrders.getSelectionModel().selectedIndexProperty()));

        BindingsHelper.setTableViewItems(tblOrders, orders);
        tblOrders.getItems().addListener((ListChangeListener<OrderModel>) c -> {
            while (c.next()) {
                if (c.wasRemoved()){
                    c.getRemoved().forEach(this::onRemove);
                }
                if (c.wasAdded()){
                    c.getAddedSubList().forEach(this::onAdd);
                }
            }
        });
    }


    void init(){
        market = MainController.getMarket();
        source.setItems(market.systemsProperty());
        source.getSelectionModel().selectFirst();
        target.setItems(FXCollections.observableArrayList(market.systemsProperty()));
        target.getItems().add(0, ModelFabric.NONE_SYSTEM);
        target.getSelectionModel().selectFirst();
        orders.clear();
        totalBalance.setValue(balance.getValue());
        totalProfit.setValue(0);
    }


    private void onAdd(OrderModel order){
        totalProfit.add(order.getProfit());
        totalBalance.add(order.getProfit());
        source.getSelectionModel().select(order.getBuyOffer().getSystem());
        balance.setDisable(true);
    }

    private void onRemove(OrderModel order) {
        totalProfit.sub(order.getProfit());
        totalBalance.sub(order.getProfit());
        source.getSelectionModel().select(order.getStation().getSystem());
        if (orders.isEmpty()) {
            balance.setDisable(false);
            source.setDisable(false);
        }
    }

    public void editOrders(){
        OrderModel sel = tblOrders.getSelectionModel().getSelectedItem();
        int index = tblOrders.getSelectionModel().getSelectedIndex();

        OrderModel order = Screeners.showOrders(market.getOrders(sel.getStation(), sel.getBuyer(), sel.getBalance()));
        if (order!=null){
            tblOrders.getItems().set(index, order);
        }

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
            tblOrders.getItems().remove(index);
            refreshPath();
        }
    }

    public void removeAll(){
        tblOrders.getItems().clear();
        totalBalance.setValue(balance.getValue());
        totalProfit.setValue(0);
        route = null;
        refreshPath();
    }


    public void showTopOrders(){
        OrderModel order = Screeners.showOrders(market.getTop(totalBalance.getValue().doubleValue()));
        if (order!=null){
            orders.add(order);
            addOrderToPath(order);
        }
    }

    public void showOrders(){
        //TODO: fix set balanace
        SystemModel s = source.getSelectionModel().getSelectedItem();
        SystemModel t = target.getSelectionModel().getSelectedItem();
        OrderModel order;
        if (t == ModelFabric.NONE_SYSTEM){
            order = Screeners.showOrders(market.getOrders(s, totalBalance.getValue().doubleValue()));
        } else {
            order = Screeners.showOrders(market.getOrders(s, t, totalBalance.getValue().doubleValue()));
        }
        if (order!=null){
            orders.add(order);
            addOrderToPath(order);
        }
    }

    public void showRoutes(){
        SystemModel s = source.getSelectionModel().getSelectedItem();
        SystemModel t = target.getSelectionModel().getSelectedItem();
        PathRouteModel path;
        if (t == ModelFabric.NONE_SYSTEM){
            path = Screeners.showRouters(market.getRoutes(s, totalBalance.getValue().doubleValue()));
        } else {
            path = Screeners.showRouters(market.getRoutes(s, t, totalBalance.getValue().doubleValue()));
        }
        if (path!=null){
            orders.addAll(path.getOrders());
            addRouteToPath(path);
        }
    }

    public void showTopRoutes(){
        PathRouteModel path = Screeners.showRouters(market.getTopRoutes(totalBalance.getValue().doubleValue()));
        if (path!=null){
            orders.addAll(path.getOrders());
            addRouteToPath(path);
        }
    }

    private void addRouteToPath(PathRouteModel route){
        if (this.route == null){
            this.route = route;
        } else {
            this.route.add(route.getPath());
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
        if (route != null)
            path.setContent(new RouteNode(route).getNode());
        else
            path.setContent(null);
    }

}
