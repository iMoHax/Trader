package ru.trader.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseButton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import ru.trader.model.*;
import ru.trader.model.support.ChangeMarketListener;

import java.util.Collection;
import java.util.Iterator;


public class OffersController {
    private final static Logger LOG = LoggerFactory.getLogger(OffersController.class);

    private VendorModel vendor;

    @FXML
    private ListView<VendorModel> vendors;

    @FXML
    private TableView<OfferDescModel> tblSell;

    @FXML
    private TableView<OfferDescModel> tblBuy;

    // инициализируем форму данными
    @FXML
    private void initialize() {
        vendors.getSelectionModel().selectedItemProperty().addListener((ob, oldValue, newValue) ->{
            if (newValue != null){
                LOG.info("Change vendor to {}", newValue);
                this.vendor = newValue;
                fillTables(vendor);
            } else {
                vendors.getSelectionModel().select(oldValue);
            }
        });
        tblSell.getSelectionModel().selectedItemProperty().addListener((v, o, n) -> {
            if (n!=null) Screeners.changeItemDesc(n);
        });
        tblBuy.getSelectionModel().selectedItemProperty().addListener((v, o, n) -> {
            if (n!=null) Screeners.changeItemDesc(n);
        });
        tblSell.setOnMouseClicked((e) -> {
            if (e.getButton() == MouseButton.SECONDARY){
                Screeners.showItemDesc(tblSell);
            }
        });
        tblBuy.setOnMouseClicked((e) -> {
            if (e.getButton() == MouseButton.SECONDARY){
                Screeners.showItemDesc(tblBuy);
            }
        });

        init();
    }

    void init(){
        MarketModel market = MainController.getMarket();
        market.addListener(new OffersChangeListener());
        vendors.setItems(market.vendorsProperty());
        vendors.getSelectionModel().selectFirst();
    }

    private void fillTables(VendorModel vendor){
        if (vendor != null){
            tblSell.setItems(FXCollections.observableList(vendor.getSells(this::asOfferDescModel)));
            tblBuy.setItems(FXCollections.observableList(vendor.getBuys(this::asOfferDescModel)));
            sort();
        } else {
            tblSell.getItems().clear();
            tblBuy.getItems().clear();
        }
    }

    private void sort(){
        Platform.runLater(()->{
            if (tblBuy.getSortOrder().size()>0){
                tblBuy.sort();
            }
            if (tblSell.getSortOrder().size()>0){
                tblSell.sort();
            }
        });
    }


    @FXML
    public void editPrice(TableColumn.CellEditEvent<OfferDescModel, Double> event){
        OfferModel offer = event.getRowValue().getOffer();
        offer.setPrice(event.getNewValue());
    }

    public VendorModel getVendor() {
        return vendor;
    }

    public void addVendor(ActionEvent actionEvent) {
        Screeners.showAddVendor();
    }

    public void editVendor(ActionEvent actionEvent) {
        Screeners.showEditVendor(vendor);
    }


    private OfferDescModel asOfferDescModel(OfferModel offer){
        return MainController.getMarket().asOfferDescModel(offer);
    }

    private void addOffer(OfferModel offer){
        switch (offer.getType()){
            case SELL: tblSell.getItems().add(asOfferDescModel(offer));
                break;
            case BUY:  tblBuy.getItems().add(asOfferDescModel(offer));
                break;
        }
    }

    private void removeOffer(OfferModel offer){
        switch (offer.getType()){
            case SELL: remove(offer, tblSell.getItems());
                break;
            case BUY:  remove(offer, tblBuy.getItems());
                break;
        }
    }

    private void remove(final OfferModel offer, final Collection<OfferDescModel> list){
        Iterator<OfferDescModel> iterator = list.iterator();
        while (iterator.hasNext()){
            if (iterator.next().getOffer().equals(offer)){
                iterator.remove();
                break;
            }
        }
    }

    private void refresh(OfferModel offer){
        LOG.info("Refresh lists link with item of offer {}", offer);
        for (OfferDescModel descModel : tblSell.getItems()) {
            if (descModel.hasItem(offer)){
                descModel.refresh(offer.getType());
                sort();
                return;
            }
        }
        for (OfferDescModel descModel : tblBuy.getItems()) {
            if (descModel.hasItem(offer)){
                descModel.refresh(offer.getType());
                sort();
                return;
            }
        }
    }

    private void refresh(){
        LOG.info("Refresh lists");
        tblSell.getItems().forEach(OfferDescModel::refresh);
        tblBuy.getItems().forEach(OfferDescModel::refresh);
        sort();
    }

    private class OffersChangeListener extends ChangeMarketListener {

        @Override
        public void priceChange(OfferModel offer, double oldPrice, double newPrice) {
            if (vendor.hasBuy(offer.getItem()) || vendor.hasSell(offer.getItem())){
                sort();
            }
        }

        @Override
        public void add(OfferModel offer) {
            refresh(offer);
            if (offer.hasVendor(vendor)){
                addOffer(offer);
            }

        }

        @Override
        public void add(VendorModel vendor) {
            refresh();
        }

        @Override
        public void remove(OfferModel offer) {
            refresh(offer);
            if (offer.hasVendor(vendor)) {
                removeOffer(offer);
            }
        }
    }
}
