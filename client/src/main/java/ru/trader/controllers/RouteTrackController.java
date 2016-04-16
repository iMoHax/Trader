package ru.trader.controllers;

import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import ru.trader.Main;
import ru.trader.analysis.CrawlerSpecificator;
import ru.trader.core.Profile;
import ru.trader.model.*;
import ru.trader.view.support.Track;
import ru.trader.view.support.ViewUtils;
import ru.trader.view.support.autocomplete.AutoCompletion;
import ru.trader.view.support.autocomplete.CachedSuggestionProvider;
import ru.trader.view.support.autocomplete.SystemsProvider;
import ru.trader.view.support.cells.OrderDecoratedListCell;
import ru.trader.view.support.cells.OrderListCell;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

public class RouteTrackController {

    @FXML
    private Node editGroup;
    @FXML
    private Node missionsGroup;
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
    @FXML
    private TextField newEntrySystemText;
    private AutoCompletion<SystemModel> newEntrySystem;
    @FXML
    private ComboBox<String> newEntryStation;
    @FXML
    private ToggleButton tbMissionsEdit;


    private RouteModel route;
    private Track trackNode;

    @FXML
    private void initialize(){
        addMissionsList.setItems(missionsController.getMissions());
        buyOrders.setCellFactory(new OrderListCell(false));
        sellOrders.setCellFactory(new OrderDecoratedListCell(true));
        editGroup.setVisible(false);
        init();
        newEntrySystem.valueProperty().addListener((ov, o , n) -> {
            newEntryStation.setItems(n.getStationNamesList());
            newEntryStation.getSelectionModel().selectFirst();
        });
    }

    void init(){
        ProfileModel profile = MainController.getProfile();
        profile.routeProperty().addListener(routeListener);
        setRoute(profile.getRoute());
        MarketModel market = MainController.getMarket();
        SystemsProvider provider = market.getSystemsProvider();
        if (newEntrySystem == null){
            newEntrySystem = new AutoCompletion<>(newEntrySystemText, new CachedSuggestionProvider<>(provider), ModelFabric.NONE_SYSTEM, provider.getConverter());
        } else {
            newEntrySystem.setSuggestions(provider.getPossibleSuggestions());
            newEntrySystem.setConverter(provider.getConverter());
        }
        newEntryStation.setValue(ModelFabric.NONE_STATION.getName());
    }

    void unbind(){
        MainController.getProfile().routeProperty().removeListener(routeListener);
    }

    private void updateRoute(RouteModel newRoute){
        if (MainController.getProfile().getRoute() == route){
            MainController.getProfile().setRoute(newRoute);
        } else {
            setRoute(newRoute);
        }
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

            station.setText(entry.getStation().getName());
            system.setText(entry.getStation().getSystem().getName());
            time.setText(ViewUtils.timeToString(entry.getTime()));
            refuel.setText(ViewUtils.fuelToString(entry.getRefill(), MainController.getProfile().getShipTank()));
            buyOrders.setItems(entry.orders());
            sellOrders.setItems(entry.sellOrders());
            missionsList.setItems(entry.missions());
        } else {
            missionsController.setStation(ModelFabric.NONE_STATION);

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
    private void toggleMissionsEdit(){
        if (editGroup.isVisible()){
            editGroup.setVisible(false);
            missionsGroup.setVisible(true);
        } else {
            editGroup.setVisible(true);
            missionsGroup.setVisible(false);
        }
    }

    @FXML
    private void addMissionsToTrack(){
        addMissionsToTrack(addMissionsList.getItems(), false);
    }

    @FXML
    private void addAllMissionsToTrack(){
        addMissionsToTrack(addMissionsList.getItems(), true);
    }

    private void addMissionsToTrack(Collection<MissionModel> missions, boolean all){
        if (missions.isEmpty()) return;
        final int startIndex = trackNode.getActive();
        final Collection<MissionModel> notAdded = route.addAll(startIndex, missions);
        if (all && !notAdded.isEmpty()){
            CrawlerSpecificator specificator = new CrawlerSpecificator();
            specificator.setFullScan(false);
            final Collection<MissionModel> oldMissions = route.getMissions(startIndex);
            oldMissions.forEach(m -> m.toSpecification(specificator));
            notAdded.forEach(m -> m.toSpecification(specificator));
            StationModel from = route.get(startIndex).getStation();
            StationModel to = route.getLast().getStation();
            route.getMarket().getRoutes(from, to, route.getBalance(startIndex), specificator, routes -> {
                Optional<RouteModel> path = Screeners.showRouters(routes);
                if (path.isPresent()) {
                    route.removeAll(oldMissions);
                    RouteModel newRoute = route.set(startIndex, path.get());
                    newRoute.addAll(startIndex, notAdded);
                    newRoute.addAll(startIndex, oldMissions);
                    updateRoute(newRoute);
                    clearMissions();
                    tbMissionsEdit.fire();
                }
            });
        } else {
            if (notAdded.isEmpty()){
                Screeners.showInfo("Результат операции", null, "Миссии добавлены");
            } else {
                Screeners.showInfo("Результат операции", "Миссии не добавлены", notAdded.toString());
            }
        }
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

    @FXML
    private void removeMissionFromTrack(){
        if (route != null){
            MissionModel mission = missionsList.getSelectionModel().getSelectedItem();
            if (mission != null){
                route.remove(mission);
            }
        }
    }

    @FXML
    private void removeAllMissionsFromTrack(){
        if (route != null){
            Collection<MissionModel> missions = new ArrayList<>(missionsList.getItems());
            if (!missions.isEmpty()){
                route.removeAll(missions);
            }
        }
    }

    @FXML
    private void newEntryAsCurrent(){
        ProfileModel profile = MainController.getProfile();
        newEntrySystem.setValue(profile.getSystem());
        newEntryStation.setValue(profile.getStation().getName());
    }

    @FXML
    private void addEntry(){
        SystemModel toSystem = newEntrySystem.getValue();
        StationModel toStation = toSystem != null ? toSystem.get(newEntryStation.getValue()) : ModelFabric.NONE_STATION;
        if (!ModelFabric.isFake(toSystem)){
            if (route != null){
                if (!ModelFabric.isFake(toStation)){
                    updateRoute(route.add(toStation));
                } else {
                    updateRoute(route.add(toSystem));
                }
            } else {
                RouteModel r;
                ProfileModel profile = MainController.getProfile();
                if (!ModelFabric.isFake(toStation)){
                    r = RouteModel.asRoute(toStation, profile);
                } else {
                    r = RouteModel.asRoute(toSystem, profile);
                }
                updateRoute(r);
            }
        }
    }

    @FXML
    private void removeLast(){
        if (route != null && route.getJumps() > 0){
            updateRoute(route.dropLast());
        }
    }

    @FXML
    private void addOrder(){
        if (route != null){
            final int startIndex = trackNode.getActive();
            RouteEntryModel entry = route.get(startIndex);
            if (entry.isTransit()) return;
            StationModel seller = entry.getStation();
            if (ModelFabric.isFake(seller)) return;
            Profile profile = Profile.clone(ModelFabric.get(MainController.getProfile()));
            profile.setBalance(route.getBalance(startIndex));
            profile.getShip().setCargo(route.getCargo(startIndex));
            if (startIndex != route.getJumps()){
                Collection<StationModel> buyers = route.getStations(startIndex);
                route.getMarket().getOrders(seller, buyers, profile, orders -> {
                    Optional<OrderModel> order = Screeners.showOrders(orders);
                    if (order.isPresent()){
                        route.add(startIndex, order.get());
                    }
                });
            } else {
                route.getMarket().getOrders(seller, profile, orders -> {
                    Optional<OrderModel> order = Screeners.showOrders(orders);
                    if (order.isPresent()){
                        updateRoute(route.add(order.get()));
                    }
                });
            }
        }
    }

    @FXML
    private void removeOrder(){
        if (route != null){
            final int index = trackNode.getActive();
            OrderModel order = buyOrders.getSelectionModel().getSelectedItem();
            if (order != null){
                route.remove(index, order);
            }
        }
    }

    @FXML
    private void clearOrders(){
        if (route != null){
            final int index = trackNode.getActive();
            route.clearOrders(index);
        }
    }

    @FXML
    private void setActive(){
        MainController.getProfile().setRoute(route);
    }

    @FXML
    private void clear(){
        updateRoute(null);
    }

    @FXML
    private void copyToClipboard(){
        if (route != null){
            Main.copyToClipboard(route.asString());
        }
    }

    private final ChangeListener<? super Number> currentEntryListener = (ov, o, n) -> ViewUtils.doFX(() -> setIndex(n.intValue()));
    private final InvalidationListener activeEntryListener = ov -> ViewUtils.doFX(this::update);
    private final ChangeListener<RouteModel> routeListener = (ov, o, n) -> ViewUtils.doFX(() -> setRoute(n));

}
