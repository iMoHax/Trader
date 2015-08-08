package ru.trader.controllers;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.util.converter.LongStringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.EMDNUpdater;
import ru.trader.core.SERVICE_TYPE;
import ru.trader.model.ItemModel;
import ru.trader.model.StationModel;
import ru.trader.model.SystemModel;
import ru.trader.model.support.StationUpdater;
import ru.trader.view.support.Localization;
import ru.trader.view.support.NumberField;
import ru.trader.view.support.PriceStringConverter;
import ru.trader.view.support.ViewUtils;
import ru.trader.view.support.cells.EditOfferCell;
import ru.trader.view.support.cells.TextFieldCell;

import java.util.Optional;


public class StationEditorController {
    private final static Logger LOG = LoggerFactory.getLogger(StationEditorController.class);

    @FXML
    private TextField name;

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
    private CheckBox cbBlackMarket;
    @FXML
    private CheckBox cbRepair;
    @FXML
    private CheckBox cbMunition;
    @FXML
    private CheckBox cbOutfit;
    @FXML
    private CheckBox cbShipyard;
    @FXML
    private CheckBox cbMediumLandpad;
    @FXML
    private CheckBox cbLargeLandpad;

    private StationUpdater updater;

    private Dialog<ButtonType> dlg;

    @FXML
    private void initialize() {
        items.getSelectionModel().setCellSelectionEnabled(true);
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
            distance.numberProperty().unbindBidirectional(updater.distanceProperty());
        }
        updater = new StationUpdater(MainController.getMarket());
        name.textProperty().bindBidirectional(updater.nameProperty());
        distance.numberProperty().bindBidirectional(updater.distanceProperty());
        cbMarket.selectedProperty().bindBidirectional(updater.serviceProperty(SERVICE_TYPE.MARKET));
        cbBlackMarket.selectedProperty().bindBidirectional(updater.serviceProperty(SERVICE_TYPE.BLACK_MARKET));
        cbMunition.selectedProperty().bindBidirectional(updater.serviceProperty(SERVICE_TYPE.MUNITION));
        cbRepair.selectedProperty().bindBidirectional(updater.serviceProperty(SERVICE_TYPE.REPAIR));
        cbOutfit.selectedProperty().bindBidirectional(updater.serviceProperty(SERVICE_TYPE.OUTFIT));
        cbShipyard.selectedProperty().bindBidirectional(updater.serviceProperty(SERVICE_TYPE.SHIPYARD));
        cbMediumLandpad.selectedProperty().bindBidirectional(updater.serviceProperty(SERVICE_TYPE.MEDIUM_LANDPAD));
        cbLargeLandpad.selectedProperty().bindBidirectional(updater.serviceProperty(SERVICE_TYPE.LARGE_LANDPAD));
        items.setItems(updater.getOffers());
    }


    private void createDialog(Parent owner, Parent content){
        dlg = new Dialog<>();
        if (owner != null) dlg.initOwner(owner.getScene().getWindow());

        ButtonType saveButton = new ButtonType(Localization.getString("dialog.button.save"), ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType(Localization.getString("dialog.button.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);

        dlg.getDialogPane().setContent(content);
        dlg.getDialogPane().getButtonTypes().addAll(saveButton, cancelButton);

        Button bSave = (Button) dlg.getDialogPane().lookupButton(saveButton);
        bSave.disableProperty().bind(distance.wrongProperty());

        dlg.setResultConverter(dialogButton -> {
            if (dialogButton == saveButton) {
                save();
            }
            if (dialogButton == cancelButton) {
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

    public void updateFromEMDN(){
        EMDNUpdater.updateFromEMDN(updater);
    }
}
