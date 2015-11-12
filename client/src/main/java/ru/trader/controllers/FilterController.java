package ru.trader.controllers;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.core.MarketFilter;
import ru.trader.core.SERVICE_TYPE;
import ru.trader.model.MarketModel;
import ru.trader.model.ModelFabric;
import ru.trader.model.StationModel;
import ru.trader.model.SystemModel;
import ru.trader.model.support.BindingsHelper;
import ru.trader.view.support.Localization;
import ru.trader.view.support.NumberField;
import ru.trader.view.support.autocomplete.AutoCompletion;
import ru.trader.view.support.autocomplete.CachedSuggestionProvider;
import ru.trader.view.support.autocomplete.SystemsProvider;
import ru.trader.view.support.cells.CustomListCell;

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
    private ListView<StationModel> excludes;

    private MarketModel market;
    private MarketFilter filter;
    private Dialog<MarketFilter> dlg;

    @FXML
    private void initialize(){
        init();
        excludes.setCellFactory(new CustomListCell<>(s -> String.format("%s (%s)", s.getSystem().getName(), s.getName())));
        system.valueProperty().addListener((ov, o, n) -> {
            station.setItems(n.getStationNamesList());
            station.getSelectionModel().selectFirst();
        });
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
    }

    private void createDialog(Parent owner, Parent content){
        dlg = new Dialog<>();
        if (owner != null) dlg.initOwner(owner.getScene().getWindow());
        dlg.setTitle(Localization.getString("filter.title"));
        ButtonType saveButton = new ButtonType(Localization.getString("dialog.button.save"), ButtonBar.ButtonData.OK_DONE);
        dlg.getDialogPane().setContent(content);
        dlg.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);
        dlg.setResultConverter(dialogButton -> {
            if (dialogButton == saveButton) {
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
        cbMarket.setSelected(filter.has(SERVICE_TYPE.MARKET));
        cbBlackMarket.setSelected(filter.has(SERVICE_TYPE.BLACK_MARKET));
        cbRefuel.setSelected(filter.has(SERVICE_TYPE.REFUEL));
        cbMunition.setSelected(filter.has(SERVICE_TYPE.MUNITION));
        cbRepair.setSelected(filter.has(SERVICE_TYPE.REPAIR));
        cbOutfit.setSelected(filter.has(SERVICE_TYPE.OUTFIT));
        cbShipyard.setSelected(filter.has(SERVICE_TYPE.SHIPYARD));
        cbMediumLandpad.setSelected(filter.has(SERVICE_TYPE.MEDIUM_LANDPAD));
        cbLargeLandpad.setSelected(filter.has(SERVICE_TYPE.LARGE_LANDPAD));
        excludes.setItems(BindingsHelper.observableList(filter.getExcludes(), market.getModeler()::get));
    }

    private void clear(){
        this.filter = null;
        center.setValue(ModelFabric.NONE_SYSTEM);
        radius.clear();
        distance.clear();
        excludes.getItems().clear();
    }

    private void save() {
        SystemModel s = center.getValue();
        LOG.trace("Old filter", filter);
        filter.setCenter(ModelFabric.isFake(s) ? null : ModelFabric.get(s));
        filter.setRadius(radius.getValue().doubleValue());
        filter.setDistance(distance.getValue().doubleValue());
        if (cbMarket.isSelected()) filter.add(SERVICE_TYPE.MARKET); else filter.remove(SERVICE_TYPE.MARKET);
        if (cbBlackMarket.isSelected()) filter.add(SERVICE_TYPE.BLACK_MARKET); else filter.remove(SERVICE_TYPE.BLACK_MARKET);
        if (cbRefuel.isSelected()) filter.add(SERVICE_TYPE.REFUEL); else filter.remove(SERVICE_TYPE.REFUEL);
        if (cbMunition.isSelected()) filter.add(SERVICE_TYPE.MUNITION); else filter.remove(SERVICE_TYPE.MUNITION);
        if (cbRepair.isSelected()) filter.add(SERVICE_TYPE.REPAIR); else filter.remove(SERVICE_TYPE.REPAIR);
        if (cbOutfit.isSelected()) filter.add(SERVICE_TYPE.OUTFIT); else filter.remove(SERVICE_TYPE.OUTFIT);
        if (cbShipyard.isSelected()) filter.add(SERVICE_TYPE.SHIPYARD); else filter.remove(SERVICE_TYPE.SHIPYARD);
        if (cbMediumLandpad.isSelected()) filter.add(SERVICE_TYPE.MEDIUM_LANDPAD); else filter.remove(SERVICE_TYPE.MEDIUM_LANDPAD);
        if (cbLargeLandpad.isSelected()) filter.add(SERVICE_TYPE.LARGE_LANDPAD); else filter.remove(SERVICE_TYPE.LARGE_LANDPAD);
        filter.clearExcludes();
        excludes.getItems().forEach(st -> filter.addExclude(ModelFabric.get(st)));
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

}
