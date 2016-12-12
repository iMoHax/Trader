package ru.trader.controllers;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.text.TextAlignment;
import org.controlsfx.glyphfont.Glyph;
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
    private TableColumn<ItemModel, Number> itemNameColumn;
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
        addRefreshButton(itemNameColumn);

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

    private void addRefreshButton(TableColumn<?, ?> itemNameColumn) {
        Button refreshButton = new Button();
        refreshButton.setGraphic(Glyph.create("FontAwesome|REFRESH"));
        refreshButton.setOnAction(e -> refresh());
        itemNameColumn.setGraphic(refreshButton);
    }

    void init(){
        MarketModel market = MainController.getMarket();
        items.clear();
        items.addAll(market.itemsProperty());
        sort();
    }

    private void sort(){
        if (tblItems.getSortOrder().size()>0)
            tblItems.sort();
    }

    @FXML
    private void refresh(){
        LOG.info("Refresh all stats");
        tblItems.getItems().forEach(ItemModel::refresh);
        sort();
    }


}
