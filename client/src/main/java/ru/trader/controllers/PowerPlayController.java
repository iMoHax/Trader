package ru.trader.controllers;

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
import ru.trader.model.MarketModel;
import ru.trader.model.ModelFabric;
import ru.trader.model.ProfileModel;
import ru.trader.model.SystemModel;
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
import java.util.Map;
import java.util.Optional;
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

    private PowerPlayAnalyzator.ControllingRadiusStat resultStat;
    private PowerPlayAnalyzator.ControllingRadiusStat detailStat;

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
    }

    private void fillDetail(SystemModel detailSystem) {
        final Place starSystem = ModelFabric.get(detailSystem);
        this.detailSystem = starSystem;
        detail.clear();
        if (starSystem != null){
            Collection<PowerPlayAnalyzator.IntersectData> controllings = analyzator.getControlling(starSystem);
            controllings.add(new PowerPlayAnalyzator.IntersectData(starSystem));
            toDetail(controllings, starSystem);
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
            toResult(intersects, starSystem);
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
            toResult(controllings, starSystem);
        }
    }

    private void getNear(){
        Collection<Place> controlls = getControlSystems();
        result.clear();
        if (!controlls.isEmpty()){
            Collection<PowerPlayAnalyzator.IntersectData> near = analyzator.getNear(controlls);
            toResult(near);
        }
    }

    private void getMaxProfit(){
        if (hqSystem.isPresent()){
            final Place hq = ModelFabric.get(hqSystem.get());
            Collection<Place> controlls = getControlSystems();
            result.clear();
            Collection<PowerPlayAnalyzator.IntersectData> near = analyzator.getMaxProfit(hq, controlls);
            toResult(near);
        }
    }

    private void getNearExpansions(){
        Collection<Place> controlls = getControlSystems();
        result.clear();
        if (!controlls.isEmpty()){
            Collection<PowerPlayAnalyzator.IntersectData> near = analyzator.getNearExpansions(controlls);
            toResult(near);
        }
    }

    private void getMaxIntersect() {
        Collection<Place> controlls = getControlSystems();
        result.clear();
        if (!controlls.isEmpty()){
            Collection<PowerPlayAnalyzator.IntersectData> intersect = analyzator.getMaxIntersect(controlls);
            toResult(intersect);
        }
    }

    private void toDetail(Collection<PowerPlayAnalyzator.IntersectData> datas) {
        toDetail(datas, null);
    }

    private void toDetail(Collection<PowerPlayAnalyzator.IntersectData> datas, Place from){
        Place hq = ModelFabric.get(hqSystem.orElse(null));
        Collection<Place> places = detailSystem != null ? Collections.singleton(detailSystem) : null;
        detailStat = new PowerPlayAnalyzator.ControllingRadiusStat(places, hq, datas);

        if (from != null){
            detail.addAll(BindingsHelper.observableList(datas, d -> new ResultEntry(d, from)));
        } else {
            detail.addAll(BindingsHelper.observableList(datas, ResultEntry::new));
        }

        detailCCSumm.setText(statToText(detailStat, false));
    }


    private void toResult(Collection<PowerPlayAnalyzator.IntersectData> datas) {
        toResult(datas, null);
    }

    private void toResult(Collection<PowerPlayAnalyzator.IntersectData> datas, Place from){
        Place hq = ModelFabric.get(hqSystem.orElse(null));
        Collection<Place> places = getSelectedSystems();
        resultStat = new PowerPlayAnalyzator.ControllingRadiusStat(places, hq, datas);

        if (from != null){
            result.addAll(BindingsHelper.observableList(datas, d -> new ResultEntry(d, from)));
        } else {
            result.addAll(BindingsHelper.observableList(datas, ResultEntry::new));
        }

        resultCCSumm.setText(statToText(resultStat, false));
    }

    private String statToText(PowerPlayAnalyzator.ControllingRadiusStat stat, boolean full){
        final String line = Localization.getString("powerplay.text.line");

        StringBuilder builder = new StringBuilder();
        PowerStringConverter converter = new PowerStringConverter();
        if (full) {
            int index = 0;
            for (Place place : stat.getStarSystems()) {
                if (index++ > 0) builder.append("\n");
                builder.append(place.getName());
                if (stat.getHeadquarter() != null) {
                    builder.append(" ").append(ViewUtils.distanceToString(place.getDistance(stat.getHeadquarter()))).append(" ");
                }
                SystemModel model = world.getModeler().get(place);
                builder.append(" (").append(ViewUtils.stationsAsStringByType(model.getNearByType(), true)).append(")");
            }
            builder.append("\n").append(line).append("\n");
            builder.append(String.format(Localization.getString("powerplay.text.detail"), stat.getIncome(), stat.getUpkeep(), stat.getContest(),
                    stat.getExploited(), stat.getEnemyExploited(), stat.getBlocked(), stat.getEnemyBlocked(), stat.getCurrentRadiusProfit()));
            builder.append("\n").append(line).append("\n");
            builder.append(Localization.getString("powerplay.text.summary.title")).append("\n");
        }
        builder.append(String.format(Localization.getString("powerplay.text.summary"), stat.getIncome(), stat.getUpkeep(), stat.getIncome()+stat.getUpkeep(),
                    stat.getFutureContest(), stat.getFutureExploited(), stat.getFutureRadiusProfit()));
        if (full) {
            builder.append("\n").append(line).append("\n");
            builder.append(Localization.getString("powerplay.text.contest.title"));
        }
        builder.append("\n");
        for (Map.Entry<POWER, PowerPlayAnalyzator.StarSystemsStat> entry : stat.getContestStat().entrySet()) {
            POWER power = entry.getKey();
            PowerPlayAnalyzator.StarSystemsStat powerStat = entry.getValue();
            builder.append(String.format(Localization.getString("powerplay.text.contest.powers"), converter.toString(power), powerStat.getExploited(), powerStat.getIntersect(), powerStat.getContest()));
            builder.append("\n");
            for (Map.Entry<Place, PowerPlayAnalyzator.StarSystemsStat> systemsStatEntry : stat.getContestStatByStarSystems().entrySet()) {
                Place place = systemsStatEntry.getKey();
                if (place.getPower() == power) {
                    PowerPlayAnalyzator.StarSystemsStat systemStat = systemsStatEntry.getValue();
                    builder.append(String.format(Localization.getString("powerplay.text.contest.systems"), place.getName(), systemStat.getExploited(), systemStat.getIntersect(), systemStat.getContest()));
                    builder.append("\n");
                }
            }
        }
        return builder.toString();
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
                historySystems.getSelectionModel().clearSelection();
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

    @FXML
    private void copyResultStatToClipboard(){
        if (resultStat != null) {
            Main.copyToClipboard(statToText(resultStat, true));
        }
    }

    @FXML
    private void copyDetailStatToClipboard(){
        if (detailStat != null) {
            Main.copyToClipboard(statToText(detailStat, true));
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
        private final ReadOnlyDoubleProperty profit;
        private final ReadOnlyLongProperty contested;
        private final ReadOnlyLongProperty cc;

        public ResultEntry(PowerPlayAnalyzator.IntersectData data) {
            this(data, null);
        }

        public ResultEntry(PowerPlayAnalyzator.IntersectData data, Place from) {
            starSystem = world.getModeler().get(data.getStarSystem());
            intersectCount = new SimpleIntegerProperty(data.getCount());
            nearStations = new SimpleStringProperty(ViewUtils.stationsAsStringByType(starSystem.getNearByType(), false));
            intersecting = new SimpleStringProperty(getControllingString(data.getControllingSystems()));
            controlling = new SimpleStringProperty(getControllingString(data.getStarSystem()));
            distance = new SimpleDoubleProperty(from != null ? from.getDistance(data.getStarSystem()) : Double.NaN);
            Place hq = ModelFabric.get(hqSystem.orElse(null));
            distanceHQ = new SimpleDoubleProperty(hq != null ? hq.getDistance(data.getStarSystem()) : Double.NaN);
            population = new SimpleLongProperty(data.getStarSystem().getPopulation());
            currentUpkeep = new SimpleLongProperty(data.getStarSystem().getUpkeep());
            cc = new SimpleLongProperty(data.getStarSystem().computeCC());

            Collection<PowerPlayAnalyzator.IntersectData> datas = analyzator.getControlling(data.getStarSystem());
            datas.add(new PowerPlayAnalyzator.IntersectData(data.getStarSystem()));
            PowerPlayAnalyzator.ControllingRadiusStat stat = new PowerPlayAnalyzator.ControllingRadiusStat(Collections.singleton(data.getStarSystem()), hq, datas);
            income = new SimpleLongProperty(stat.getIncome());
            upkeep = new SimpleDoubleProperty(stat.getUpkeep());
            profit = new SimpleDoubleProperty(stat.getFutureRadiusProfit());
            contested = new SimpleLongProperty(stat.getFutureContest());

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

        public ReadOnlyDoubleProperty profitProperty() {
            return profit;
        }

        public ReadOnlyLongProperty contestedProperty() {
            return contested;
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