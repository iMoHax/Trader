package ru.trader.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.util.Pair;
import org.controlsfx.control.CheckComboBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.core.*;
import ru.trader.model.MarketModel;
import ru.trader.model.ModelFabric;
import ru.trader.model.StationModel;
import ru.trader.model.SystemModel;
import ru.trader.model.support.BindingsHelper;
import ru.trader.view.support.*;
import ru.trader.view.support.autocomplete.AutoCompletion;
import ru.trader.view.support.autocomplete.CachedSuggestionProvider;
import ru.trader.view.support.autocomplete.SystemsProvider;
import ru.trader.view.support.cells.CustomListCell;

import java.util.Map;
import java.util.Optional;

public class FilterController {
    private final static Logger LOG = LoggerFactory.getLogger(FilterController.class);

    @FXML
    private TextField centerText;
    private AutoCompletion<SystemModel> center;
    @FXML
    private NumberField radius;
    @FXML
    private NumberField distance;
    @FXML
    private TextField systemText;
    private AutoCompletion<SystemModel> system;
    @FXML
    private ComboBox<String> station;
    @FXML
    private CheckComboBox<STATION_TYPE> stationTypes;
    @FXML
    private CheckComboBox<SERVICE_TYPE> services;
    @FXML
    private CheckComboBox<FACTION> factions;
    @FXML
    private CheckComboBox<GOVERNMENT> governments;

    @FXML
    private ListView<StationModel> excludes;
    @FXML
    private TextField vFilterSystemText;
    private AutoCompletion<SystemModel> vFilterSystem;
    @FXML
    private ComboBox<String> vFilterStation;
    @FXML
    private ListView<Pair<String, VendorFilter>> vFilters;


    private MarketModel market;
    private MarketFilter filter;
    private Dialog<MarketFilter> dlg;

    @FXML
    private void initialize(){
        init();
        stationTypes.setConverter(new StationTypeStringConverter());
        stationTypes.getItems().setAll(STATION_TYPE.values());
        services.setConverter(new ServiceTypeStringConverter());
        services.getItems().setAll(SERVICE_TYPE.values());
        factions.setConverter(new FactionStringConverter());
        factions.getItems().setAll(FACTION.values());
        governments.setConverter(new GovernmentStringConverter());
        governments.getItems().setAll(GOVERNMENT.values());
        excludes.setCellFactory(new CustomListCell<>(StationModel::getFullName));
        system.valueProperty().addListener((ov, o, n) -> {
            station.setItems(n.getStationNamesList());
            station.getSelectionModel().selectFirst();
        });
        vFilterSystem.valueProperty().addListener((ov, o, n) -> {
            vFilterStation.setItems(n.getStationNamesList());
            vFilterStation.getSelectionModel().selectFirst();
        });
        vFilters.setCellFactory(new CustomListCell<>(Pair::getKey));
        vFilters.setItems(FXCollections.observableArrayList());
    }

    void init(){
        market = MainController.getMarket();
        SystemsProvider provider = market.getSystemsProvider();
        if (system == null){
            system = new AutoCompletion<>(systemText, new CachedSuggestionProvider<>(provider), ModelFabric.NONE_SYSTEM, provider.getConverter());
        } else {
            system.setSuggestions(provider.getPossibleSuggestions());
            system.setConverter(provider.getConverter());
        }
        if (center == null){
            center = new AutoCompletion<>(centerText, new CachedSuggestionProvider<>(provider), ModelFabric.NONE_SYSTEM, provider.getConverter());
        } else {
            center.setSuggestions(provider.getPossibleSuggestions());
            center.setConverter(provider.getConverter());
        }
        if (vFilterSystem == null){
            vFilterSystem = new AutoCompletion<>(vFilterSystemText, new CachedSuggestionProvider<>(provider), ModelFabric.NONE_SYSTEM, provider.getConverter());
        } else {
            vFilterSystem.setSuggestions(provider.getPossibleSuggestions());
            vFilterSystem.setConverter(provider.getConverter());
        }
    }

    private void createDialog(Parent owner, Parent content){
        dlg = new Dialog<>();
        if (owner != null) dlg.initOwner(owner.getScene().getWindow());
        dlg.setTitle(Localization.getString("filter.title"));
        dlg.getDialogPane().setContent(content);
        dlg.getDialogPane().getButtonTypes().addAll(Dialogs.SAVE, Dialogs.CANCEL);
        dlg.setResultConverter(dialogButton -> {
            if (dialogButton == Dialogs.SAVE) {
                save();
                return this.filter;
            }
            return null;
        });
        dlg.setResizable(false);
    }

    private void fill(MarketFilter filter){
        this.filter = filter;
        center.setValue(market.getModeler().get(filter.getCenter()));
        radius.setValue(filter.getRadius());
        distance.setValue(filter.getDistance());
        stationTypes.getCheckModel().clearChecks();
        for (STATION_TYPE stationType : filter.getTypes()) {
            stationTypes.getCheckModel().check(stationType);
        }
        services.getCheckModel().clearChecks();
        for (SERVICE_TYPE service : filter.getServices()) {
            services.getCheckModel().check(service);
        }
        factions.getCheckModel().clearChecks();
        for (FACTION faction : filter.getFactions()) {
            factions.getCheckModel().check(faction);
        }
        governments.getCheckModel().clearChecks();
        for (GOVERNMENT government : filter.getGovernments()) {
            governments.getCheckModel().check(government);
        }
        excludes.setItems(BindingsHelper.observableList(filter.getExcludes(), market.getModeler()::get));
        vFilters.getItems().clear();
        for (Map.Entry<String, VendorFilter> entry : filter.getVendorFilters().entrySet()) {
            vFilters.getItems().add(new Pair<>(entry.getKey(), entry.getValue()));
        }
    }

    private void clear(){
        this.filter = null;
        center.setValue(ModelFabric.NONE_SYSTEM);
        radius.clear();
        distance.clear();
        excludes.setItems(FXCollections.emptyObservableList());
        vFilters.getItems().clear();
    }

    private void save() {
        SystemModel s = center.getValue();
        LOG.trace("Old filter", filter);
        filter.setCenter(ModelFabric.isFake(s) ? null : ModelFabric.get(s));
        filter.setRadius(radius.getValue().doubleValue());
        filter.setDistance(distance.getValue().doubleValue());
        filter.clearTypes();
        stationTypes.getCheckModel().getCheckedItems().forEach(filter::add);
        filter.clearServices();
        services.getCheckModel().getCheckedItems().forEach(filter::add);
        filter.clearFactions();
        factions.getCheckModel().getCheckedItems().forEach(filter::add);
        filter.clearGovernments();
        governments.getCheckModel().getCheckedItems().forEach(filter::add);
        filter.clearExcludes();
        excludes.getItems().forEach(st -> filter.addExclude(ModelFabric.get(st)));
        filter.clearVendorFilters();
        vFilters.getItems().forEach(f -> filter.addFilter(f.getKey(), f.getValue()));
        LOG.trace("New filter", filter);
    }

    public Optional<MarketFilter> showDialog(Parent parent, Parent content){
        return showDialog(parent, content, new MarketFilter());
    }

    public boolean showEditDialog(Parent parent, Parent content, MarketFilter filter){
        return showDialog(parent, content, filter).isPresent();
    }

    private Optional<MarketFilter> showDialog(Parent parent, Parent content, MarketFilter filter){
        if (dlg == null){
            createDialog(parent, content);
        }
        fill(filter);
        Optional<MarketFilter> result = dlg.showAndWait();
        clear();
        return result;
    }

    @FXML
    private void add() {
        SystemModel s = system.getValue();
        if (s != null){
            StationModel st = s.get(station.getValue());
            if (!ModelFabric.isFake(st)){
                excludes.getItems().add(st);
            } else {
                excludes.getItems().addAll(s.getStations());
            }
        }
    }

    @FXML
    private void remove() {
        int index = excludes.getSelectionModel().getSelectedIndex();
        if (index >= 0){
            excludes.getItems().remove(index);
        }
    }

    @FXML
    private void clean() {
        excludes.getItems().clear();
    }

    @FXML
    private void addVendorFilter() {
        SystemModel s = vFilterSystem.getValue();
        if (s != null){
            StationModel st = s.get(vFilterStation.getValue());
            if (!ModelFabric.isFake(st)){
                Optional<VendorFilter> filter =  Screeners.showVendorFilter();
                if (filter.isPresent()) {
                    String key = MarketFilter.getVendorKey(ModelFabric.get(st));
                    vFilters.getItems().add(new Pair<>(key, filter.get()));
                }
            }
        }
    }

    @FXML
    private void editVendorFilter() {
        int index = vFilters.getSelectionModel().getSelectedIndex();
        if (index >= 0){
            VendorFilter filter = vFilters.getItems().get(index).getValue();
            Screeners.showFilter(filter);
        }
    }

    @FXML
    private void removeVendorFilter() {
        int index = vFilters.getSelectionModel().getSelectedIndex();
        if (index >= 0){
            vFilters.getItems().remove(index);
        }
    }

    @FXML
    private void cleanVendorFilters() {
        vFilters.getItems().clear();
    }

    @FXML
    private void editDefaultVendorFilter() {
        Screeners.showFilter(filter.getDefaultVendorFilter());
    }

}
