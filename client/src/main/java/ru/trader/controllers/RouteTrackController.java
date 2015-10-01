package ru.trader.controllers;

import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import ru.trader.analysis.CrawlerSpecificator;
import ru.trader.model.*;
import ru.trader.view.support.ViewUtils;
import ru.trader.view.support.autocomplete.AutoCompletion;
import ru.trader.view.support.autocomplete.SystemsProvider;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class RouteTrackController {

    @FXML
    private ListView<MissionModel> missionsList;
    @FXML
    private MissionsController missionsController;

    private RouteModel route;
    private int index;

    @FXML
    private void initialize(){
        MainController.getProfile().routeProperty().addListener(routeListener);
        missionsList.setItems(missionsController.getMissions());
    }

    public void setRoute(RouteModel route){
        if (this.route != null){
            this.route.currentEntryProperty().removeListener(currentEntryListener);
        }
        this.route = route;
        setIndex(route.getCurrentEntry());
        this.route.currentEntryProperty().addListener(currentEntryListener);
    }

    public void setIndex(int index){
        this.index = index;
        missionsController.setStation(route.get(index).getStation());
        ObservableList<StationModel> stations = FXCollections.observableArrayList(route.getStations(index));
        missionsController.getBuyerProvider().setPossibleSuggestions(stations);
        missionsController.getReceiverProvider().setPossibleSuggestions(stations);
        List<ItemModel> items = route.getSellOffers(index).stream().map(OfferModel::getItem).collect(Collectors.toList());
        missionsController.getItem().getItems().setAll(items);
    }

    @FXML
    private void addMissions(){
        int startIndex = route.isLoop() ? 0 : index;
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
    private final ChangeListener<RouteModel> routeListener = (ov, o, n) -> {
       if (n != null){
         ViewUtils.doFX(() -> setRoute(n));
       }
    };

}
