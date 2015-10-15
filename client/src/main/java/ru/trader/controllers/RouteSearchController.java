package ru.trader.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import ru.trader.analysis.CrawlerSpecificator;
import ru.trader.model.*;
import ru.trader.view.support.autocomplete.AutoCompletion;
import ru.trader.view.support.autocomplete.CachedSuggestionProvider;
import ru.trader.view.support.autocomplete.SystemsProvider;

import java.util.Optional;

public class RouteSearchController {

    @FXML
    private TextField fromSystemText;
    private AutoCompletion<SystemModel> fromSystem;
    @FXML
    private ComboBox<String> fromStation;
    @FXML
    private TextField toSystemText;
    private AutoCompletion<SystemModel> toSystem;
    @FXML
    private ComboBox<String> toStation;
    @FXML
    private CheckBox cbFast;
    @FXML
    private ListView<MissionModel> missionsList;
    @FXML
    private MissionsController missionsController;

    private MarketModel market;
    private ProfileModel profile;

    @FXML
    private void initialize(){
        init();
        profile = MainController.getProfile();
        missionsList.setItems(missionsController.getMissions());
        initListeners();
    }

    private void init(){
        market = MainController.getMarket();
        SystemsProvider provider = market.getSystemsProvider();
        if (fromSystem == null){
            fromSystem = new AutoCompletion<>(fromSystemText, new CachedSuggestionProvider<>(provider), ModelFabric.NONE_SYSTEM, provider.getConverter());
        } else {
            fromSystem.setSuggestions(provider.getPossibleSuggestions());
            fromSystem.setConverter(provider.getConverter());
        }
        if (toSystem == null){
            toSystem = new AutoCompletion<>(toSystemText, new CachedSuggestionProvider<>(provider), ModelFabric.NONE_SYSTEM, provider.getConverter());
        } else {
            toSystem.setSuggestions(provider.getPossibleSuggestions());
            toSystem.setConverter(provider.getConverter());
        }
        fromStation.setValue(ModelFabric.NONE_STATION.getName());
        toStation.setValue(ModelFabric.NONE_STATION.getName());
    }

    private void initListeners(){
        fromSystem.valueProperty().addListener((ov, o , n) -> {
            fromStation.setItems(n.getStationNamesList());
            fromStation.getSelectionModel().selectFirst();
        });
        fromStation.valueProperty().addListener((ov, o , n) -> missionsController.setStation(fromSystem.getValue().get(n)));
        toSystem.valueProperty().addListener((ov, o , n) -> {
            toStation.setItems(n.getStationNamesList());
            toStation.getSelectionModel().selectFirst();
        });
    }

    @FXML
    private void currentAsFrom(){
        fromSystem.setValue(profile.getSystem());
        fromStation.setValue(profile.getStation().getName());
    }

    @FXML
    private void loop(){
        toSystem.setValue(fromSystem.getValue());
        toStation.setValue(fromStation.getValue());
    }

    @FXML
    private void search(){
        SystemModel f = fromSystem.getValue();
        SystemModel t = toSystem.getValue();
        StationModel fS = f != null ? f.get(fromStation.getValue()) : ModelFabric.NONE_STATION;
        StationModel tS = t != null ? t.get(toStation.getValue()) : ModelFabric.NONE_STATION;

        CrawlerSpecificator specificator = new CrawlerSpecificator();
        specificator.setByTime(cbFast.isSelected());
        missionsList.getItems().forEach(m -> m.toSpecification(specificator));
        market.getRoutes(f, fS, t, tS, profile.getBalance(), specificator, routes -> {
            Optional<RouteModel> path = Screeners.showRouters(routes);
            if (path.isPresent()){
                RouteModel route = path.get();
                route.addAll(0, missionsList.getItems());
                profile.setRoute(route);
            }
        });
    }

    @FXML
    private void removeMission(){
        int index = missionsList.getSelectionModel().getSelectedIndex();
        if (index >= 0){
            missionsList.getItems().remove(index);
        }

    }
}
