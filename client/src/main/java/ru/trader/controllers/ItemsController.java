package ru.trader.controllers;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseButton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.model.*;
import ru.trader.model.support.BindingsHelper;
import ru.trader.model.support.ChangeMarketListener;
import ru.trader.model.support.ModelBindings;


public class ItemsController {
    private final static Logger LOG = LoggerFactory.getLogger(ItemsController.class);

    @FXML
    private TableView<ItemModel> tblItems;

    @FXML
    private TableColumn<ItemModel, Number> minProfit;
    @FXML
    private TableColumn<ItemModel, Number> avgProfit;
    @FXML
    private TableColumn<ItemModel, Number> maxProfit;

    private final ObservableList<ItemModel> items = FXCollections.observableArrayList();

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
            ItemModel iDesc = data.getValue();
            return ModelBindings.diff(iDesc.minBuyProperty(),iDesc.maxSellProperty());
        });
        avgProfit.setCellValueFactory((data) -> {
            ItemModel iDesc = data.getValue();
            return iDesc.avgBuyProperty().subtract(iDesc.avgSellProperty());
        });
        maxProfit.setCellValueFactory((data) -> {
            ItemModel iDesc = data.getValue();
            return ModelBindings.diff(iDesc.maxBuyProperty(), iDesc.minSellProperty());
        });
        BindingsHelper.setTableViewItems(tblItems, items);
        init();
    }

    void init(){
        MarketModel market = MainController.getMarket();
        market.getNotificator().add(new ItemsStatChangeListener());
        items.clear();
        items.addAll(market.itemsProperty());
        sort();
    }

    private void sort(){
        if (tblItems.getSortOrder().size()>0)
            tblItems.sort();
    }

    private void refresh(){
        LOG.info("Refresh all stats");
        tblItems.getItems().forEach(ItemModel::refresh);
        sort();
    }

    private void addItem(ItemModel item){
        items.add(item);
    }

    private class ItemsStatChangeListener extends ChangeMarketListener {

        @Override
        public void add(ItemModel item) {
            addItem(item);
        }

        @Override
        public void remove(SystemModel system) {
            if (!system.isEmpty()) refresh();
        }

        @Override
        public void add(StationModel station) {
            refresh();
        }

        @Override
        public void remove(StationModel station) { refresh();}

        @Override
        public void add(OfferModel offer) {
            sort();
        }

        @Override
        public void remove(OfferModel offer) {
            sort();
        }

        @Override
        public void priceChange(OfferModel offer, double price, double value) {
            sort();
        }
    }


}
