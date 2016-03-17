package ru.trader.db.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.ComboBoxTableCell;
import ru.trader.controllers.MainController;
import ru.trader.controllers.Screeners;
import ru.trader.core.*;
import ru.trader.model.MarketModel;
import ru.trader.model.SystemModel;
import ru.trader.model.support.ChangeMarketListener;
import ru.trader.view.support.*;

import java.util.Optional;

public class SystemsController {

    @FXML
    private TableView<SystemModel> tblSystems;
    @FXML
    private TableColumn<SystemModel, FACTION> faction;
    @FXML
    private TableColumn<SystemModel, GOVERNMENT> government;
    @FXML
    private TableColumn<SystemModel, POWER> power;
    @FXML
    private TableColumn<SystemModel, POWER_STATE> powerState;


    private ObservableList<SystemModel> stations = FXCollections.observableArrayList();
    private MarketModel world = null;

    @FXML
    private void initialize() {
        tblSystems.setItems(stations);
        faction.setCellFactory(ComboBoxTableCell.forTableColumn(new FactionStringConverter(), FXCollections.observableArrayList(FACTION.values())));
        government.setCellFactory(ComboBoxTableCell.forTableColumn(new GovernmentStringConverter(), FXCollections.observableArrayList(GOVERNMENT.values())));
        power.setCellFactory(ComboBoxTableCell.forTableColumn(new PowerStringConverter(), FXCollections.observableArrayList(POWER.values())));
        powerState.setCellFactory(ComboBoxTableCell.forTableColumn(new PowerStateStringConverter(), FXCollections.observableArrayList(POWER_STATE.values())));

        init();
    }

    void init(){
        if (world != null) world.getNotificator().remove(marketChangeListener);
        world = MainController.getWorld();
        world.getNotificator().add(marketChangeListener);
        stations.clear();
        stations.addAll(world.getSystems());
    }

    @FXML
    private void add(){
        Screeners.showSystemsEditor(null);
    }

    @FXML
    private void edit(){
        SystemModel system = tblSystems.getSelectionModel().getSelectedItem();
        if (system != null){
            Screeners.showSystemsEditor(system);
        }
    }

    @FXML
    private void remove(){
        SystemModel system = tblSystems.getSelectionModel().getSelectedItem();
        if (system != null){
            remove(system);
        }
    }

    private void remove(SystemModel system){
        Optional<ButtonType> res = Screeners.showConfirm(String.format(Localization.getString("dialog.confirm.remove"), system.getName()));
        if (res.isPresent() && res.get() == ButtonType.YES) {
            world.remove(system);
        }
    }


    private final ChangeMarketListener marketChangeListener = new ChangeMarketListener() {
        @Override
        public void add(SystemModel system) {
            ViewUtils.doFX(() -> {
                SystemsController.this.stations.add(system);
            });
        }

        @Override
        public void remove(SystemModel system) {
            ViewUtils.doFX(() -> {
                SystemsController.this.stations.remove(system);
            });
        }
    };
}

