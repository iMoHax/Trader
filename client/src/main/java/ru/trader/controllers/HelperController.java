package ru.trader.controllers;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import ru.trader.KeyBinding;
import ru.trader.model.MissionModel;
import ru.trader.model.OrderModel;
import ru.trader.model.RouteEntryModel;
import ru.trader.model.RouteModel;
import ru.trader.view.support.ViewUtils;
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

    private Stage stage;
    private RouteModel route;
    private final BooleanProperty docked;
    private final IntegerProperty currentEntry;

    public HelperController() {
        currentEntry = new SimpleIntegerProperty(-1);
        docked = new SimpleBooleanProperty(true);
    }

    @FXML
    private void initialize(){
        currentEntry.addListener(routeEntryListener);
        buyOrders.setCellFactory(new OrderListCell(false));
        sellOrders.setCellFactory(new OrderListCell(true));
        bindKeys();
    }

    public void show(Parent content, RouteModel route) {
        this.route = route;
        currentEntry.setValue(0);
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

    private void setRouteEntry(int index){
        RouteEntryModel entry = route.get(index);
        station.setText(entry.getStation().getName());
        system.setText(entry.getStation().getSystem().getName());
        time.setText(ViewUtils.timeToString(entry.getTime()));
        refuel.setText(String.valueOf(entry.getRefill()));
        buyOrders.setItems(entry.orders());
        sellOrders.setItems(entry.sellOrders());
        missions.setItems(entry.missions());
    }

    @FXML
    private void next(){
        int index = currentEntry.get();
        if (index < route.getJumps() - 1){
            currentEntry.setValue(index+1);
        }
    }

    @FXML
    private void pause(){

    }

    @FXML
    private void previous(){
        int index = currentEntry.get();
        if (index > 0){
            currentEntry.setValue(index-1);
        }
    }

    private final ChangeListener<Number> routeEntryListener = (ov, o, n) -> ViewUtils.doFX(() -> setRouteEntry(n.intValue()));

    private void bindKeys(){
        KeyBinding.bind(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD4, KeyEvent.CTRL_MASK | KeyEvent.ALT_MASK), k -> ViewUtils.doFX(this::previous));
        KeyBinding.bind(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD6, KeyEvent.CTRL_MASK | KeyEvent.ALT_MASK), k -> ViewUtils.doFX(this::next));
    }
}
