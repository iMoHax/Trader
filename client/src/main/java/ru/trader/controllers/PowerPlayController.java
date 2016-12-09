package ru.trader.controllers;

import javafx.beans.InvalidationListener;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.*;
import org.controlsfx.control.CheckComboBox;
import org.controlsfx.control.MasterDetailPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.Main;
import ru.trader.analysis.PowerPlayAnalyzator;
import ru.trader.core.*;
import ru.trader.model.*;
import ru.trader.model.support.BindingsHelper;
import ru.trader.view.support.Localization;
import ru.trader.view.support.PowerStateStringConverter;
import ru.trader.view.support.PowerStringConverter;
import ru.trader.view.support.ViewUtils;
import ru.trader.view.support.autocomplete.AutoCompletion;
import ru.trader.view.support.autocomplete.CachedSuggestionProvider;
import ru.trader.view.support.autocomplete.SystemsProvider;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class PowerPlayController {
    private final static Logger LOG = LoggerFactory.getLogger(PowerPlayController.class);

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
    private RadioButton rbMaxProfit;
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
    private Label resultCCSumm;
    @FXML
    private Label detailCCSumm;

    private MarketModel world;
    private ProfileModel profile;
    private Optional<SystemModel> hqSystem;
    private PowerPlayAnalyzator analyzator;
    private final ObservableList<ResultEntry> result = FXCollections.observableArrayList();
    private final ObservableList<ResultEntry> detail = FXCollections.observableArrayList();
    private Place detailSystem;

    @FXML
    private void initialize(){
        init();
        profile = MainController.getProfile();

        historySystems.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

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
        result.clear();
        detail.clear();
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

        controlSystems.setOnDragDetected(new StarSystemDragDetect(controlSystems));
        controlSystems.setOnDragEntered(new StarSystemDragEntered(controlSystems));
        controlSystems.setOnDragExited(new StarSystemDragExited(controlSystems));
        controlSystems.setOnDragOver(new StarSystemDragOver(controlSystems));
        controlSystems.setOnDragDropped(new StarSystemDragDrop(controlSystems));


        historySystems.getSelectionModel().selectedItemProperty().addListener((ov, o, n) -> {
            if (n != null) {
                checkedSystem.setValue(n);
            }
        });
        historySystems.setOnDragDetected(new StarSystemDragDetect(historySystems));
        historySystems.setOnDragEntered(new StarSystemDragEntered(historySystems));
        historySystems.setOnDragExited(new StarSystemDragExited(historySystems));
        historySystems.setOnDragOver(new StarSystemDragOver(historySystems));
        historySystems.setOnDragDropped(new StarSystemDragDrop(historySystems));

        tblResults.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                if (!tblResults.getSelectionModel().isEmpty()) {
                    tblResults.getSelectionModel().clearSelection();
                }
            }
        });
        tblResults.setOnDragDetected(new StarSystemDragDetect(tblResults));
        tblResults.getSelectionModel().selectedItemProperty().addListener((ov, o, n) -> {
            if (n != null) {
                fillDetail(n.starSystem);
                resultPane.setShowDetailNode(true);
            } else {
                resultPane.setShowDetailNode(false);
            }
        });
        tblDetail.setOnDragDetected(new StarSystemDragDetect(tblDetail));
        result.addListener((InvalidationListener) i -> {
                    resultCCSumm.setText(getCCSummText(result, getSelectedSystems()));
                }
        );
        detail.addListener((InvalidationListener) i -> {
                    detailCCSumm.setText(getCCSummText(detail, detailSystem != null ? Collections.singleton(detailSystem) : null));
                }
        );
    }

    private String getCCSummText(Collection<ResultEntry> collection, Collection<Place> starSystems){
        String ccFormat = Localization.getString("powerplay.label.summcc");
        String pwCCFormat = Localization.getString("powerplay.label.cc");
        PowerStringConverter converter = new PowerStringConverter();
        Place hq = ModelFabric.get(hqSystem.orElse(null));
        long[] contestedCc = new long[POWER.values().length];
        long[] intersectedCc = new long[POWER.values().length];
        long[] totalCc = new long[POWER.values().length];
        long contested = 0;
        long intersected = 0;
        long summCc = 0;
        for (ResultEntry entry : collection) {
            long cc = entry.getCc();
            summCc += cc;
            if (entry.getPower() == null || entry.getPowerState() == null) continue;
            if (entry.getPowerState() != POWER_STATE.NONE){
                if (hq == null || entry.getPowerState().isContested() || entry.getPower() != hq.getPower()) {
                    contested += cc;
                }
                if (hq != null && entry.getPowerState().isExploited() && entry.getPower() == hq.getPower()) {
                    intersected += cc;
                }
                Set<POWER> powers = entry.getControllingSystems().stream().map(Place::getPower).collect(Collectors.toSet());
                if (entry.getPowerState().isContested()){
                    for (POWER power : powers){
                        contestedCc[power.ordinal()] += cc;
                    }
                } else {
                    if (entry.getControllingSystems().size()>1){
                        intersectedCc[entry.getPower().ordinal()] += cc;
                    }
                    if (entry.getPowerState().isControl() || entry.getPowerState().isExploited()) {
                        totalCc[entry.getPower().ordinal()] += cc;
                    }
                }
            }
        }
        double upkeep = 0;
        if (hq != null && starSystems != null){
            for (Place starSystem : starSystems) {
                upkeep += starSystem.computeUpkeep(hq);
            }
        }

        StringBuilder builder = new StringBuilder();
        builder.append(String.format(ccFormat, summCc, contested, summCc - contested, upkeep, intersected, summCc - contested - upkeep - intersected));
        for (int i = 0; i < POWER.values().length; i++) {
            if (totalCc[i] > 0 || contestedCc[i] > 0){
                builder.append("\n");
                builder.append(String.format(pwCCFormat, converter.toString(POWER.values()[i]), totalCc[i], contestedCc[i], intersectedCc[i]));
            }
        }
        return builder.toString();
    }

    private void fillDetail(SystemModel detailSystem) {
        final Place starSystem = ModelFabric.get(detailSystem);
        this.detailSystem = starSystem;
        detail.clear();
        if (starSystem != null){
            Collection<PowerPlayAnalyzator.IntersectData> controllings = analyzator.getControlling(starSystem);
            controllings.add(new PowerPlayAnalyzator.IntersectData(starSystem));
            detail.addAll(BindingsHelper.observableList(controllings, d -> new ResultEntry(d, starSystem)));
        }
    }

    private Place getCheckedSystem(){
        return ModelFabric.get(checkedSystem.getValue());
    }

    private Collection<Place> getSelectedSystems() {
        Collection<Place> places = historySystems.getSelectionModel().getSelectedItems().stream().map(ModelFabric::get).collect(Collectors.toSet());
        if (places.isEmpty()){
            Place starSystem = getCheckedSystem();
            if (starSystem != null){
                return Collections.singleton(starSystem);
            }
        }
        return places;
    }

    private void getIntersects(){
        Place starSystem = getCheckedSystem();
        Collection<Place> controlls = getControlSystems();
        result.clear();
        if (starSystem != null && !controlls.isEmpty()){
            Collection<PowerPlayAnalyzator.IntersectData> intersects = analyzator.getIntersects(starSystem, controlls);
            result.addAll(BindingsHelper.observableList(intersects, d -> new ResultEntry(d, starSystem)));
        }
    }

    private void getControlling(){
        final Place starSystem = getCheckedSystem();
        final Collection<Place> selectedSystems = getSelectedSystems();
        result.clear();
        if (starSystem != null || !selectedSystems.isEmpty()){
            Collection<PowerPlayAnalyzator.IntersectData> controllings =
                    selectedSystems.isEmpty() ? analyzator.getControlling(starSystem) : analyzator.getControlling(selectedSystems);
            controllings.add(new PowerPlayAnalyzator.IntersectData(starSystem));
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

    private void getMaxProfit(){
        if (hqSystem.isPresent()){
            final Place hq = ModelFabric.get(hqSystem.get());
            Collection<Place> controlls = getControlSystems();
            result.clear();
            Collection<PowerPlayAnalyzator.IntersectData> near = analyzator.getMaxProfit(hq, controlls);
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

    private void getMaxIntersect(){
        Collection<Place> controlls = getControlSystems();
        result.clear();
        if (!controlls.isEmpty()){
            Collection<PowerPlayAnalyzator.IntersectData> intersect = analyzator.getMaxIntersect(controlls);
            result.addAll(BindingsHelper.observableList(intersect, ResultEntry::new));
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
        if (rbMaxIntersect.isSelected()){
            getMaxIntersect();
        }
        if (rbMaxProfit.isSelected()){
            getMaxProfit();
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
        historySystems.getItems().clear();
    }

    private SystemModel getFocusedSystem(){
        if (historySystems.isFocused()) return historySystems.getSelectionModel().getSelectedItem();
        if (controlSystems.isFocused()) return controlSystems.getSelectionModel().getSelectedItem();
        if (tblResults.isFocused()){
            ResultEntry entry = tblResults.getSelectionModel().getSelectedItem();
            return entry != null ? entry.starSystem : null;
        }
        if (tblDetail.isFocused()){
            ResultEntry entry = tblDetail.getSelectionModel().getSelectedItem();
            return entry != null ? entry.starSystem : null;
        }
        return null;
    }

    @FXML
    private void copySystemToClipboard(){
        SystemModel starSystem = getFocusedSystem();
        if (starSystem != null){
            Main.copyToClipboard(starSystem.getName());
        }
    }

    @FXML
    private void editSystem(){
        SystemModel starSystem = getFocusedSystem();
        if (starSystem != null){
            Screeners.showSystemsEditor(starSystem);
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
        private final ReadOnlyLongProperty currentUpkeep;
        private final ReadOnlyLongProperty income;
        private final ReadOnlyDoubleProperty upkeep;
        private final ReadOnlyLongProperty cc;

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
            currentUpkeep = new SimpleLongProperty(data.getStarSystem().getUpkeep());
            upkeep = new SimpleDoubleProperty(hq != null ? data.getStarSystem().computeUpkeep(hq) : Double.NaN);
            income = new SimpleLongProperty(data.getStarSystem().getIncome());
            cc = new SimpleLongProperty(data.getStarSystem().computeCC());
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

        private Collection<Place> getControllingSystems(){
            Place place = ModelFabric.get(starSystem);
            return place != null ? place.getControllingSystems() : Collections.emptyList();
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

        public ReadOnlyDoubleProperty upkeepProperty() {
            return upkeep;
        }

        public ReadOnlyLongProperty currentUpkeepProperty() {
            return currentUpkeep;
        }

        public ReadOnlyLongProperty incomeProperty() {
            return income;
        }

        public ReadOnlyStringProperty nearStationsProperty() {
            return nearStations;
        }

        public long getCc() {
            return cc.get();
        }

        public ReadOnlyLongProperty ccProperty() {
            return cc;
        }
    }

    private class StarSystemDragDetect implements EventHandler<MouseEvent> {
        private final Node source;

        public StarSystemDragDetect(TableView<ResultEntry> source) {
            this.source = source;
        }

        public StarSystemDragDetect(ListView<SystemModel> source) {
            this.source = source;
        }

        @Override
        public void handle(MouseEvent event) {
            SystemModel starSystem = getStarSystem();
            if (starSystem != null) {
                Dragboard db = source.startDragAndDrop(TransferMode.COPY);
                ClipboardContent content = new ClipboardContent();
                content.putString(starSystem.getName());
                db.setContent(content);
                event.consume();
            }
        }


        private SystemModel getStarSystem(){
            if (source instanceof TableView){
                @SuppressWarnings("unchecked")
                TableView<ResultEntry> tbl = (TableView<ResultEntry>) source;
                ResultEntry entry = tbl.getSelectionModel().getSelectedItem();
                if (entry != null) return entry.starSystem;
            } else
            if (source instanceof ListView){
                @SuppressWarnings("unchecked")
                ListView<SystemModel> list = (ListView<SystemModel>) source;
                SystemModel entry = list.getSelectionModel().getSelectedItem();
                if (entry != null) return entry;
            }
            return null;
        }
    }

    private class StarSystemDragDrop implements EventHandler<DragEvent> {
        private final ListView<SystemModel> target;

        private StarSystemDragDrop(ListView<SystemModel> target) {
            this.target = target;
        }

        @Override
        public void handle(DragEvent event) {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasString()) {
                SystemModel starSystem = world.get(db.getString());
                if (!ModelFabric.isFake(starSystem)){
                    target.getItems().add(starSystem);
                }
                success = true;
            }
            event.setDropCompleted(success);
            event.consume();
        }
    }

    private class StarSystemDragOver implements EventHandler<DragEvent> {
        private final ListView<SystemModel> target;

        private StarSystemDragOver(ListView<SystemModel> target) {
            this.target = target;
        }

        @Override
        public void handle(DragEvent event) {
            if (event.getGestureSource() != target && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }
            event.consume();
        }
    }


    private class StarSystemDragEntered implements EventHandler<DragEvent> {
        private final Node target;

        private StarSystemDragEntered(Node target) {
            this.target = target;
        }

        @Override
        public void handle(DragEvent event) {
            if (event.getGestureSource() != target && event.getDragboard().hasString()) {
                target.getStyleClass().add(ViewUtils.DRAG_CSS_CLASS);
            }
            event.consume();
        }
    }

    private class StarSystemDragExited implements EventHandler<DragEvent> {
        private final Node target;

        private StarSystemDragExited(Node target) {
            this.target = target;
        }

        @Override
        public void handle(DragEvent event) {
            if (event.getGestureSource() != target && event.getDragboard().hasString()) {
                target.getStyleClass().remove(ViewUtils.DRAG_CSS_CLASS);
            }
            event.consume();
        }
    }

}