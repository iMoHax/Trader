package ru.trader.controllers;

import javafx.beans.InvalidationListener;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.util.converter.NumberStringConverter;
import org.controlsfx.control.CheckComboBox;
import org.controlsfx.control.MasterDetailPane;
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
import java.util.Optional;
import java.util.stream.Collectors;

public class PowerPlayController {

    @FXML
    private TextField checkedSystemText;
    private AutoCompletion<SystemModel> checkedSystem;
    @FXML
    private TextField controlSystemText;
    private AutoCompletion<SystemModel> controlSystem;
    @FXML
    private ComboBox<POWER> cbCurrentPower;
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
    private ListView<SystemModel> historySystems;
    @FXML
    private ListView<SystemModel> controlSystems;
    @FXML
    private MasterDetailPane resultPane;
    @FXML
    private TableView<ResultEntry> tblResults;
    @FXML
    private TableView<ResultEntry> tblDetail;
    @FXML
    private Label resultPopSumm;
    @FXML
    private Label detailPopSumm;

    private MarketModel world;
    private ProfileModel profile;
    private Optional<SystemModel> hqSystem;
    private PowerPlayAnalyzator analyzator;
    private final ObservableList<ResultEntry> result = FXCollections.observableArrayList();
    private final ObservableList<ResultEntry> detail = FXCollections.observableArrayList();


    @FXML
    private void initialize(){
        init();
        profile = MainController.getProfile();

        cbCurrentPower.setConverter(new PowerStringConverter());
        cbCurrentPower.setItems(FXCollections.observableArrayList(POWER.values()));
        cbPower.setConverter(new PowerStringConverter());
        cbPower.setItems(FXCollections.observableArrayList(POWER.values()));
        cbStates.setConverter(new PowerStateStringConverter());
        cbStates.getItems().setAll(POWER_STATE.values());
        cbStates.getCheckModel().check(POWER_STATE.CONTROL);
        cbStates.getCheckModel().check(POWER_STATE.HEADQUARTERS);
        cbStates.getCheckModel().check(POWER_STATE.TURMOIL);

        BindingsHelper.setTableViewItems(tblResults, result);
        BindingsHelper.setTableViewItems(tblDetail, detail);

        initListeners();
    }

    void init(){
        //TODO: add to screens reinit

        world = MainController.getWorld();
        analyzator = world.getPowerPlayAnalyzer();
        if (cbCurrentPower.getValue() != POWER.NONE && cbCurrentPower.getValue() != null){
            hqSystem = getHeadquarter(cbCurrentPower.getValue());
        } else {
            hqSystem = Optional.empty();
        }
        historySystems.getItems().clear();
        controlSystems.getItems().clear();

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

    private Optional<SystemModel> getHeadquarter(POWER power) {
        StarSystemFilter filter = new StarSystemFilter(true);
        filter.add(power);
        filter.add(POWER_STATE.HEADQUARTERS);
        return world.getSystems(filter).stream().findFirst();
    }

    private void initListeners(){
        cbCurrentPower.valueProperty().addListener((ov, o, n) -> {
            hqSystem = getHeadquarter(n);
        });

        historySystems.getSelectionModel().selectedItemProperty().addListener((ov, o, n) -> {
            if (n != null) {
                checkedSystem.setValue(n);
            }
        });

        tblResults.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                if (!tblResults.getSelectionModel().isEmpty()) {
                    tblResults.getSelectionModel().clearSelection();
                }
            }
        });
        tblResults.getSelectionModel().selectedItemProperty().addListener((ov, o, n) -> {
            if (n != null) {
                fillDetail(n.starSystem);
                resultPane.setShowDetailNode(true);
            } else {
                resultPane.setShowDetailNode(false);
            }
        });
        NumberStringConverter converter = new NumberStringConverter("#,##0.##");
        result.addListener((InvalidationListener) i ->
            resultPopSumm.setText(converter.toString(getPopulationSumm(result)))
        );
        detail.addListener((InvalidationListener) i ->
                        detailPopSumm.setText(converter.toString(getPopulationSumm(detail)))
        );
    }

    private long getPopulationSumm(Collection<ResultEntry> collection){
        return collection.stream().mapToLong(ResultEntry::getPopulation).sum();
    }

    private void fillDetail(SystemModel detailSystem) {
        final Place starSystem = ModelFabric.get(detailSystem);
        detail.clear();
        if (starSystem != null){
            Collection<PowerPlayAnalyzator.IntersectData> controllings = analyzator.getControlling(starSystem);
            detail.addAll(BindingsHelper.observableList(controllings, d -> new ResultEntry(d, starSystem)));
        }
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
        addHistorySystem();
        if (controlSystems.getItems().isEmpty()){
            addControlSystem();
        }
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
    private void addHistorySystem() {
        addHistorySystem(checkedSystem.getValue());
    }

    private void addHistorySystem(SystemModel starSystem){
        if (!ModelFabric.isFake(starSystem)){
            if (!historySystems.getItems().contains(starSystem)) {
                historySystems.getItems().add(starSystem);
            }
        }
    }

    @FXML
    private void removeHistorySystem(){
        int index = historySystems.getSelectionModel().getSelectedIndex();
        if (index >= 0){
            historySystems.getItems().remove(index);
        }
    }

    @FXML
    private void clearHistorySystems(){
        controlSystems.getItems().clear();
    }


    @FXML
    private void copySystemToClipboard(){
        SystemModel starSystem = null;
        if (historySystems.isFocused()) starSystem = historySystems.getSelectionModel().getSelectedItem();
        if (controlSystems.isFocused()) starSystem = controlSystems.getSelectionModel().getSelectedItem();
        if (tblResults.isFocused()){
            ResultEntry entry = tblResults.getSelectionModel().getSelectedItem();
            starSystem = entry != null ? entry.starSystem : null;
        }
        if (tblDetail.isFocused()){
            ResultEntry entry = tblDetail.getSelectionModel().getSelectedItem();
            starSystem = entry != null ? entry.starSystem : null;
        }
        if (starSystem != null){
            Main.copyToClipboard(starSystem.getName());
        }
    }

    public class ResultEntry {
        private final SystemModel starSystem;
        private final ReadOnlyStringProperty nearStations;
        private final ReadOnlyDoubleProperty distance;
        private final ReadOnlyDoubleProperty distanceHQ;
        private final ReadOnlyIntegerProperty intersectCount;
        private final ReadOnlyStringProperty intersecting;
        private final ReadOnlyStringProperty controlling;
        private final ReadOnlyLongProperty population;
        private final ReadOnlyLongProperty upkeep;
        private final ReadOnlyLongProperty income;

        public ResultEntry(PowerPlayAnalyzator.IntersectData data) {
            this(data, null);
        }

        public ResultEntry(PowerPlayAnalyzator.IntersectData data, Place from) {
            starSystem = world.getModeler().get(data.getStarSystem());
            intersectCount = new SimpleIntegerProperty(data.getCount());
            nearStations = new SimpleStringProperty(getStationsString(starSystem.getNearByType()));
            intersecting = new SimpleStringProperty(getControllingString(data.getControllingSystems()));
            controlling = new SimpleStringProperty(getControllingString(data.getStarSystem()));
            distance = new SimpleDoubleProperty(from != null ? from.getDistance(data.getStarSystem()) : Double.NaN);
            Place hq = ModelFabric.get(hqSystem.orElse(null));
            distanceHQ = new SimpleDoubleProperty(hq != null ? hq.getDistance(data.getStarSystem()) : Double.NaN);
            population = new SimpleLongProperty(data.getStarSystem().getPopulation());
            upkeep = new SimpleLongProperty(data.getStarSystem().getUpkeep());
            income = new SimpleLongProperty(data.getStarSystem().getIncome());

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

        private String getControllingString(Place place) {
            StringBuilder res = new StringBuilder();
            for (Place system : place.getControllingSystems()) {
                if (res.length() != 0) res.append("\n");
                res.append(system.getName());
                res.append(" (").append(ViewUtils.distanceToString(system.getDistance(place))).append(")");
            }
            return res.toString();
        }

        private String getStationsString(Collection<StationModel> stations) {
            StringBuilder res = new StringBuilder();
            for (StationModel station : stations) {
                if (res.length() != 0) res.append("\n");
                if (station.getType() != null){
                    if (station.getType().isPlanetary()) {
                        res.append("LP");
                    } else
                    if (station.getType().hasLargeLandpad()){
                        res.append("L");
                    } else {
                        res.append("M");
                    }
                } else {
                    res.append("?");
                }
                res.append(" - ").append(station.getName());
                res.append(" (").append(ViewUtils.stationDistanceToString(station.getDistance())).append(")");
            }
            return res.toString();
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

        public ReadOnlyDoubleProperty distanceHQProperty() {
            return distanceHQ;
        }

        public ReadOnlyIntegerProperty intersectCountProperty() {
            return intersectCount;
        }

        public ReadOnlyStringProperty controllingProperty() {
            return controlling;
        }

        public ReadOnlyStringProperty intersectingProperty() {
            return intersecting;
        }

        public long getPopulation() {
            return population.get();
        }

        public ReadOnlyLongProperty populationProperty() {
            return population;
        }

        public ReadOnlyLongProperty upkeepProperty() {
            return upkeep;
        }

        public ReadOnlyLongProperty incomeProperty() {
            return income;
        }

        public ReadOnlyStringProperty nearStationsProperty() {
            return nearStations;
        }
    }
}