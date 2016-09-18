package ru.trader.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.util.Callback;
import javafx.util.converter.LongStringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.core.*;
import ru.trader.model.ItemModel;
import ru.trader.model.StationModel;
import ru.trader.model.SystemModel;
import ru.trader.model.support.StationUpdater;
import ru.trader.view.support.*;
import ru.trader.view.support.cells.DecoratedRowFactory;
import ru.trader.view.support.cells.EditOfferCell;
import ru.trader.view.support.cells.TextFieldCell;

import java.util.Optional;


public class StationEditorController {
    private final static Logger LOG = LoggerFactory.getLogger(StationEditorController.class);

    @FXML
    private TextField name;
    @FXML
    private ComboBox<STATION_TYPE> type;
    @FXML
    private ComboBox<FACTION> faction;
    @FXML
    private ComboBox<GOVERNMENT> government;
    @FXML
    private ComboBox<ECONOMIC_TYPE> economic;
    @FXML
    private ComboBox<ECONOMIC_TYPE> subEconomic;

    @FXML
    private TableView<StationUpdater.FakeOffer> items;
    @FXML
    private TableColumn<StationUpdater.FakeOffer, Double> buy;
    @FXML
    private TableColumn<StationUpdater.FakeOffer, Double> sell;
    @FXML
    private TableColumn<StationUpdater.FakeOffer, Long> supply;
    @FXML
    private TableColumn<StationUpdater.FakeOffer, Long> demand;

    @FXML
    private NumberField distance;
    @FXML
    private CheckBox cbMarket;
    @FXML
    private CheckBox cbRefuel;
    @FXML
    private CheckBox cbBlackMarket;
    @FXML
    private CheckBox cbRepair;
    @FXML
    private CheckBox cbMunition;
    @FXML
    private CheckBox cbOutfit;
    @FXML
    private CheckBox cbShipyard;

    private StationUpdater updater;

    private Dialog<ButtonType> dlg;

    @FXML
    private void initialize() {
        type.setItems(FXCollections.observableArrayList(STATION_TYPE.values()));
        type.setConverter(new StationTypeStringConverter());
        faction.setItems(FXCollections.observableArrayList(FACTION.values()));
        faction.setConverter(new FactionStringConverter());
        faction.valueProperty().addListener(e -> updateItems());
        government.setItems(FXCollections.observableArrayList(GOVERNMENT.values()));
        government.setConverter(new GovernmentStringConverter());
        government.valueProperty().addListener(e -> updateItems());
        economic.setItems(FXCollections.observableArrayList(ECONOMIC_TYPE.values()));
        economic.setConverter(new EconomicTypeStringConverter());
        subEconomic.setItems(FXCollections.observableArrayList(ECONOMIC_TYPE.values()));
        subEconomic.setConverter(new EconomicTypeStringConverter());
        items.getSelectionModel().setCellSelectionEnabled(true);
        items.setRowFactory(new FakeOfferDecoratedRow());
        buy.setCellFactory(EditOfferCell.forTable(new PriceStringConverter(), false));
        sell.setCellFactory(EditOfferCell.forTable(new PriceStringConverter(), true));
        demand.setCellFactory(TextFieldCell.forTableColumn(new LongStringConverter()));
        supply.setCellFactory(TextFieldCell.forTableColumn(new LongStringConverter()));
        name.setOnAction((v) -> distance.requestFocus());
        distance.setOnAction((v) -> {
            items.requestFocus();
            items.getSelectionModel().select(0, buy);
        });
        init();
    }

    void init(){
        if (updater != null){
            name.textProperty().unbindBidirectional(updater.nameProperty());
            type.valueProperty().unbindBidirectional(updater.typeProperty());
            faction.valueProperty().unbindBidirectional(updater.factionProperty());
            government.valueProperty().unbindBidirectional(updater.governmentProperty());
            distance.numberProperty().unbindBidirectional(updater.distanceProperty());
            economic.valueProperty().unbindBidirectional(updater.economicProperty());
            subEconomic.valueProperty().unbindBidirectional(updater.subEconomicProperty());
        }
        updater = new StationUpdater(MainController.getMarket());
        name.textProperty().bindBidirectional(updater.nameProperty());
        type.valueProperty().bindBidirectional(updater.typeProperty());
        faction.valueProperty().bindBidirectional(updater.factionProperty());
        government.valueProperty().bindBidirectional(updater.governmentProperty());
        distance.numberProperty().bindBidirectional(updater.distanceProperty());
        economic.valueProperty().bindBidirectional(updater.economicProperty());
        subEconomic.valueProperty().bindBidirectional(updater.subEconomicProperty());
        cbMarket.selectedProperty().bindBidirectional(updater.serviceProperty(SERVICE_TYPE.MARKET));
        cbBlackMarket.selectedProperty().bindBidirectional(updater.serviceProperty(SERVICE_TYPE.BLACK_MARKET));
        cbRefuel.selectedProperty().bindBidirectional(updater.serviceProperty(SERVICE_TYPE.REFUEL));
        cbMunition.selectedProperty().bindBidirectional(updater.serviceProperty(SERVICE_TYPE.MUNITION));
        cbRepair.selectedProperty().bindBidirectional(updater.serviceProperty(SERVICE_TYPE.REPAIR));
        cbOutfit.selectedProperty().bindBidirectional(updater.serviceProperty(SERVICE_TYPE.OUTFIT));
        cbShipyard.selectedProperty().bindBidirectional(updater.serviceProperty(SERVICE_TYPE.SHIPYARD));
        items.setItems(updater.getOffers());
    }

    private void updateItems(){
        items.setItems(null);
        items.layout();
        if (updater != null){
            items.setItems(updater.getOffers());
        }
    }

    private void createDialog(Parent owner, Parent content){
        dlg = new Dialog<>();
        if (owner != null) dlg.initOwner(owner.getScene().getWindow());


        dlg.getDialogPane().setContent(content);
        dlg.getDialogPane().getButtonTypes().addAll(Dialogs.SAVE, Dialogs.CANCEL);

        Button bSave = (Button) dlg.getDialogPane().lookupButton(Dialogs.SAVE);
        bSave.disableProperty().bind(distance.wrongProperty());

        dlg.setResultConverter(dialogButton -> {
            if (dialogButton == Dialogs.SAVE) {
                save();
            }
            if (dialogButton == Dialogs.CANCEL) {
                cancel();
            }
            return dialogButton;
        });
        dlg.setResizable(false);
    }

    private void save(){
        items.getSelectionModel().selectFirst();
        updater.commit();
        items.getSelectionModel().clearSelection();
    }

    private void cancel(){
        items.getSelectionModel().selectFirst();
        items.getSelectionModel().clearSelection();
    }

    public void showDialog(Parent parent, Parent content, StationModel station){
        if (dlg == null){
            createDialog(parent, content);
        }
        dlg.setTitle(Localization.getString("vEditor.title.edit"));
        updater.edit(station);
        dlg.showAndWait();
        updater.reset();
    }

    public void showDialog(Parent parent, Parent content, SystemModel system){
        if (dlg == null){
            createDialog(parent, content);
        }
        dlg.setTitle(Localization.getString("vEditor.title.add"));
        updater.create(system);
        dlg.showAndWait();
        updater.reset();
    }

    public void up(){
        int index = items.getSelectionModel().getSelectedIndex();
        if (index>0){
            StationUpdater.FakeOffer offer = items.getItems().remove(index);
            items.getItems().add(index-1, offer);
            selectRow(index - 1);
        }
    }

    public void down(){
        int index = items.getSelectionModel().getSelectedIndex();
        if (index>=0 && index<items.getItems().size()-1){
            StationUpdater.FakeOffer offer = items.getItems().remove(index);
            items.getItems().add(index+1, offer);
            selectRow(index + 1);
        }
    }

    public void add() {
        Optional<ItemModel> item = Screeners.showAddItem();
        if (item.isPresent()){
            int index = items.getSelectionModel().getSelectedIndex();
            if (index<0) index = items.getItems().size()-1;
            updater.add(index, item.get());
            selectRow(index);
        }
    }

    private void selectRow(int index){
        items.requestFocus();
        items.getSelectionModel().select(index, items.getColumns().get(0));
        ViewUtils.show(items, index);
    }

    private class FakeOfferDecoratedRow extends DecoratedRowFactory<StationUpdater.FakeOffer> {

        public FakeOfferDecoratedRow() {
            super();
        }

        public FakeOfferDecoratedRow(Callback<TableView<StationUpdater.FakeOffer>, TableRow<StationUpdater.FakeOffer>> decorated) {
            super(decorated);
        }

        @Override
        protected void doStyle(TableRow<StationUpdater.FakeOffer> row, StationUpdater.FakeOffer entry) {
            ObservableList<String> styles = row.getStyleClass();
            styles.remove(ViewUtils.ILLEGAL_ITEM_STYLE);
            if (entry != null){
                GOVERNMENT g = government.getValue();
                FACTION f = faction.getValue();
                if (entry.getItem().isIllegal(updater.getSystem(), f, g)){
                    styles.add(ViewUtils.ILLEGAL_ITEM_STYLE);
                }
            }
        }
    }
}
