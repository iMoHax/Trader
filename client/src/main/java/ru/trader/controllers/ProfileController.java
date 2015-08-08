package ru.trader.controllers;


import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;
import ru.trader.core.Engine;
import ru.trader.model.MarketModel;
import ru.trader.model.ProfileModel;
import ru.trader.model.StationModel;
import ru.trader.model.SystemModel;
import ru.trader.view.support.NumberField;
import ru.trader.view.support.ViewUtils;
import ru.trader.view.support.autocomplete.AutoCompletion;
import ru.trader.view.support.autocomplete.SystemsProvider;



public class ProfileController {

    @FXML
    private TextField name;
    @FXML
    private NumberField balance;
    @FXML
    private TextField systemText;
    @FXML
    private ComboBox<StationModel> station;
    @FXML
    private NumberField mass;
    @FXML
    private NumberField tank;
    @FXML
    private NumberField cargo;
    @FXML
    private ComboBox<Engine> engine;
    @FXML
    private Button btnAddSystem;
    @FXML
    private Button btnAddStation;

    private AutoCompletion<SystemModel> system;
    private ProfileModel profile;
    private boolean ignoreChanges;

    @FXML
    private void initialize() {
        profile = MainController.getProfile();
        MarketModel world = MainController.getWorld();
        SystemsProvider provider = new SystemsProvider(world);
        system = new AutoCompletion<>(systemText, provider, provider.getConverter());
        engine.setItems(FXCollections.observableList(Engine.getEngines()));
        engine.setConverter(new EngineStringConverter());
        btnAddSystem.setOnAction(e -> Screeners.showSystemsEditor(null));
        btnAddStation.setOnAction(e -> Screeners.showAddStation(profile.getSystem()));
        initListeners();
    }

    private void initListeners(){
        name.textProperty().addListener((ov, o, n) -> {
            if (!ignoreChanges)
                profile.setName(n);
        });
        balance.numberProperty().addListener((ov, o, n) -> {
            if (!ignoreChanges)
                profile.setBalance(n.doubleValue());
        });
        system.completionProperty().addListener((ov, o , n) -> {
            if (!ignoreChanges){
                profile.setSystem(n);
            }
            station.setItems(n.getStationsList());
        });
        station.valueProperty().addListener((ov, o, n) -> {
            if (!ignoreChanges)
                profile.setStation(n);
        });
        mass.numberProperty().addListener((ov, o, n) -> {
            if (!ignoreChanges)
                profile.setShipMass(n.doubleValue());
        });
        tank.numberProperty().addListener((ov, o, n) -> {
            if (!ignoreChanges)
                profile.setShipTank(n.doubleValue());
        });
        cargo.numberProperty().addListener((ov, o, n) -> {
            if (!ignoreChanges)
                profile.setShipCargo(n.intValue());
        });
        engine.valueProperty().addListener((ov, o, n) -> {
            if (!ignoreChanges)
                profile.setShipEngine(n);
        });
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
        station.setValue(profile.getStation());
        mass.setValue(profile.getShipMass());
        tank.setValue(profile.getShipTank());
        cargo.setValue(profile.getShipCargo());
        engine.setValue(profile.getShipEngine());
        bind();
        ignoreChanges = false;
    }

    private void bind(){
        profile.nameProperty().addListener(nameListener);
        profile.balanceProperty().addListener(balanceListener);
        profile.systemProperty().addListener(systemListener);
        profile.stationProperty().addListener(stationListener);
        profile.shipMassProperty().addListener(massListener);
        profile.shipTankProperty().addListener(tankListener);
        profile.shipCargoProperty().addListener(cargoListener);
        profile.shipEngineProperty().addListener(engineListener);
    }

    private void unbind(){
        profile.nameProperty().removeListener(nameListener);
        profile.balanceProperty().removeListener(balanceListener);
        profile.systemProperty().removeListener(systemListener);
        profile.stationProperty().removeListener(stationListener);
        profile.shipMassProperty().removeListener(massListener);
        profile.shipTankProperty().removeListener(tankListener);
        profile.shipCargoProperty().removeListener(cargoListener);
        profile.shipEngineProperty().removeListener(engineListener);
    }


    private final ChangeListener<String> nameListener = (ov, o, n) -> ViewUtils.doFX(() -> name.setText(n));
    private final ChangeListener<Number> balanceListener = (ov, o, n) -> ViewUtils.doFX(() -> balance.setValue(n));
    private final ChangeListener<SystemModel> systemListener = (ov, o, n) -> ViewUtils.doFX(() -> system.setValue(n));
    private final ChangeListener<StationModel> stationListener = (ov, o, n) -> ViewUtils.doFX(() -> station.setValue(n));
    private final ChangeListener<Number> massListener = (ov, o, n) -> ViewUtils.doFX(() -> mass.setValue(n));
    private final ChangeListener<Number> tankListener = (ov, o, n) -> ViewUtils.doFX(() -> tank.setValue(n));
    private final ChangeListener<Number> cargoListener = (ov, o, n) -> ViewUtils.doFX(() -> cargo.setValue(n));
    private final ChangeListener<Engine> engineListener = (ov, o, n) -> ViewUtils.doFX(() -> engine.setValue(n));

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
