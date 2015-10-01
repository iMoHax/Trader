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
    private StationsProvider receiverProvider;
    private StationsProvider buyerProvider;

    public MissionsController() {
        missions = FXCollections.observableArrayList();
    }

    @FXML
    private void initialize(){
        MarketModel world = MainController.getWorld();
        receiverProvider = new StationsProvider(world);
        receiver = new AutoCompletion<>(receiverText, receiverProvider, ModelFabric.NONE_STATION, receiverProvider.getConverter());
        buyerProvider = new StationsProvider(world);
        buyer = new AutoCompletion<>(buyerText, buyerProvider, ModelFabric.NONE_STATION, buyerProvider.getConverter());
        item.setItems(world.itemsProperty());
        addCourierBtn.disableProperty().bind(Bindings.createBooleanBinding(() -> receiver.getCompletion() == null, receiver.completionProperty())
                .or(courierProfit.wrongProperty())
        );
        addDeliveryBtn.disableProperty().bind(Bindings.createBooleanBinding(() -> buyer.getCompletion() == null, buyer.completionProperty())
                .or(deliveryCount.wrongProperty())
                .or(deliveryProfit.wrongProperty())
        );
        addSupplyBtn.disableProperty().bind(Bindings.createBooleanBinding(() -> item.getValue() == null, item.valueProperty())
                .or(supplyCount.wrongProperty())
                .or(supplyProfit.wrongProperty())
        );
    }

    StationsProvider getReceiverProvider() {
        return receiverProvider;
    }

    StationsProvider getBuyerProvider() {
        return buyerProvider;
    }

    ComboBox<ItemModel> getItem() {
        return item;
    }

    @FXML
    private void addCourier(){
        StationModel station = receiver.getCompletion();
        double profit = courierProfit.getValue().doubleValue();
        if (station != null && profit > 0){
            missions.add(new MissionModel(station, profit));
        }
    }

    @FXML
    private void addDelivery(){
        StationModel station = buyer.getCompletion();
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
        if (item != null && profit > 0){
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
