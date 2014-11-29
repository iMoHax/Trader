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
            if (n != ModelFabric.NONE_SYSTEM){
                ObservableList<StationModel> stations = FXCollections.observableArrayList(ModelFabric.NONE_STATION);
                stations.addAll(n.getStations());
                sStation.setItems(stations);
            } else {
                sStation.setItems(FXCollections.observableArrayList(ModelFabric.NONE_STATION));
            }
        });
        target.valueProperty().addListener((ov, o, n) -> {
            if (n != ModelFabric.NONE_SYSTEM){
                ObservableList<StationModel> stations = FXCollections.observableArrayList(ModelFabric.NONE_STATION);
                stations.addAll(n.getStations());
                tStation.setItems(stations);
            } else {
                tStation.setItems(FXCollections.observableArrayList(ModelFabric.NONE_STATION));
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
        source.setValue(order.getBuyer().getSystem());
        sStation.setValue(order.getBuyer());
        balance.setDisable(true);
    }

    private void onRemove(OrderModel order) {
        totalProfit.sub(order.getProfit());
        totalBalance.sub(order.getProfit());
        source.setValue(order.getSystem());
        sStation.setValue(order.getStation());
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
            orders.set(index, order);
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
            orders.remove(index);
            refreshPath();
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
        OrderModel order = Screeners.showOrders(market.getTop(totalBalance.getValue().doubleValue()));
        if (order!=null){
            orders.add(order);
            addOrderToPath(order);
        }
    }

    public void showOrders(){
        SystemModel s = source.getValue();
        SystemModel t = target.getValue();
        StationModel sS = sStation.getValue();
        StationModel tS = tStation.getValue();
        OrderModel order = Screeners.showOrders(market.getOrders(s, sS, t, tS, totalBalance.getValue().doubleValue()));
        if (order!=null){
            //TODO: fix set balanace
            orders.add(order);
            addOrderToPath(order);
        }
    }

    public void showRoutes(){
        SystemModel s = source.getValue();
        SystemModel t = target.getValue();
        StationModel sS = sStation.getValue();
        StationModel tS = tStation.getValue();
        PathRouteModel path = Screeners.showRouters(market.getRoutes(s, sS, t, tS, totalBalance.getValue().doubleValue()));
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

    private class RouterChangeListener extends ChangeMarketListener {
        @Override
        public void add(SystemModel system) {
            target.getItems().add(system);
        }

        @Override
        public void remove(SystemModel system) {
            target.getItems().remove(system);
        }

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
