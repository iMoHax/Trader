package ru.trader.controllers;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import org.controlsfx.control.ButtonBar;
import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;
import org.controlsfx.dialog.DialogAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.EMDNUpdater;
import ru.trader.model.*;
import ru.trader.model.support.StationUpdater;
import ru.trader.view.support.Localization;
import ru.trader.view.support.NumberField;
import ru.trader.view.support.PriceStringConverter;
import ru.trader.view.support.ViewUtils;
import ru.trader.view.support.cells.EditOfferCell;

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
    private NumberField x;
    @FXML
    private NumberField y;
    @FXML
    private NumberField z;

    private StationUpdater updater;

    private final Action actSave = new DialogAction(Localization.getString("dialog.button.save"), ButtonBar.ButtonType.OK_DONE, false, true, false, (e) -> {
        items.getSelectionModel().selectFirst();
        updater.commit();
        items.getSelectionModel().clearSelection();
    });

    private final Action actCancel = new DialogAction(impl.org.controlsfx.i18n.Localization.asKey("dlg.cancel.button"), ButtonBar.ButtonType.CANCEL_CLOSE, true, true, true, (e) -> {
        items.getSelectionModel().selectFirst();
        items.getSelectionModel().clearSelection();
    });

    @FXML
    private void initialize() {
        items.getSelectionModel().setCellSelectionEnabled(true);
        buy.setCellFactory(EditOfferCell.forTable(new PriceStringConverter(), false));
        sell.setCellFactory(EditOfferCell.forTable(new PriceStringConverter(), true));
        actSave.disabledProperty().bind(x.wrongProperty().or(y.wrongProperty().or(z.wrongProperty())));
        name.setOnAction((v)->x.requestFocus());
        x.setOnAction((v) -> z.requestFocus());
        z.setOnAction((v) -> y.requestFocus());
        y.setOnAction((v) -> {
            items.requestFocus();
            items.getSelectionModel().select(0, buy);
        });
        init();
    }

    private void init(){
        updater = new StationUpdater(MainController.getMarket());
        name.textProperty().bindBidirectional(updater.nameProperty());
        x.numberProperty().bindBidirectional(updater.xProperty());
        y.numberProperty().bindBidirectional(updater.yProperty());
        z.numberProperty().bindBidirectional(updater.zProperty());
        items.setItems(updater.getOffers());
    }

    public void showDialog(Parent parent, Parent content, StationModel station){
        showDialog(parent, content, station.getSystem(), station);
    }

    public void showDialog(Parent parent, Parent content, SystemModel system, StationModel station){
        updater.init(system, station);
        Dialog dlg = new Dialog(parent, Localization.getString(station == null ? "vEditor.title.add" : "vEditor.title.edit"));
        dlg.setContent(content);
        dlg.getActions().addAll(actSave, actCancel);
        dlg.setResizable(false);
        dlg.show();
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
