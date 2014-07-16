package ru.trader.controllers;


import javafx.collections.FXCollections;
import javafx.fxml.FXML;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseButton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.model.*;
import ru.trader.model.support.ChangeMarketListener;
import ru.trader.model.support.ModelBindings;


public class ItemsController {
    private final static Logger LOG = LoggerFactory.getLogger(ItemsController.class);

    @FXML
    private TableView<ItemDescModel> tblItems;

    @FXML
    private TableColumn<ItemDescModel, Number> minProfit;
    @FXML
    private TableColumn<ItemDescModel, Number> avgProfit;
    @FXML
    private TableColumn<ItemDescModel, Number> maxProfit;

    @FXML
    private void initialize() {
        tblItems.getSelectionModel().selectedItemProperty().addListener((v, o, n) -> {
            if (n!=null) Screeners.changeItemDesc(n);
        });
        tblItems.setOnMouseClicked((e) -> {
            if (e.getButton() == MouseButton.SECONDARY){
                Screeners.showItemDesc(tblItems);
            }
        });
        minProfit.setCellValueFactory((data) -> {
            ItemDescModel iDesc = data.getValue();
            return ModelBindings.diff(iDesc.minBuyProperty(),iDesc.maxSellProperty());
        });
        avgProfit.setCellValueFactory((data) -> {
            ItemDescModel iDesc = data.getValue();
            return iDesc.avgBuyProperty().subtract(iDesc.avgSellProperty());
        });
        maxProfit.setCellValueFactory((data) -> {
            ItemDescModel iDesc = data.getValue();
            return ModelBindings.diff(iDesc.maxBuyProperty(), iDesc.minSellProperty());
        });
        init();
    }

    void init(){
        MarketModel market = MainController.getMarket();
        market.addListener(new ItemsStatChangeListener());
        tblItems.setItems(FXCollections.observableArrayList(market.itemsProperty()));
        if (tblItems.getSortOrder().size()>0)
            tblItems.sort();
    }

    private void refresh(OfferModel offer){
        LOG.info("Refresh item desc link with item of offer {}", offer);
        for (ItemDescModel descModel : tblItems.getItems()) {
            if (descModel.hasItem(offer)){
                descModel.refresh(offer.getType());
                return;
            }
        }
    }

    private void refresh(){
        LOG.info("Refresh all stats");
        tblItems.getItems().forEach(ItemDescModel::refresh);
    }

    private void addItem(ItemDescModel item){
        tblItems.getItems().add(item);
    }

    private class ItemsStatChangeListener extends ChangeMarketListener {

        @Override
        public void add(ItemDescModel item) {
            addItem(item);
        }

        @Override
        public void add(OfferModel offer) {
            refresh(offer);
        }

        @Override
        public void add(VendorModel vendor) {
            refresh();
        }

        @Override
        public void remove(OfferModel offer) {
            refresh(offer);
        }

        @Override
        public void priceChange(OfferModel offer, double price, double value) {
            refresh(offer);
        }
    }


}
