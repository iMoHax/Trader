package ru.trader.controllers;

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
    private TableView<SystemModel> tblResults;

    private MarketModel world;
    private ProfileModel profile;
    private PowerPlayAnalyzator analyzator;
    private final List<SystemModel> result = FXCollections.observableArrayList();


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
            Collection<Place> intersects = analyzator.getIntersects(starSystem, controlls);
            result.addAll(BindingsHelper.observableList(intersects, world.getModeler()::get));
        }
    }

    private void getControlling(){
        Place starSystem = ModelFabric.get(checkedSystem.getValue());
        result.clear();
        if (starSystem != null){
            Collection<Place> controllings = analyzator.getControlling(starSystem);
            result.addAll(BindingsHelper.observableList(controllings, world.getModeler()::get));
        }
    }

    private void getNear(){
        Collection<Place> controlls = getControlSystems();
        result.clear();
        if (!controlls.isEmpty()){
            Collection<Place> near = analyzator.getNear(controlls);
            result.addAll(BindingsHelper.observableList(near, world.getModeler()::get));
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
        SystemModel starSystem = tblResults.getSelectionModel().getSelectedItem();
        if (starSystem != null){
            Main.copyToClipboard(starSystem.getName());
        }
    }

}
