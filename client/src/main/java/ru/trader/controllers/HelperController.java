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
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import ru.trader.KeyBinding;
import ru.trader.Main;
import ru.trader.model.*;
import ru.trader.view.support.ViewUtils;
import ru.trader.view.support.cells.OfferListCell;
import ru.trader.view.support.cells.OrderListCell;
import ru.trader.view.support.cells.StationListCell;

import javax.swing.*;
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
            Scene scene = new Scene(content);
            stage.setScene(scene);
            stage.initStyle(StageStyle.TRANSPARENT);
            scene.setFill(Color.TRANSPARENT);
            stage.setAlwaysOnTop(true);
            addDragListeners(content);
            stage.setX(Main.SETTINGS.helper().getX());
            stage.setY(Main.SETTINGS.helper().getY());
            bind();
            Main.SETTINGS.helper().setVisible(true);
            stage.show();
            setRoute(MainController.getProfile().getRoute());
        } else {
            if (toggle && stage.isShowing()){
                Main.SETTINGS.helper().setVisible(false);
                stage.hide();
            } else {
                Main.SETTINGS.helper().setVisible(true);
                stage.show();
            }
        }
    }

    public void close(){
        if (stage != null){
            stage.close();
            unbind();
            stage = null;
        }
    }

    private void bind(){
        ProfileModel profile = MainController.getProfile();
        profile.routeProperty().addListener(routeListener);
        profile.dockedProperty().addListener(dockedListener);
        Main.SETTINGS.helper().xProperty().bind(stage.xProperty());
        Main.SETTINGS.helper().yProperty().bind(stage.yProperty());
        bindKeys();
    }

    private void unbind(){
        ProfileModel profile = MainController.getProfile();
        profile.routeProperty().removeListener(routeListener);
        profile.dockedProperty().removeListener(dockedListener);
        Main.SETTINGS.helper().xProperty().unbind();
        Main.SETTINGS.helper().yProperty().unbind();
    }

    private void setRoute(RouteModel route){
        if (this.route != null){
            this.route.currentEntryProperty().removeListener(currentEntryListener);
        }
        this.route = route;
        if (route != null) {
            setRouteEntry(route.getCurrentEntry());
            this.route.currentEntryProperty().addListener(currentEntryListener);
            showStationInfo();
        } else {
            setRouteEntry(-1);
            hideStationInfo();
        }
    }

    private void setRouteEntry(int index){
        if (index != -1) {
            entry = route.get(index);
            station.setText(entry.getStation().getName());
            system.setText(entry.getStation().getSystem().getName());
            if (index > 0){
                time.setText(ViewUtils.timeToString(route.get(index-1).getTime()));
            } else {
                time.setText(ViewUtils.timeToString(0));
            }
            refuel.setText(String.valueOf(entry.getRefill()));
            buyOrders.setItems(entry.orders());
            sellOrders.setItems(entry.sellOrders());
            missions.setItems(entry.getCompletedMissions());
            stations.setItems(FXCollections.observableArrayList(route.getStations(index)));
            sellOffers.setItems(FXCollections.observableArrayList(route.getSellOffers(index)));
            Main.copyToClipboard(system.getText());
        } else {
            entry = null;
            station.setText("");
            system.setText("No route");
            time.setText("");
            refuel.setText("");
            buyOrders.setItems(FXCollections.emptyObservableList());
            sellOrders.setItems(FXCollections.emptyObservableList());
            missions.setItems(FXCollections.emptyObservableList());
            stations.setItems(FXCollections.emptyObservableList());
            sellOffers.setItems(FXCollections.emptyObservableList());
        }
    }

    @FXML
    private void complete(){
        if (route == null) return;
        ProfileModel profile = MainController.getProfile();
        boolean isEnd = route.isEnd();
        RouteEntryModel entry = this.entry;
        if (profile.isDocked() && MainController.getProfile().getStation().equals(entry.getStation())) {
            if (!isEnd)
                profile.setDocked(false);
            else {
                if (!route.isLoop()) {
                    profile.clearRoute();
                } else {
                    route.complete();
                }
            }
        } else {
            profile.setSystem(entry.getStation().getSystem());
            if (!entry.isTransit()) {
                profile.setStation(entry.getStation());
                profile.setDocked(true);
            }
        }
    }

    @FXML
    private void copy(){
        Main.copyToClipboard(system.getText());
    }

    private void bindKeys(){
        KeyBinding.bind(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.CTRL_MASK), k -> ViewUtils.doFX(this::complete));
    }

    private final ChangeListener<? super Number> currentEntryListener = (ov, o, n) -> ViewUtils.doFX(() -> setRouteEntry(n.intValue()));
    private final ChangeListener<Boolean> dockedListener = (ov, o, n) -> ViewUtils.doFX(() -> setDocked(n));
    private final ChangeListener<RouteModel> routeListener = (ov, o, n) -> ViewUtils.doFX(() -> setRoute(n));

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
