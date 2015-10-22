package ru.trader.controllers;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import ru.trader.model.*;
import ru.trader.view.support.NumberField;
import ru.trader.view.support.autocomplete.AutoCompletion;
import ru.trader.view.support.autocomplete.CachedSuggestionProvider;
import ru.trader.view.support.autocomplete.StationsProvider;


public class MissionsController {

    @FXML
    private TextField receiverText;
    private AutoCompletion<StationModel> receiver;
    @FXML
    private NumberField courierProfit;
    @FXML
    private Button addCourierBtn;

    @FXML
    private TextField buyerText;
    private AutoCompletion<StationModel> buyer;
    @FXML
    private NumberField deliveryCount;
    @FXML
    private NumberField deliveryProfit;
    @FXML
    private Button addDeliveryBtn;

    @FXML
    private ComboBox<ItemModel> item;
    @FXML
    private NumberField supplyCount;
    @FXML
    private NumberField supplyProfit;
    @FXML
    private Button addSupplyBtn;

    private final ObservableList<MissionModel> missions;
    private StationModel station;

    public MissionsController() {
        missions = FXCollections.observableArrayList();
    }

    @FXML
    private void initialize(){
        init();
        addCourierBtn.disableProperty().bind(Bindings.createBooleanBinding(() -> receiver.getValue() == null, receiver.valueProperty())
                .or(courierProfit.wrongProperty())
        );
        addDeliveryBtn.disableProperty().bind(Bindings.createBooleanBinding(() -> buyer.getValue() == null, buyer.valueProperty())
                .or(deliveryCount.wrongProperty())
                .or(deliveryProfit.wrongProperty())
        );
        addSupplyBtn.disableProperty().bind(Bindings.createBooleanBinding(() -> item.getValue() == null, item.valueProperty())
                .or(supplyCount.wrongProperty())
                .or(supplyProfit.wrongProperty())
        );
    }

    void init(){
        MarketModel world = MainController.getWorld();
        item.setItems(world.itemsProperty());
        StationsProvider provider = new StationsProvider(world);
        if (receiver == null){
            receiver = new AutoCompletion<>(receiverText, new CachedSuggestionProvider<>(provider), ModelFabric.NONE_STATION, provider.getConverter());
        } else {
            receiver.setSuggestions(provider.getPossibleSuggestions());
            receiver.setConverter(provider.getConverter());
        }
        if (buyer == null){
            buyer = new AutoCompletion<>(buyerText, new CachedSuggestionProvider<>(provider), ModelFabric.NONE_STATION, provider.getConverter());
        } else {
            buyer.setSuggestions(provider.getPossibleSuggestions());
            buyer.setConverter(provider.getConverter());
        }
    }

    void setStations(ObservableList<String> stationNames) {
        receiver.setSuggestions(stationNames);
        buyer.setSuggestions(stationNames);
    }

    void setItems(ObservableList<ItemModel> items){
        item.setItems(items);
    }

    @FXML
    private void addCourier(){
        StationModel station = receiver.getValue();
        double profit = courierProfit.getValue().doubleValue();
        if (station != null && profit > 0){
            missions.add(new MissionModel(station, profit));
        }
    }

    @FXML
    private void addDelivery(){
        StationModel station = buyer.getValue();
        long count = deliveryCount.getValue().longValue();
        double profit = deliveryProfit.getValue().doubleValue();
        if (station != null && profit > 0){
            missions.add(new MissionModel(station, count, profit));
        }
    }

    @FXML
    private void addSupply(){
        ItemModel item = this.item.getValue();
        long count = supplyCount.getValue().longValue();
        double profit = supplyProfit.getValue().doubleValue();
        if (station != null && item != null && profit > 0){
            missions.add(new MissionModel(station, item, count, profit));
        }
    }

    public ObservableList<MissionModel> getMissions() {
        return missions;
    }

    public void setStation(StationModel station) {
        this.station = station;
    }
}
