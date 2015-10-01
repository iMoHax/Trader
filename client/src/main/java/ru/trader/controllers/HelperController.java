package ru.trader.controllers;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import ru.trader.KeyBinding;
import ru.trader.Main;
import ru.trader.model.*;
import ru.trader.view.support.ViewUtils;
import ru.trader.view.support.cells.OfferListCell;
import ru.trader.view.support.cells.OrderListCell;

import javax.swing.*;
import java.awt.event.KeyEvent;


public class HelperController {

    @FXML
    private Label station;
    @FXML
    private Label system;
    @FXML
    private Label time;
    @FXML
    private Label refuel;
    @FXML
    private ListView<OrderModel> buyOrders;
    @FXML
    private ListView<OrderModel> sellOrders;
    @FXML
    private ListView<MissionModel> missions;
    @FXML
    private ListView<StationModel> stations;
    @FXML
    private ListView<OfferModel> sellOffers;

    private Stage stage;
    private RouteModel route;
    private final BooleanProperty docked;

    public HelperController() {
        docked = new SimpleBooleanProperty(true);
    }

    @FXML
    private void initialize(){
        MainController.getProfile().routeProperty().addListener(routeListener);
        buyOrders.setCellFactory(new OrderListCell(false));
        sellOrders.setCellFactory(new OrderListCell(true));
        sellOffers.setCellFactory(new OfferListCell(true));
        bindKeys();
    }

    public void show(Parent content) {
        if (stage == null){
            stage = new Stage();
            stage.setScene(new Scene(content));
            stage.show();
        } else {
            stage.show();
        }
    }

    public void close(){
        if (stage != null){
            stage.close();
        }
    }

    private void setRoute(RouteModel route){
        if (this.route != null){
            this.route.currentEntryProperty().removeListener(currentEntryListener);
        }
        this.route = route;
        setRouteEntry(route.getCurrentEntry());
        this.route.currentEntryProperty().addListener(currentEntryListener);
    }

    private void setRouteEntry(int index){
        RouteEntryModel entry = route.get(index);
        station.setText(entry.getStation().getName());
        system.setText(entry.getStation().getSystem().getName());
        time.setText(ViewUtils.timeToString(entry.getTime()));
        refuel.setText(String.valueOf(entry.getRefill()));
        buyOrders.setItems(entry.orders());
        sellOrders.setItems(entry.sellOrders());
        missions.setItems(entry.missions());
        stations.setItems(FXCollections.observableArrayList(route.getStations(index)));
        sellOffers.setItems(FXCollections.observableArrayList(route.getSellOffers(index)));
        Main.copyToClipboard(system.getText());
    }

    @FXML
    private void next(){
        int index = route.getCurrentEntry();
        if (index < route.getJumps() - 1){
            route.setCurrentEntry(index+1);
        } else {
            if (route.isLoop()){
                route.setCurrentEntry(0);
            }
        }
    }

    @FXML
    private void pause(){

    }

    @FXML
    private void previous(){
        int index = route.getCurrentEntry();
        if (index > 0){
            route.setCurrentEntry(index-1);
        }
    }

    @FXML
    private void copy(){
        Main.copyToClipboard(system.getText());
    }

    private void bindKeys(){
        KeyBinding.bind(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD4, KeyEvent.CTRL_MASK | KeyEvent.ALT_MASK), k -> ViewUtils.doFX(this::previous));
        KeyBinding.bind(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD6, KeyEvent.CTRL_MASK | KeyEvent.ALT_MASK), k -> ViewUtils.doFX(this::next));
    }

    private final ChangeListener<? super Number> currentEntryListener = (ov, o, n) -> ViewUtils.doFX(() -> setRouteEntry(n.intValue()));
    private final ChangeListener<RouteModel> routeListener = (ov, o, n) -> {
        if (n != null){
            ViewUtils.doFX(() -> setRoute(n));
        }
    };


}
