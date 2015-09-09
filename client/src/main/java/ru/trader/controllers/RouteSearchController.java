package ru.trader.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import ru.trader.analysis.CrawlerSpecificator;
import ru.trader.model.*;
import ru.trader.view.support.autocomplete.AutoCompletion;
import ru.trader.view.support.autocomplete.SystemsProvider;

import java.util.Optional;

public class RouteSearchController {

    @FXML
    private TextField fromSystemText;
    private AutoCompletion<SystemModel> fromSystem;
    @FXML
    private ComboBox<StationModel> fromStation;
    @FXML
    private TextField toSystemText;
    private AutoCompletion<SystemModel> toSystem;
    @FXML
    private ComboBox<StationModel> toStation;
    @FXML
    private ListView<MissionModel> missionsList;
    @FXML
    private MissionsController missionsController;

    private MarketModel market;
    private ProfileModel profile;

    @FXML
    private void initialize(){
        init();
        missionsList.setItems(missionsController.getMissions());
        initListeners();
    }

    private void init(){
        market = MainController.getMarket();
        profile = MainController.getProfile();
        SystemsProvider provider = new SystemsProvider(market);
        fromSystem = new AutoCompletion<>(fromSystemText, provider, provider.getConverter());
        provider = new SystemsProvider(market);
        toSystem = new AutoCompletion<>(toSystemText, provider, provider.getConverter());
    }

    private void initListeners(){
        fromSystem.completionProperty().addListener((ov, o , n) -> fromStation.setItems(n.getStationsList()));
        fromStation.valueProperty().addListener((ov, o , n) -> missionsController.setStation(n));
        toSystem.completionProperty().addListener((ov, o , n) -> toStation.setItems(n.getStationsList()));
    }

    @FXML
    private void currentAsFrom(){
        fromSystem.setValue(profile.getSystem());
        fromStation.setValue(profile.getStation());
    }

    @FXML
    private void loop(){
        toSystem.setValue(fromSystem.getCompletion());
        toStation.setValue(fromStation.getValue());
    }

    @FXML
    private void search(){
        SystemModel f = fromSystem.getCompletion();
        SystemModel t = toSystem.getCompletion();
        StationModel fS = fromStation.getValue();
        StationModel tS = toStation.getValue();

        CrawlerSpecificator specificator = new CrawlerSpecificator();
        missionsList.getItems().forEach(m -> m.toSpecification(specificator));
        market.getRoutes(f, fS, t, tS, profile.getBalance(), specificator, routes -> {
            Optional<RouteModel> path = Screeners.showRouters(routes);
            if (path.isPresent()){
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
