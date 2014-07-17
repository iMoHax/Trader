package ru.trader.controllers;


import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableView;
import ru.trader.core.Vendor;
import ru.trader.model.MarketModel;
import ru.trader.model.OfferDescModel;
import ru.trader.model.OrderModel;
import ru.trader.view.support.NumberField;


import java.awt.*;
import java.util.Collection;
import java.util.stream.Collectors;


public class RoutersController {

    @FXML
    private NumberField balance;

    @FXML
    private NumberField cargo;

    @FXML
    private Button add;

    @FXML
    private ComboBox<Vendor> vendors;

    @FXML
    private TableView<OrderModel> tblOrders;

    @FXML
    private NumberField totalProfit;

    @FXML
    private void initialize(){
        init();
        add.disableProperty().bind(this.balance.wrongProperty().or(this.cargo.wrongProperty()));
        tblOrders.setItems(FXCollections.observableArrayList());
        tblOrders.getItems().addListener((ListChangeListener<OrderModel>) c -> {
            while (c.next()) {
                if (c.wasRemoved()){
                    for (OrderModel o : c.getRemoved()) {
                        totalProfit.setValue(totalProfit.getValue().doubleValue()-o.getProfit());
                    }
                }
                if (c.wasAdded()){
                    for (OrderModel o : c.getAddedSubList()) {
                        totalProfit.setValue(totalProfit.getValue().doubleValue()+o.getProfit());
                    }
                }
            }
        });
    }

    void init(){
        MarketModel market = MainController.getMarket();
        vendors.setItems(market.vendorsProperty());
        vendors.getSelectionModel().selectFirst();
        tblOrders.getItems().clear();
        totalProfit.setValue(0);
    }

    private Collection<OfferDescModel> getOffers(){
        MarketModel market = MainController.getMarket();
        Vendor vendor = vendors.getSelectionModel().getSelectedItem();
        return vendor.getAllSellOffers().stream().map(market::asOfferDescModel).collect(Collectors.toList());
    }

    public void addOrders(ActionEvent e){
        Collection<OrderModel> orders = Screeners.showOrders(getOffers(), balance.getValue().doubleValue(), cargo.getValue().longValue());
        if (orders!=null){
            tblOrders.getItems().addAll(orders);
        }
    }

    public void removeSelected(ActionEvent e){
        TableView.TableViewSelectionModel<OrderModel> select = tblOrders.getSelectionModel();
        if (!select.isEmpty()){
            int index = select.getSelectedIndex();
            tblOrders.getItems().remove(index);
        }
    }
}
