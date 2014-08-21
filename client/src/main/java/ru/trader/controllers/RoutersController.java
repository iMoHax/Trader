package ru.trader.controllers;


import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableView;
import ru.trader.model.*;
import ru.trader.view.support.NumberField;
import ru.trader.view.support.RouteNode;


public class RoutersController {

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
    private ComboBox<VendorModel> source;
    @FXML
    private ComboBox<VendorModel> target;

    @FXML
    private TableView<OrderModel> tblOrders;

    @FXML
    private NumberField totalProfit;

    @FXML
    private NumberField totalBalance;

    private MarketModel market;

    private PathRouteModel route;

    @FXML
    private void initialize(){
        init();
        balance.numberProperty().addListener((ov, o, n) -> totalBalance.setValue(n));
        cargo.numberProperty().addListener((ov, o, n) -> market.setCargo(n.intValue()));
        tank.numberProperty().addListener((ov, o, n) -> market.setTank(n.doubleValue()));
        distance.numberProperty().addListener((ov, o, n) -> market.setDistance(n.doubleValue()));
        jumps.numberProperty().addListener((ov, o, n) -> market.setJumps(n.intValue()));

        balance.setOnAction((v)->cargo.requestFocus());
        cargo.setOnAction((v) -> tank.requestFocus());
        tank.setOnAction((v) -> distance.requestFocus());
        distance.setOnAction((v)->jumps.requestFocus());
        jumps.setOnAction((v)->balance.requestFocus());

        balance.setValue(1000);
        cargo.setValue(4);
        tank.setValue(20);
        distance.setValue(7);
        jumps.setValue(3);

        editBtn.disableProperty().bind(tblOrders.getSelectionModel().selectedIndexProperty().isEqualTo(-1));
        removeBtn.disableProperty().bind(Bindings.createBooleanBinding(()-> {
            int sel = tblOrders.getSelectionModel().getSelectedIndex();
            return sel == -1 || sel != tblOrders.getItems().size()-1;
        }, tblOrders.getSelectionModel().selectedIndexProperty()));


        tblOrders.setItems(FXCollections.observableArrayList());
        tblOrders.getItems().addListener((ListChangeListener<OrderModel>) c -> {
            while (c.next()) {
                if (c.wasRemoved()){
                    for (OrderModel o : c.getRemoved()) {
                        onRemove(o);
                    }
                }
                if (c.wasAdded()){
                    for (OrderModel o : c.getAddedSubList()) {
                       onAdd(o);
                    }
                }
            }
        });
    }


    void init(){
        market = MainController.getMarket();
        source.setItems(market.vendorsProperty());
        source.getSelectionModel().selectFirst();
        target.setItems(FXCollections.observableArrayList(market.vendorsProperty()));
        target.getItems().add(0, null);
        tblOrders.getItems().clear();
        totalBalance.setValue(balance.getValue());
        totalProfit.setValue(0);
    }


    private void onAdd(OrderModel order){
        totalProfit.add(order.getProfit());
        totalBalance.add(order.getProfit());
        source.getSelectionModel().select(order.getBuyOffer().getVendor());
        balance.setDisable(true);
        source.setDisable(true);
    }

    private void onRemove(OrderModel order) {
        totalProfit.sub(order.getProfit());
        totalBalance.sub(order.getProfit());
        source.getSelectionModel().select(order.getVendor());
        if (tblOrders.getItems().isEmpty()) {
            balance.setDisable(false);
            source.setDisable(false);
        }
    }

    public void editOrders(){
        OrderModel sel = tblOrders.getSelectionModel().getSelectedItem();
        int index = tblOrders.getSelectionModel().getSelectedIndex();

        OrderModel order = Screeners.showOrders(market.getOrders(sel.getVendor(), sel.getBuyer(), sel.getBalance()));
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
        OrderModel order = Screeners.showOrders(market.getTop(100, totalBalance.getValue().doubleValue()));
        if (order!=null){
            tblOrders.getItems().add(order);
            addOrderToPath(order);
        }
    }

    public void showOrders(){
        VendorModel s = source.getSelectionModel().getSelectedItem();
        VendorModel t = target.getSelectionModel().getSelectedItem();
        OrderModel order;
        if (t==null){
            order = Screeners.showOrders(market.getOrders(s, totalBalance.getValue().doubleValue()));
        } else {
            order = Screeners.showOrders(market.getOrders(s, t, totalBalance.getValue().doubleValue()));
        }
        if (order!=null){
            tblOrders.getItems().add(order);
            addOrderToPath(order);
        }
    }

    public void showRoutes(){
        VendorModel s = source.getSelectionModel().getSelectedItem();
        VendorModel t = target.getSelectionModel().getSelectedItem();
        PathRouteModel path;
        if (t==null){
            path = Screeners.showRouters(market.getRoutes(s, totalBalance.getValue().doubleValue()));
        } else {
            path = Screeners.showRouters(market.getRoutes(s, t, totalBalance.getValue().doubleValue()));
        }
        if (path!=null){
            tblOrders.getItems().addAll(path.getOrders());
            addRouteToPath(path);
        }
    }

    public void showTopRoutes(){
        PathRouteModel path = Screeners.showRouters(market.getTopRoutes(totalBalance.getValue().doubleValue()));
        if (path!=null){
            tblOrders.getItems().addAll(path.getOrders());
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
