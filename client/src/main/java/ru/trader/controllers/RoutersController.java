package ru.trader.controllers;


import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import ru.trader.core.Vendor;
import ru.trader.model.MarketModel;
import ru.trader.model.OfferDescModel;
import ru.trader.view.support.NumberField;

import java.util.Collection;
import java.util.stream.Collectors;


public class RoutersController {

    @FXML
    private NumberField balance;

    @FXML
    private NumberField cargo;

    @FXML
    private Button buy;

    @FXML
    private Button sell;

    @FXML
    private ComboBox<Vendor> vendors;

    @FXML
    private void initialize(){
        init();
        buy.disableProperty().bind(this.balance.wrongProperty().or(this.cargo.wrongProperty()));
        buy.setOnAction((e) -> Screeners.showOrders(getOffers(), balance.getValue().doubleValue(), cargo.getValue().longValue()));
    }

    void init(){
        MarketModel market = MainController.getMarket();
        vendors.setItems(market.vendorsProperty());
        vendors.getSelectionModel().selectFirst();
    }

    private Collection<OfferDescModel> getOffers(){
        MarketModel market = MainController.getMarket();
        Vendor vendor = vendors.getSelectionModel().getSelectedItem();
        return vendor.getAllSellOffers().stream().map(market::asOfferDescModel).collect(Collectors.toList());
    }
}
