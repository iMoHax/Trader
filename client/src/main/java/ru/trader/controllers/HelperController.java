package ru.trader.controllers;

import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import ru.trader.KeyBinding;
import ru.trader.Main;
import ru.trader.model.*;
import ru.trader.view.support.ViewUtils;
import ru.trader.view.support.cells.OfferListCell;
import ru.trader.view.support.cells.OrderListCell;
import ru.trader.view.support.cells.StationListCell;

import javax.swing.KeyStroke;
import java.awt.event.KeyEvent;


public class HelperController {

    @FXML
    private Node refuelGroup;
    @FXML
    private Node ordersGroup;
    @FXML
    private Node missionsGroup;
    @FXML
    private Node infoGroup;
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
    @FXML
    private ToggleButton infoBtn;


    private Stage stage;
    private RouteModel route;
    private RouteEntryModel entry;

    @FXML
    private void initialize(){
        ProfileModel profile = MainController.getProfile();
        profile.routeProperty().addListener(routeListener);
        profile.dockedProperty().addListener(dockedListener);
        buyOrders.setCellFactory(new OrderListCell(false));
        sellOrders.setCellFactory(new OrderListCell(true));
        sellOffers.setCellFactory(new OfferListCell(true));
        stations.setCellFactory(new StationListCell());
        infoBtn.selectedProperty().addListener((ov, o, n) -> {
            if (n) showInfo();
                else hideInfo();
        });
        refuelGroup.managedProperty().bind(refuelGroup.visibleProperty());
        missionsGroup.managedProperty().bind(missionsGroup.visibleProperty());
        ordersGroup.managedProperty().bind(ordersGroup.visibleProperty());
        infoGroup.managedProperty().bind(infoGroup.visibleProperty());
        hideInfo();
        hideStationInfo();
        bindKeys();
    }

    private void resize(){
        if (stage == null) return;
        Pane root = (Pane)stage.getScene().getRoot();
        root.autosize();
        Bounds bounds = root.getLayoutBounds();
        stage.setWidth(bounds.getWidth());
        stage.setHeight(bounds.getHeight());
    }

    private void setDocked(boolean docked){
        if (route == null) return;
        if (docked && MainController.getProfile().getStation().equals(entry.getStation())){
            showStationInfo();
        } else {
            hideStationInfo();
            hideInfo();
        }
    }

    private void showStationInfo(){
        refuelGroup.setVisible(entry.getRefill() > 0);
        ordersGroup.setVisible(!buyOrders.getItems().isEmpty() || !sellOrders.getItems().isEmpty());
        missionsGroup.setVisible(!missions.getItems().isEmpty());
        resize();
    }

    private void hideStationInfo(){
        refuelGroup.setVisible(false);
        ordersGroup.setVisible(false);
        missionsGroup.setVisible(false);
        resize();
    }

    private void showInfo(){
        infoGroup.setVisible(true);
        resize();
    }

    private void hideInfo(){
        infoGroup.setVisible(false);
        resize();
    }

    public void show(Parent content, boolean toggle) {
        if (stage == null){
            stage = new Stage();
            stage.setScene(new Scene(content));
            stage.initStyle(StageStyle.UNDECORATED);
            stage.setAlwaysOnTop(true);
            addDragListeners(content);
            stage.show();
        } else {
            if (toggle && stage.isShowing()){
                stage.hide();
            } else {
                stage.show();
            }
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
        showStationInfo();
    }

    private void setRouteEntry(int index){
        entry = route.get(index);
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
    private final ChangeListener<Boolean> dockedListener = (ov, o, n) -> ViewUtils.doFX(() -> setDocked(n));
    private final ChangeListener<RouteModel> routeListener = (ov, o, n) -> {
        if (n != null){
            ViewUtils.doFX(() -> setRoute(n));
        }
    };

    private void addDragListeners(final Node node){
        new DragListener(node);
    }

    private class DragListener {
        private final EventHandler<MouseEvent> pressedListener;
        private final EventHandler<MouseEvent> draggedListener;
        double x, y;

        private DragListener(Node node) {
            pressedListener = (MouseEvent mouseEvent) -> {
                x = mouseEvent.getSceneX();
                y = mouseEvent.getSceneY();
            };
            draggedListener = (MouseEvent mouseEvent) -> {
                node.getScene().getWindow().setX(mouseEvent.getScreenX()-x);
                node.getScene().getWindow().setY(mouseEvent.getScreenY()-y);
            };
            node.setOnMousePressed(pressedListener);
            node.setOnMouseDragged(draggedListener);
        }
    }
}
