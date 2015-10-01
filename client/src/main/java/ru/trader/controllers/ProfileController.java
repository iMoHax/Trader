package ru.trader.controllers;


import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;
import ru.trader.core.Engine;
import ru.trader.model.*;
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
        system = new AutoCompletion<>(systemText, provider, ModelFabric.NONE_SYSTEM, provider.getConverter());
        engine.setItems(FXCollections.observableList(Engine.getEngines()));
        engine.setConverter(new EngineStringConverter());
        btnAddSystem.setOnAction(e -> Screeners.showSystemsEditor(null));
        btnAddStation.setOnAction(e -> Screeners.showAddStation(profile.getSystem()));
        initListeners();
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

    private void initListeners(){
        name.textProperty().addListener((ov, o, n) -> consumeChanges(() -> profile.setName(n)));
        balance.numberProperty().addListener((ov, o, n) -> consumeChanges(() -> profile.setBalance(n.doubleValue())));
        system.completionProperty().addListener((ov, o , n) -> {
            doAndConsumeChanges(() -> station.setItems(n.getStationsList()));
            consumeChanges(() -> {profile.setSystem(n); profile.setStation(ModelFabric.NONE_STATION);});
        });
        station.valueProperty().addListener((ov, o, n) -> consumeChanges(() -> profile.setStation(n)));
        mass.numberProperty().addListener((ov, o, n) -> consumeChanges(() -> profile.setShipMass(n.doubleValue())));
        tank.numberProperty().addListener((ov, o, n) -> consumeChanges(() -> profile.setShipTank(n.doubleValue())));
        cargo.numberProperty().addListener((ov, o, n) -> consumeChanges(() -> profile.setShipCargo(n.intValue())));
        engine.valueProperty().addListener((ov, o, n) -> consumeChanges(() -> profile.setShipEngine(n)));
    }

    public void setProfile(ProfileModel profile){
        if (this.profile != null){
            unbind();
        }
        this.profile = profile;
        name.setText(profile.getName());
        balance.setValue(profile.getBalance());
        system.setValue(profile.getSystem());
        station.setValue(profile.getStation());
        mass.setValue(profile.getShipMass());
        tank.setValue(profile.getShipTank());
        cargo.setValue(profile.getShipCargo());
        engine.setValue(profile.getShipEngine());
        bind();
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


    private final ChangeListener<String> nameListener = (ov, o, n) -> consumeChanges(() -> name.setText(n));
    private final ChangeListener<Number> balanceListener = (ov, o, n) -> consumeChanges(() -> balance.setValue(n));
    private final ChangeListener<SystemModel> systemListener = (ov, o, n) -> consumeChanges(()  -> system.setValue(n));
    private final ChangeListener<StationModel> stationListener = (ov, o, n) -> consumeChanges(() -> station.setValue(n));
    private final ChangeListener<Number> massListener = (ov, o, n) -> consumeChanges(() -> mass.setValue(n));
    private final ChangeListener<Number> tankListener = (ov, o, n) -> consumeChanges(() -> tank.setValue(n));
    private final ChangeListener<Number> cargoListener = (ov, o, n) -> consumeChanges(() -> cargo.setValue(n));
    private final ChangeListener<Engine> engineListener = (ov, o, n) -> consumeChanges(() -> engine.setValue(n));

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
