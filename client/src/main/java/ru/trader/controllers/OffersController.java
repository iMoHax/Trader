package ru.trader.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.core.SERVICE_TYPE;
import ru.trader.model.*;
import ru.trader.model.support.BindingsHelper;
import ru.trader.model.support.ChangeMarketListener;
import ru.trader.view.support.FactionStringConverter;
import ru.trader.view.support.GovernmentStringConverter;
import ru.trader.view.support.ViewUtils;
import ru.trader.view.support.autocomplete.AutoCompletion;
import ru.trader.view.support.autocomplete.CachedSuggestionProvider;
import ru.trader.view.support.autocomplete.SystemsProvider;

import java.util.List;


public class OffersController {
    private final static Logger LOG = LoggerFactory.getLogger(OffersController.class);

    private StationModel station;

    @FXML
    private TextField systemText;
    private AutoCompletion<SystemModel> system;
    @FXML
    private ListView<StationModel> stationsList;
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
    private CheckBox cbRefuel;
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
    @FXML
    private TitledPane stationPane;

    private final List<OfferModel> sells = FXCollections.observableArrayList();
    private final List<OfferModel> buys = FXCollections.observableArrayList();

    // инициализируем форму данными
    @FXML
    private void initialize() {
        init();
        system.valueProperty().addListener((ob, oldValue, newValue) -> {
            LOG.info("Change system to {}", newValue);
            fillDetails(newValue);
        });
        stationsList.getSelectionModel().selectedItemProperty().addListener((v, o, n) -> {
            if (n != null) {
                fillTables(n);
            } else {
                fillTables(ModelFabric.NONE_STATION);
            }
        });

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
    }

    void init(){
        station = null;
        MarketModel market = MainController.getMarket();
        //TODO: create global notificator
        market.getNotificator().add(offersChangeListener);
        SystemsProvider provider = market.getSystemsProvider();
        if (system == null){
            system = new AutoCompletion<>(systemText, new CachedSuggestionProvider<>(provider), ModelFabric.NONE_SYSTEM, provider.getConverter());
        } else {
            system.setSuggestions(provider.getPossibleSuggestions());
            system.setConverter(provider.getConverter());
        }
    }

    private void fillDetails(SystemModel system){
        if (ModelFabric.isFake(system)) return;
        List<StationModel> stations = system.getStations();
        stationsList.setItems(FXCollections.observableList(stations));
        if (!stations.isEmpty()){
            stationsList.getSelectionModel().selectFirst();
        } else {
            fillTables(ModelFabric.NONE_STATION);
        }
    }

    private void fillTables(StationModel station){
        LOG.info("Change station to {}", station);
        this.station = station;
        sells.clear();
        buys.clear();
        stationPane.setText(station.getName());
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
            cbRefuel.setSelected(station.hasService(SERVICE_TYPE.REFUEL));
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
        return system.getValue();
    }

    public StationModel getStation() {
        return station;
    }

    @FXML
    private void addStation() {
        Screeners.showAddStation(getSystem());
    }

    @FXML
    private void editStation() {
        Screeners.showEditStation(getStation());
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

    private final ChangeMarketListener offersChangeListener = new ChangeMarketListener() {

        @Override
        public void priceChange(OfferModel offer, double oldPrice, double newPrice) {
            StationModel station = getStation();
            if (station != null && (station.hasBuy(offer.getItem()) || station.hasSell(offer.getItem()))){
                ViewUtils.doFX(OffersController.this::sort);
            }
        }

        @Override
        public void add(OfferModel offer) {
            if (offer.getStation().equals(getStation())){
                ViewUtils.doFX(()-> addOffer(offer));
            }
        }

        @Override
        public void add(StationModel station) {
            ViewUtils.doFX(() -> {
                if (station.getSystem().equals(system.getValue())) {
                    stationsList.getItems().add(station);
                }
                refresh();
                sort();
            });
        }

        @Override
        public void remove(OfferModel offer) {
            if (offer.getStation().equals(getStation())){
                ViewUtils.doFX(() -> removeOffer(offer));
            }
        }

        @Override
        public void remove(StationModel station) {
            ViewUtils.doFX(() -> {
                if (station.getSystem().equals(system.getValue())) {
                    stationsList.getItems().remove(station);
                }
                refresh();
                sort();
            });
        }
    };
}
