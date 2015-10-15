package ru.trader.controllers;

import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import ru.trader.model.*;
import ru.trader.model.support.BindingsHelper;
import ru.trader.view.support.Track;
import ru.trader.view.support.ViewUtils;
import ru.trader.view.support.cells.OrderListCell;

import java.util.stream.Collectors;

public class RouteTrackController {

    @FXML
    private Node editGroup;
    @FXML
    private Node infoGroup;
    @FXML
    private Node refuelGroup;
    @FXML
    private Node ordersGroup;
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
    private ListView<MissionModel> addMissionsList;
    @FXML
    private ListView<MissionModel> missionsList;
    @FXML
    private MissionsController missionsController;
    @FXML
    private Pane track;

    private RouteModel route;
    private Track trackNode;

    @FXML
    private void initialize(){
        MainController.getProfile().routeProperty().addListener(routeListener);
        addMissionsList.setItems(missionsController.getMissions());
        buyOrders.setCellFactory(new OrderListCell(false));
        sellOrders.setCellFactory(new OrderListCell(true));
        editGroup.setVisible(false);
    }

    public void setRoute(RouteModel route){
        if (this.route != null){
            this.route.currentEntryProperty().removeListener(currentEntryListener);
        }
        this.route = route;
        fillTrack();
        setIndex(route.getCurrentEntry());
        this.route.currentEntryProperty().addListener(currentEntryListener);
    }

    public void setIndex(int index){
        trackNode.setActive(index);
    }

    private void fillTrack(){
        if (trackNode != null) trackNode.activeProperty().removeListener(activeEntryListener);
        trackNode = new Track(route);
        track.getChildren().setAll(trackNode.getNode());
        trackNode.activeProperty().addListener(activeEntryListener);
    }

    private void update(){
        int index = trackNode.getActive();
        if (index == -1) return;
        RouteEntryModel entry = route.get(index);
        missionsController.setStation(entry.getStation());
        ObservableList<String> stations = BindingsHelper.observableList(route.getStations(index), StationModel::getFullName);
        missionsController.setStations(stations);
        ObservableList<ItemModel> items = FXCollections.observableList(route.getSellOffers(index).stream().map(OfferModel::getItem).collect(Collectors.toList()));
        missionsController.setItems(items);

        station.setText(entry.getStation().getName());
        system.setText(entry.getStation().getSystem().getName());
        time.setText(ViewUtils.timeToString(entry.getTime()));
        refuel.setText(String.valueOf(entry.getRefill()));
        buyOrders.setItems(entry.orders());
        sellOrders.setItems(entry.sellOrders());
        missionsList.setItems(entry.missions());
    }

    @FXML
    private void toggleEdit(){
        if (editGroup.isVisible()){
            editGroup.setVisible(false);
            infoGroup.setVisible(true);
        } else {
            editGroup.setVisible(true);
            infoGroup.setVisible(false);
        }
    }

    @FXML
    private void addMissions(){
        int startIndex = route.isLoop() ? 0 : trackNode.getActive();
        route.addAll(startIndex, missionsList.getItems());
    }

    @FXML
    private void removeMission(){
        int index = missionsList.getSelectionModel().getSelectedIndex();
        if (index >= 0){
            missionsList.getItems().remove(index);
        }
    }

    private final ChangeListener<? super Number> currentEntryListener = (ov, o, n) -> ViewUtils.doFX(() -> setIndex(n.intValue()));
    private final InvalidationListener activeEntryListener = ov -> ViewUtils.doFX(this::update);
    private final ChangeListener<RouteModel> routeListener = (ov, o, n) -> {
       if (n != null){
         ViewUtils.doFX(() -> setRoute(n));
       }
    };

}
