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
import ru.trader.model.StationModel;
import ru.trader.model.MarketModel;
import ru.trader.model.support.ChangeMarketListener;
import ru.trader.view.support.*;
import ru.trader.view.support.cells.CheckComboBoxTableCell;

import java.util.Collection;
import java.util.Optional;

public class StationsController {

    @FXML
    private TableView<StationModel> tblStations;
    @FXML
    private TableColumn<StationModel, STATION_TYPE> type;
    @FXML
    private TableColumn<StationModel, FACTION> faction;
    @FXML
    private TableColumn<StationModel, GOVERNMENT> government;
    @FXML
    private TableColumn<StationModel, ECONOMIC_TYPE> economic;
    @FXML
    private TableColumn<StationModel, ECONOMIC_TYPE> subEconomic;
    @FXML
    private TableColumn<StationModel, Collection<SERVICE_TYPE>> services;


    private ObservableList<StationModel> stations = FXCollections.observableArrayList();
    private MarketModel world = null;

    @FXML
    private void initialize() {
        tblStations.setItems(stations);
        type.setCellFactory(ComboBoxTableCell.forTableColumn(new StationTypeStringConverter(), FXCollections.observableArrayList(STATION_TYPE.values())));
        faction.setCellFactory(ComboBoxTableCell.forTableColumn(new FactionStringConverter(), FXCollections.observableArrayList(FACTION.values())));
        government.setCellFactory(ComboBoxTableCell.forTableColumn(new GovernmentStringConverter(), FXCollections.observableArrayList(GOVERNMENT.values())));
        economic.setCellFactory(ComboBoxTableCell.forTableColumn(new EconomicTypeStringConverter(), FXCollections.observableArrayList(ECONOMIC_TYPE.values())));
        subEconomic.setCellFactory(ComboBoxTableCell.forTableColumn(new EconomicTypeStringConverter(), FXCollections.observableArrayList(ECONOMIC_TYPE.values())));
        services.setCellFactory(CheckComboBoxTableCell.forTableColumn(services,
                FXCollections.observableArrayList(SERVICE_TYPE.values()), new ServiceTypeStringConverter(), this::setService)
        );

        init();
    }

    void init(){
        if (world != null) world.getNotificator().remove(marketChangeListener);
        world = MainController.getWorld();
        world.getNotificator().add(marketChangeListener);
        stations.clear();
        stations.addAll(world.getStations());
    }

    private void setService(StationModel station, SERVICE_TYPE service, boolean add){
        if (add){
            station.addService(service);
        } else {
            station.removeService(service);
        }
    }

    @FXML
    private void add(){
        StationModel station = tblStations.getSelectionModel().getSelectedItem();
        if (station != null){
            Screeners.showAddStation(station.getSystem());
        }
    }

    @FXML
    private void edit(){
        StationModel station = tblStations.getSelectionModel().getSelectedItem();
        if (station != null){
            Screeners.showEditStation(station);
        }
    }

    @FXML
    private void remove(){
        StationModel station = tblStations.getSelectionModel().getSelectedItem();
        if (station != null){
            remove(station);
        }
    }

    private void remove(StationModel station){
        Optional<ButtonType> res = Screeners.showConfirm(String.format(Localization.getString("dialog.confirm.remove"), station.getFullName()));
        if (res.isPresent() && res.get() == ButtonType.YES) {
            station.getSystem().remove(station);
        }
    }


    private final ChangeMarketListener marketChangeListener = new ChangeMarketListener() {
        @Override
        public void add(StationModel station) {
            ViewUtils.doFX(() -> {
                StationsController.this.stations.add(station);
            });
        }

        @Override
        public void remove(StationModel station) {
            ViewUtils.doFX(() -> {
                StationsController.this.stations.remove(station);
            });
        }
    };
}

