package ru.trader.controllers;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.controlsfx.control.CheckComboBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.core.MarketFilter;
import ru.trader.core.OFFER_TYPE;
import ru.trader.core.SERVICE_TYPE;
import ru.trader.core.STATION_TYPE;
import ru.trader.model.*;
import ru.trader.model.support.BindingsHelper;
import ru.trader.model.support.ChangeMarketListener;
import ru.trader.view.support.NumberField;
import ru.trader.view.support.ServiceTypeStringConverter;
import ru.trader.view.support.StationTypeStringConverter;
import ru.trader.view.support.autocomplete.AutoCompletion;
import ru.trader.view.support.autocomplete.CachedSuggestionProvider;
import ru.trader.view.support.autocomplete.ItemsProvider;
import ru.trader.view.support.autocomplete.SystemsProvider;
import ru.trader.view.support.cells.ItemListCell;

import java.util.Collection;
import java.util.List;


public class SearchController {
    private final static Logger LOG = LoggerFactory.getLogger(SearchController.class);

    @FXML
    private TextField sourceText;
    private AutoCompletion<SystemModel> source;
    @FXML
    private RadioButton rbSeller;
    @FXML
    private RadioButton rbBuyer;
    @FXML
    private TextField itemText;
    private AutoCompletion<ItemModel> itemField;
    @FXML
    private ListView<ItemModel> itemsList;
    @FXML
    private NumberField distance;
    @FXML
    private CheckComboBox<STATION_TYPE> stationTypes;
    @FXML
    private CheckComboBox<SERVICE_TYPE> services;
    @FXML
    private TableView<ResultEntry> tblResults;

    private final List<ResultEntry> results = FXCollections.observableArrayList();
    private final ToggleGroup offerType = new ToggleGroup();

    private MarketModel market;

    @FXML
    private void initialize() {
        init();
        itemsList.setItems(FXCollections.observableArrayList());
        itemsList.setCellFactory(new ItemListCell());
        stationTypes.setConverter(new StationTypeStringConverter());
        stationTypes.getItems().setAll(STATION_TYPE.values());
        services.setConverter(new ServiceTypeStringConverter());
        services.getItems().setAll(SERVICE_TYPE.values());
        rbBuyer.setToggleGroup(offerType);
        rbBuyer.setUserData(OFFER_TYPE.BUY);
        rbSeller.setToggleGroup(offerType);
        rbSeller.setUserData(OFFER_TYPE.SELL);
        rbSeller.setSelected(true);
        BindingsHelper.setTableViewItems(tblResults, results);
    }

    void init(){
        if (market != null) market.getNotificator().remove(searchChangeListener);
        market = MainController.getMarket();
        market.getNotificator().add(searchChangeListener);
        SystemsProvider provider = market.getSystemsProvider();
        if (source == null) {
            source = new AutoCompletion<>(sourceText, new CachedSuggestionProvider<>(provider), ModelFabric.NONE_SYSTEM, provider.getConverter());
        } else {
            source.setSuggestions(provider.getPossibleSuggestions());
            source.setConverter(provider.getConverter());
        }
        ItemsProvider itemsProvider = market.getItemsProvider();
        if (itemField == null){
            itemField = new AutoCompletion<>(itemText, new CachedSuggestionProvider<>(itemsProvider), ModelFabric.NONE_ITEM, itemsProvider.getConverter());
        } else {
            itemField.setSuggestions(itemsProvider.getPossibleSuggestions());
            itemField.setConverter(itemsProvider.getConverter());
        }
    }

    @FXML
    private void searchStations(){
        MarketFilter filter = new MarketFilter();
        filter.setDistance(distance.getValue().doubleValue());
        stationTypes.getCheckModel().getCheckedItems().forEach(filter::add);
        services.getCheckModel().getCheckedItems().forEach(filter::add);
        ItemModel item = itemField.getValue();
        Collection<ItemModel> items = itemsList.getItems();
        if (ModelFabric.isFake(item) && items.isEmpty()){
            Collection<StationModel> stations = market.getStations(filter);
            fill(stations);
        } else {
            OFFER_TYPE oType = (OFFER_TYPE) offerType.getSelectedToggle().getUserData();
            Collection<OfferModel> offers;
            if (items.isEmpty()) {
                offers = market.getOffers(oType, item, filter);
            } else {
                offers = market.getOffers(oType, items, filter);
            }
            fill(offers);
        }
    }

    private void fill(Collection<?> entries){
        results.clear();
        for (Object entry : entries) {
            if (entry instanceof StationModel){
                results.add(new ResultEntry((StationModel) entry));
            } else {
                if (entry instanceof OfferModel) {
                    results.add(new ResultEntry((OfferModel) entry));
                } else {
                    throw new IllegalArgumentException("Argument must have StationModel or OfferModel class");
                }
            }
        }
    }

    @FXML
    private void currentAsSource(){
        source.setValue(MainController.getProfile().getSystem());
    }

    @FXML
    private void addItem(){
        ItemModel item = itemField.getValue();
        if (!ModelFabric.isFake(item)){
            if (itemsList.getItems().contains(item)) return;
            itemsList.getItems().add(item);
        }
    }

    @FXML
    private void removeItem(){
        int index = itemsList.getSelectionModel().getSelectedIndex();
        if (index != -1){
            itemsList.getItems().remove(index);
        }
    }

    @FXML
    private void clearItems(){
        itemsList.getItems().clear();
    }

    public class ResultEntry {
        private final StationModel station;
        private final OfferModel offer;
        private final ReadOnlyDoubleProperty distance;

        private ResultEntry(StationModel station) {
            this(station, null);
        }

        private ResultEntry(OfferModel offer) {
            this(offer.getStation(), offer);
        }

        private ResultEntry(StationModel station, OfferModel offer) {
            this.station = station;
            this.offer = offer;
            SystemModel system = source.getValue();
            this.distance = new SimpleDoubleProperty(ModelFabric.isFake(system)? Double.NaN : system.getDistance(station.getSystem()));
        }

        public SystemModel getSystem(){
            return station.getSystem();
        }

        private StationModel getStation(){
            return station;
        }

        private OfferModel getOffer() {
            return offer;
        }

        public ReadOnlyStringProperty stationProperty(){
            return new SimpleStringProperty(String.format("%s (%.0f Ls)", station.getName(), station.getDistance()));
        }

        public ReadOnlyStringProperty nameProperty(){
            return offer != null ? offer.nameProperty() : new SimpleStringProperty("");
        }

        public ReadOnlyDoubleProperty priceProperty(){
            return offer != null ? offer.priceProperty() : new SimpleDoubleProperty(Double.NaN);
        }

        public ReadOnlyLongProperty countProperty(){
            return offer != null ? offer.countProperty() : new SimpleLongProperty(0);
        }

        public ReadOnlyDoubleProperty distanceProperty(){
            return distance;
        }

    }

    private final ChangeMarketListener searchChangeListener = new ChangeMarketListener() {

    };

}
