package ru.trader.controllers;

import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
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
        addMissionsList.setItems(missionsController.getMissions());
        buyOrders.setCellFactory(new OrderListCell(false));
        sellOrders.setCellFactory(new OrderListCell(true));
        editGroup.setVisible(false);
        init();
    }

    void init(){
        ProfileModel profile = MainController.getProfile();
        profile.routeProperty().addListener(routeListener);
        setRoute(profile.getRoute());
    }

    void unbind(){
        MainController.getProfile().routeProperty().removeListener(routeListener);
    }

    public void setRoute(RouteModel route){
        if (this.route != null){
            this.route.currentEntryProperty().removeListener(currentEntryListener);
        }
        this.route = route;
        fillTrack();
        if (route != null) {
            setIndex(route.getCurrentEntry());
            this.route.currentEntryProperty().addListener(currentEntryListener);
        } else {
            setIndex(-1);
        }
    }

    public void setIndex(int index){
        if (index != -1) {
            trackNode.setActive(index);
        } else {
            update();
        }
    }

    private void fillTrack(){
        if (trackNode != null) trackNode.activeProperty().removeListener(activeEntryListener);
        if (route != null) {
            trackNode = new Track(route);
            track.getChildren().setAll(trackNode.getNode());
            trackNode.activeProperty().addListener(activeEntryListener);
        } else {
            trackNode = null;
            track.getChildren().clear();
        }
    }

    private void update(){
        int index = trackNode != null ? trackNode.getActive() : -1;
        if (index != -1) {
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
        } else {
            missionsController.setStation(ModelFabric.NONE_STATION);
            missionsController.setStations(FXCollections.emptyObservableList());
            missionsController.setItems(FXCollections.emptyObservableList());

            station.setText("");
            system.setText("");
            time.setText("");
            refuel.setText("");
            buyOrders.setItems(FXCollections.emptyObservableList());
            sellOrders.setItems(FXCollections.emptyObservableList());
            missionsList.setItems(FXCollections.emptyObservableList());
        }
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
    private void addMissionsToTrack(){
        int startIndex = trackNode.getActive();
        route.addAll(startIndex, addMissionsList.getItems());
    }


    @FXML
    private void addMission(){
        missionsController.add();
    }

    @FXML
    private void removeMission(){
        int index = addMissionsList.getSelectionModel().getSelectedIndex();
        if (index >= 0){
            missionsController.remove(index);
        }
    }

    @FXML
    private void clearMissions(){
        missionsController.clear();

    }

    private final ChangeListener<? super Number> currentEntryListener = (ov, o, n) -> ViewUtils.doFX(() -> setIndex(n.intValue()));
    private final InvalidationListener activeEntryListener = ov -> ViewUtils.doFX(this::update);
    private final ChangeListener<RouteModel> routeListener = (ov, o, n) -> ViewUtils.doFX(() -> setRoute(n));

}
