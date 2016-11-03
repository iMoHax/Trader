package ru.trader.controllers;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.controlsfx.control.CheckComboBox;
import ru.trader.Main;
import ru.trader.analysis.PowerPlayAnalyzator;
import ru.trader.core.*;
import ru.trader.model.*;
import ru.trader.model.support.BindingsHelper;
import ru.trader.view.support.PowerStateStringConverter;
import ru.trader.view.support.PowerStringConverter;
import ru.trader.view.support.ViewUtils;
import ru.trader.view.support.autocomplete.AutoCompletion;
import ru.trader.view.support.autocomplete.CachedSuggestionProvider;
import ru.trader.view.support.autocomplete.SystemsProvider;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class PowerPlayController {

    @FXML
    private TextField checkedSystemText;
    private AutoCompletion<SystemModel> checkedSystem;
    @FXML
    private TextField controlSystemText;
    private AutoCompletion<SystemModel> controlSystem;
    @FXML
    private ComboBox<POWER> cbPower;
    @FXML
    private CheckComboBox<POWER_STATE> cbStates;

    @FXML
    private RadioButton rbIntersect;
    @FXML
    private RadioButton rbNear;
    @FXML
    private RadioButton rbMaxIntersect;
    @FXML
    private RadioButton rbExpansions;
    @FXML
    private RadioButton rbControlling;
    @FXML
    private ListView<SystemModel> controlSystems;
    @FXML
    private TableView<ResultEntry> tblResults;

    private MarketModel world;
    private ProfileModel profile;
    private PowerPlayAnalyzator analyzator;
    private final List<ResultEntry> result = FXCollections.observableArrayList();


    @FXML
    private void initialize(){
        init();
        profile = MainController.getProfile();

        cbPower.setConverter(new PowerStringConverter());
        cbPower.setItems(FXCollections.observableArrayList(POWER.values()));
        cbStates.setConverter(new PowerStateStringConverter());
        cbStates.getItems().setAll(POWER_STATE.values());
        cbStates.getCheckModel().check(POWER_STATE.CONTROL);
        cbStates.getCheckModel().check(POWER_STATE.HEADQUARTERS);

        BindingsHelper.setTableViewItems(tblResults, result);

        initListeners();
    }

    void init(){
        world = MainController.getWorld();
        analyzator = world.getPowerPlayAnalyzer();

        SystemsProvider provider = world.getSystemsProvider();
        if (checkedSystem == null){
            checkedSystem = new AutoCompletion<>(checkedSystemText, new CachedSuggestionProvider<>(provider), ModelFabric.NONE_SYSTEM, provider.getConverter());
        } else {
            checkedSystem.setSuggestions(provider.getPossibleSuggestions());
            checkedSystem.setConverter(provider.getConverter());
        }
        if (controlSystem == null){
            controlSystem = new AutoCompletion<>(controlSystemText, new CachedSuggestionProvider<>(provider), ModelFabric.NONE_SYSTEM, provider.getConverter());
        } else {
            controlSystem.setSuggestions(provider.getPossibleSuggestions());
            controlSystem.setConverter(provider.getConverter());
        }
    }

    private void initListeners(){
    }


    private void getIntersects(){
        Place starSystem = ModelFabric.get(checkedSystem.getValue());
        Collection<Place> controlls = getControlSystems();
        result.clear();
        if (starSystem != null && !controlls.isEmpty()){
            Collection<PowerPlayAnalyzator.IntersectData> intersects = analyzator.getIntersects(starSystem, controlls);
            result.addAll(BindingsHelper.observableList(intersects, d -> new ResultEntry(d, starSystem)));
        }
    }

    private void getControlling(){
        final Place starSystem = ModelFabric.get(checkedSystem.getValue());
        result.clear();
        if (starSystem != null){
            Collection<PowerPlayAnalyzator.IntersectData> controllings = analyzator.getControlling(starSystem);
            result.addAll(BindingsHelper.observableList(controllings,d -> new ResultEntry(d, starSystem)));
        }
    }

    private void getNear(){
        Collection<Place> controlls = getControlSystems();
        result.clear();
        if (!controlls.isEmpty()){
            Collection<PowerPlayAnalyzator.IntersectData> near = analyzator.getNear(controlls);
            result.addAll(BindingsHelper.observableList(near, ResultEntry::new));
        }
    }

    private void getNearExpansions(){
        Collection<Place> controlls = getControlSystems();
        result.clear();
        if (!controlls.isEmpty()){
            Collection<PowerPlayAnalyzator.IntersectData> near = analyzator.getNearExpansions(controlls);
            result.addAll(BindingsHelper.observableList(near, ResultEntry::new));
        }
    }


    @FXML
    private void currentAsChecked(){
        checkedSystem.setValue(profile.getSystem());
    }

    @FXML
    private void currentAsControl(){
        controlSystem.setValue(profile.getSystem());
    }


    private Collection<Place> getControlSystems(){
        return controlSystems.getItems().stream().map(ModelFabric::get).collect(Collectors.toSet());
    }

    @FXML
    private void search(){
        if (rbIntersect.isSelected()){
            getIntersects();
        }
        if (rbControlling.isSelected()){
            getControlling();
        }
        if (rbNear.isSelected()){
            getNear();
        }
        if (rbExpansions.isSelected()){
            getNearExpansions();
        }
    }


    @FXML
    private void addControlSystem(){
        SystemModel starSystem = controlSystem.getValue();
        if (!ModelFabric.isFake(starSystem)){
            controlSystems.getItems().add(starSystem);
        }
        POWER power = cbPower.getValue();
        if (power != null && power != POWER.NONE){
            StarSystemFilter filter = new StarSystemFilter(true);
            filter.add(power);
            cbStates.getCheckModel().getCheckedItems().forEach(filter::add);
            controlSystems.getItems().addAll(world.getSystems(filter));
        }
        cbPower.setValue(null);
        controlSystem.setValue(ModelFabric.NONE_SYSTEM);
    }

    @FXML
    private void removeControlSystem(){
        int index = controlSystems.getSelectionModel().getSelectedIndex();
        if (index >= 0){
            controlSystems.getItems().remove(index);
        }
    }

    @FXML
    private void clearControlSystems(){
        controlSystems.getItems().clear();
    }

    @FXML
    private void copyToClipboard(){
        ResultEntry entry = tblResults.getSelectionModel().getSelectedItem();
        if (entry != null){
            Main.copyToClipboard(entry.starSystem.getName());
        }
    }


    public class ResultEntry {
        private final SystemModel starSystem;
        private final StationModel nearStation;
        private final ReadOnlyDoubleProperty distance;
        private final ReadOnlyStringProperty maxSizePad;
        private final ReadOnlyIntegerProperty intersectCount;
        private final ReadOnlyStringProperty controlling;

        public ResultEntry(PowerPlayAnalyzator.IntersectData data) {
            this(data, null);
        }

        public ResultEntry(PowerPlayAnalyzator.IntersectData data, Place from) {
            starSystem = world.getModeler().get(data.getStarSystem());
            maxSizePad = new SimpleStringProperty(starSystem.getMaxSizePad());
            intersectCount = new SimpleIntegerProperty(data.getCount());
            nearStation = starSystem.getNear();
            controlling = new SimpleStringProperty(getControllingString(data.getControllingSystems()));
            distance = new SimpleDoubleProperty(from != null ? from.getDistance(data.getStarSystem()) : Double.NaN);
        }

        private String getControllingString(Collection<PowerPlayAnalyzator.ControllingData> controllings) {
            StringBuilder res = new StringBuilder();
            for (PowerPlayAnalyzator.ControllingData data : controllings) {
                if (res.length() != 0) res.append("\n");
                res.append(data.getCenter().getName());
                res.append(" (").append(ViewUtils.distanceToString(data.getDistance())).append(")");
            }
            return res.toString();
        }


        public ReadOnlyStringProperty stationProperty(){
            return new SimpleStringProperty(String.format("%s (%.0f Ls)", nearStation.getName(), nearStation.getDistance()));
        }

        public ReadOnlyStringProperty nameProperty(){
            return starSystem.nameProperty();
        }

        public GOVERNMENT getGovernment() {
            return starSystem.getGovernment();
        }

        public FACTION getFaction() {
            return starSystem.getFaction();
        }

        public POWER getPower() {
            return starSystem.getPower();
        }

        public POWER_STATE getPowerState() {
            return starSystem.getPowerState();
        }

        public ReadOnlyDoubleProperty distanceProperty() {
            return distance;
        }

        public ReadOnlyStringProperty maxSizePadProperty() {
            return maxSizePad;
        }

        public ReadOnlyIntegerProperty intersectCountProperty() {
            return intersectCount;
        }

        public ReadOnlyStringProperty controllingProperty() {
            return controlling;
        }
    }
}