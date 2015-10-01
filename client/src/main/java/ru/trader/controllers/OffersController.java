package ru.trader.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import org.controlsfx.control.SegmentedButton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.core.SERVICE_TYPE;
import ru.trader.model.*;
import ru.trader.model.support.BindingsHelper;
import ru.trader.model.support.ChangeMarketListener;
import ru.trader.view.support.FactionStringConverter;
import ru.trader.view.support.GovernmentStringConverter;
import ru.trader.view.support.ViewUtils;

import java.util.List;


public class OffersController {
    private final static Logger LOG = LoggerFactory.getLogger(OffersController.class);

    private StationModel station;
    private SystemModel system;

    @FXML
    private Insets stationsMargin;
    @FXML
    private ListView<SystemModel> systems;
    @FXML
    private SegmentedButton stationsBar;
    @FXML
    private TableView<OfferModel> tblSell;
    @FXML
    private TableView<OfferModel> tblBuy;
    @FXML
    private Label faction;
    @FXML
    private Label government;
    @FXML
    private Label distance;
    @FXML
    private CheckBox cbMarket;
    @FXML
    private CheckBox cbBlackMarket;
    @FXML
    private CheckBox cbRepair;
    @FXML
    private CheckBox cbMunition;
    @FXML
    private CheckBox cbOutfit;
    @FXML
    private CheckBox cbShipyard;
    @FXML
    private CheckBox cbMediumLandpad;
    @FXML
    private CheckBox cbLargeLandpad;

    private final List<OfferModel> sells = FXCollections.observableArrayList();
    private final List<OfferModel> buys = FXCollections.observableArrayList();

    private final static ToggleGroup stationsGrp = new ToggleGroup();

    // инициализируем форму данными
    @FXML
    private void initialize() {
        systems.getSelectionModel().selectedItemProperty().addListener((ob, oldValue, newValue) ->{
            if (newValue != null){
                LOG.info("Change system to {}", newValue);
                fillDetails(newValue);
            } else {
                systems.getSelectionModel().select(oldValue);
            }
        });
        stationsGrp.selectedToggleProperty().addListener((v, o, n) -> {
            if (n != null){
                fillTables((StationModel) n.getUserData());
            } else {
                fillTables(ModelFabric.NONE_STATION);
            }
        });
        stationsBar.setToggleGroup(stationsGrp);

        tblSell.getSelectionModel().selectedItemProperty().addListener((v, o, n) -> {
            if (n != null) Screeners.changeItemDesc(n.getItem());
        });
        tblBuy.getSelectionModel().selectedItemProperty().addListener((v, o, n) -> {
            if (n != null) Screeners.changeItemDesc(n.getItem());
        });
        tblSell.setOnMouseClicked((e) -> {
            if (e.getButton() == MouseButton.SECONDARY) {
                Screeners.showItemDesc(tblSell);
            }
        });
        tblBuy.setOnMouseClicked((e) -> {
            if (e.getButton() == MouseButton.SECONDARY) {
                Screeners.showItemDesc(tblBuy);
            }
        });
        BindingsHelper.setTableViewItems(tblSell, sells);
        BindingsHelper.setTableViewItems(tblBuy, buys);

        init();
    }

    void init(){
        station = null;
        system = null;
        MarketModel market = MainController.getMarket();
        market.getNotificator().add(new OffersChangeListener());
        systems.setItems(market.systemsProperty());
        systems.getSelectionModel().selectFirst();
    }

    private void fillDetails(SystemModel system){
        this.system = system;
        List<StationModel> stations = system.getStations();
        stationsBar.getButtons().clear();
        stations.forEach(s -> stationsBar.getButtons().add(buildStationNode(s)));
        if (!stations.isEmpty()){
            stationsBar.getButtons().get(0).setSelected(true);
        } else {
            fillTables(ModelFabric.NONE_STATION);
        }
    }

    private ToggleButton buildStationNode(StationModel station){
        ToggleButton stationBtn = new ToggleButton(station.getName());
        stationBtn.setUserData(station);
        return stationBtn;
    }

    private void fillTables(StationModel station){
        LOG.info("Change station to {}", station);
        this.station = station;
        sells.clear();
        buys.clear();
        distance.setText("");
        government.setText("");
        faction.setText("");
        cbMarket.setSelected(false);
        cbBlackMarket.setSelected(false);
        cbMunition.setSelected(false);
        cbRepair.setSelected(false);
        cbOutfit.setSelected(false);
        cbShipyard.setSelected(false);
        cbMediumLandpad.setSelected(false);
        cbLargeLandpad.setSelected(false);
        if (station != ModelFabric.NONE_STATION){
            faction.setText(FactionStringConverter.toLocalizationString(station.getFaction()));
            government.setText(GovernmentStringConverter.toLocalizationString(station.getGovernment()));
            distance.setText(String.valueOf(station.getDistance()));
            cbMarket.setSelected(station.hasService(SERVICE_TYPE.MARKET));
            cbBlackMarket.setSelected(station.hasService(SERVICE_TYPE.BLACK_MARKET));
            cbMunition.setSelected(station.hasService(SERVICE_TYPE.MUNITION));
            cbRepair.setSelected(station.hasService(SERVICE_TYPE.REPAIR));
            cbOutfit.setSelected(station.hasService(SERVICE_TYPE.OUTFIT));
            cbShipyard.setSelected(station.hasService(SERVICE_TYPE.SHIPYARD));
            cbMediumLandpad.setSelected(station.hasService(SERVICE_TYPE.MEDIUM_LANDPAD));
            cbLargeLandpad.setSelected(station.hasService(SERVICE_TYPE.LARGE_LANDPAD));
            sells.addAll(station.getSells());
            buys.addAll(station.getBuys());
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
    public void editPrice(TableColumn.CellEditEvent<OfferModel, Double> event){
        OfferModel offer = event.getRowValue();
        offer.setPrice(event.getNewValue());
    }

    public SystemModel getSystem() {
        return system;
    }

    public StationModel getStation() {
        return station;
    }

    public void addStation(ActionEvent actionEvent) {
        Screeners.showAddStation(system);
    }

    public void editStation(ActionEvent actionEvent) {
        Screeners.showEditStation(station);
    }


    private void addOffer(OfferModel offer){
        switch (offer.getType()){
            case SELL: sells.add(offer);
                break;
            case BUY:  buys.add(offer);
                break;
        }
    }

    private void removeOffer(OfferModel offer){
        switch (offer.getType()){
            case SELL: sells.remove(offer);
                break;
            case BUY:  buys.remove(offer);
                break;
        }
    }

    private void refresh(){
        LOG.info("Refresh lists");
        tblSell.getItems().forEach(OfferModel::refresh);
        tblBuy.getItems().forEach(OfferModel::refresh);
    }

    private class OffersChangeListener extends ChangeMarketListener {

        @Override
        public void priceChange(OfferModel offer, double oldPrice, double newPrice) {
            if (station.hasBuy(offer.getItem()) || station.hasSell(offer.getItem())){
                ViewUtils.doFX(OffersController.this::sort);
            }
        }

        @Override
        public void add(OfferModel offer) {
            if (offer.getStation().equals(station)){
                ViewUtils.doFX(()-> addOffer(offer));
            }
        }

        @Override
        public void add(StationModel station) {
            ViewUtils.doFX(() -> {
                stationsBar.getButtons().add(buildStationNode(station));
                refresh();
                sort();
            });
        }

        @Override
        public void remove(OfferModel offer) {
            if (offer.getStation().equals(station)){
                ViewUtils.doFX(() -> removeOffer(offer));
            }
        }

        @Override
        public void remove(StationModel station) {
            ViewUtils.doFX(() -> {
                stationsBar.getToggleGroup().getSelectedToggle().setSelected(false);
                stationsBar.getButtons().removeIf(b -> b.getUserData().equals(station));
                refresh();
                sort();
            });
        }

    }
}
