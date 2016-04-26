package ru.trader.controllers;


import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.util.StringConverter;
import ru.trader.Main;
import ru.trader.ServicesManager;
import ru.trader.core.Engine;
import ru.trader.model.*;
import ru.trader.model.support.ChangeMarketListener;
import ru.trader.view.support.NumberField;
import ru.trader.view.support.ViewUtils;
import ru.trader.view.support.autocomplete.AutoCompletion;
import ru.trader.view.support.autocomplete.CachedSuggestionProvider;
import ru.trader.view.support.autocomplete.SystemsProvider;



public class ProfileController {

    @FXML
    private TextField name;
    @FXML
    private NumberField balance;
    @FXML
    private TextField systemText;
    @FXML
    private ComboBox<String> station;
    @FXML
    private CheckBox docked;
    @FXML
    private NumberField mass;
    @FXML
    private NumberField tank;
    @FXML
    private NumberField cargo;
    @FXML
    private ComboBox<Engine> engine;
    @FXML
    private Label jumpRange;
    @FXML
    private Pane profileInfo;
    @FXML
    private Pane shipInfo;
    @FXML
    private Button btnAddSystem;
    @FXML
    private Button btnAddStation;
    @FXML
    private ToggleButton btnHelper;
    @FXML
    private ToggleButton btnEDCE;
    @FXML
    private Node warningIcon;

    private AutoCompletion<SystemModel> system;
    private ProfileModel profile;
    private boolean ignoreChanges;

    @FXML
    private void initialize() {
        init();
        system.valueProperty().addListener((ov, o, n) -> {
            doAndConsumeChanges(() -> {
                station.setItems(n.getStationNamesList());
                station.getSelectionModel().selectFirst();
            });
            consumeChanges(() -> {
                profile.setSystem(n);
                profile.setStation(ModelFabric.NONE_STATION);
            });
        });
        system.valueProperty().addListener(i -> updateIcons());
        engine.setItems(FXCollections.observableList(Engine.getEngines()));
        engine.setConverter(new EngineStringConverter());
        btnAddSystem.setOnAction(e -> {
            if (ModelFabric.isFake(profile.getSystem())) Screeners.showSystemsEditor(null);
            else Screeners.showSystemsEditor(profile.getSystem());
        });
        btnAddStation.setOnAction(e -> {
            if (ModelFabric.isFake(profile.getStation())) Screeners.showAddStation(profile.getSystem());
            else Screeners.showEditStation(profile.getStation());
        });
        btnHelper.setOnAction(e -> toggleHelper());
        btnHelper.setSelected(Main.SETTINGS.helper().isVisible());
        shipInfo.setVisible(false);
        initListeners();
    }

    @FXML
    private void toggleShipInfo(){
        if (shipInfo.isVisible()){
            profileInfo.setVisible(true);
            shipInfo.setVisible(false);
        } else {
            profileInfo.setVisible(false);
            shipInfo.setVisible(true);
        }
    }

    @FXML
    private void toggleHelper(){
        Screeners.toggleHelper();
    }

    private void consumeChanges(Runnable runnable){
        if (ignoreChanges) return;
        ignoreChanges = true;
        ViewUtils.doFX(runnable);
        ignoreChanges = false;
    }

    private void doAndConsumeChanges(Runnable runnable){
        boolean old = ignoreChanges;
        ignoreChanges = true;
        ViewUtils.doFX(runnable);
        ignoreChanges = old;
    }

    void init(){
        profile = MainController.getProfile();
        MarketModel world = MainController.getWorld();
        world.getNotificator().add(marketChangeListener);
        SystemsProvider provider = world.getSystemsProvider();
        if (system == null){
            system = new AutoCompletion<>(systemText, new CachedSuggestionProvider<>(provider), ModelFabric.NONE_SYSTEM, provider.getConverter());
        } else {
            system.setSuggestions(provider.getPossibleSuggestions());
            system.setConverter(provider.getConverter());
        }
    }

    private void initListeners(){
        name.textProperty().addListener((ov, o, n) -> consumeChanges(() -> profile.setName(n)));
        balance.numberProperty().addListener((ov, o, n) -> consumeChanges(() -> profile.setBalance(n.doubleValue())));
        station.valueProperty().addListener((ov, o, n) -> consumeChanges(() -> profile.setStation(getStation(n))));
        docked.selectedProperty().addListener((ov, o, n) -> consumeChanges(() -> profile.setDocked(n)));
        mass.numberProperty().addListener((ov, o, n) -> consumeChanges(() -> profile.setShipMass(n.doubleValue())));
        tank.numberProperty().addListener((ov, o, n) -> consumeChanges(() -> profile.setShipTank(n.doubleValue())));
        cargo.numberProperty().addListener((ov, o, n) -> consumeChanges(() -> profile.setShipCargo(n.intValue())));
        engine.valueProperty().addListener((ov, o, n) -> consumeChanges(() -> profile.setShipEngine(n)));
    }

    public void initEDCEBtn(){
        btnEDCE.selectedProperty().bindBidirectional(Main.SETTINGS.edce().activeProperty());
        setEDCEBtnStyles(btnEDCE.isSelected());
        ServicesManager.getEdce().activeProperty().addListener((ov, o, n) -> {
            setEDCEBtnStyles(n);
        });

    }

    private void setEDCEBtnStyles(boolean active){
        final String CSS_OK_CLASS = "service-ok";
        final String CSS_WARNING_CLASS = "service-warning";
        if (active) {
            btnEDCE.getStyleClass().remove(CSS_WARNING_CLASS);
            btnEDCE.getStyleClass().add(CSS_OK_CLASS);
        } else {
            btnEDCE.getStyleClass().remove(CSS_OK_CLASS);
            if (btnEDCE.isSelected()) {
                btnEDCE.getStyleClass().add(CSS_WARNING_CLASS);
            }
        }
    }

    public void setProfile(ProfileModel profile){
        if (this.profile != null){
            unbind();
        }
        this.profile = profile;
        ignoreChanges = true;
        name.setText(profile.getName());
        balance.setValue(profile.getBalance());
        system.setValue(profile.getSystem());
        station.setValue(profile.getStation().getName());
        docked.setSelected(profile.isDocked());
        mass.setValue(profile.getShipMass());
        tank.setValue(profile.getShipTank());
        cargo.setValue(profile.getShipCargo());
        engine.setValue(profile.getShipEngine());
        ignoreChanges = false;
        bind();
    }

    private StationModel getStation(){
        return getStation(station.getValue());
    }

    private StationModel getStation(String name){
        SystemModel s = system.getValue();
        return s == null ? ModelFabric.NONE_STATION : s.get(name);
    }

    private void updateIcons(){
        SystemModel s = system.getValue();
        if (!s.isCorrect()){
            warningIcon.setVisible(true);
            return;
        }
        StationModel st = getStation();
        if (!st.isCorrect()){
            warningIcon.setVisible(true);
            return;
        }
        warningIcon.setVisible(false);
    }

    private void bind(){
        profile.nameProperty().addListener(nameListener);
        profile.balanceProperty().addListener(balanceListener);
        profile.systemProperty().addListener(systemListener);
        profile.stationProperty().addListener(stationListener);
        profile.dockedProperty().addListener(dockedListener);
        profile.shipMassProperty().addListener(massListener);
        profile.shipTankProperty().addListener(tankListener);
        profile.shipCargoProperty().addListener(cargoListener);
        profile.shipEngineProperty().addListener(engineListener);
        jumpRange.textProperty().bind(Bindings.createStringBinding(()-> String.format("%.1f - %.1f - %.1f", profile.getShipJumpRange(), profile.getMaxShipJumpRange(), profile.getEmptyMaxShipJumpRange()),
                    profile.shipMassProperty(), profile.shipCargoProperty(), profile.shipTankProperty(), profile.shipEngineProperty()
        ));
    }

    private void unbind(){
        profile.nameProperty().removeListener(nameListener);
        profile.balanceProperty().removeListener(balanceListener);
        profile.systemProperty().removeListener(systemListener);
        profile.stationProperty().removeListener(stationListener);
        profile.dockedProperty().removeListener(dockedListener);
        profile.shipMassProperty().removeListener(massListener);
        profile.shipTankProperty().removeListener(tankListener);
        profile.shipCargoProperty().removeListener(cargoListener);
        profile.shipEngineProperty().removeListener(engineListener);
        jumpRange.textProperty().unbind();
    }


    private final ChangeListener<String> nameListener = (ov, o, n) -> consumeChanges(() -> name.setText(n));
    private final ChangeListener<Number> balanceListener = (ov, o, n) -> consumeChanges(() -> balance.setValue(n));
    private final ChangeListener<SystemModel> systemListener = (ov, o, n) -> consumeChanges(()  -> system.setValue(n));
    private final ChangeListener<StationModel> stationListener = (ov, o, n) -> consumeChanges(() -> station.setValue(n.getName()));
    private final ChangeListener<Boolean> dockedListener = (ov, o, n) -> consumeChanges(() -> docked.setSelected(n));
    private final ChangeListener<Number> massListener = (ov, o, n) -> consumeChanges(() -> mass.setValue(n));
    private final ChangeListener<Number> tankListener = (ov, o, n) -> consumeChanges(() -> tank.setValue(n));
    private final ChangeListener<Number> cargoListener = (ov, o, n) -> consumeChanges(() -> cargo.setValue(n));
    private final ChangeListener<Engine> engineListener = (ov, o, n) -> consumeChanges(() -> engine.setValue(n));

    private final ChangeMarketListener marketChangeListener = new ChangeMarketListener() {

        @Override
        public void add(StationModel station) {
            ViewUtils.doFX(() -> {
                if (station.getSystem().equals(system.getValue())) {
                    ProfileController.this.station.getItems().add(station.getName());
                }
            });
        }

        @Override
        public void remove(StationModel station) {
            ViewUtils.doFX(() -> {
                if (station.getSystem().equals(system.getValue())) {
                    ProfileController.this.station.getItems().remove(station.getName());
                }
            });
        }
    };

    private class EngineStringConverter extends StringConverter<Engine> {
        @Override
        public String toString(Engine engine) {
            return ""+engine.getClazz()+engine.getRating();
        }

        @Override
        public Engine fromString(String string) {
            throw new UnsupportedOperationException();
        }
    }

}
