package ru.trader.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import org.controlsfx.control.ButtonBar;
import org.controlsfx.control.action.AbstractAction;
import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.DefaultDialogAction;
import org.controlsfx.dialog.Dialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.EMDNUpdater;
import ru.trader.emdn.ItemData;
import ru.trader.emdn.Station;
import ru.trader.model.*;
import ru.trader.model.support.VendorUpdater;
import ru.trader.view.support.Localization;
import ru.trader.view.support.NumberField;
import ru.trader.view.support.PriceStringConverter;
import ru.trader.view.support.ViewUtils;
import ru.trader.view.support.cells.EditOfferCell;

import java.util.Optional;


public class VendorEditorController {
    private final static Logger LOG = LoggerFactory.getLogger(VendorEditorController.class);

    private final Action actSave = new AbstractAction(Localization.getString("dialog.button.save")) {
        {
            ButtonBar.setType(this, ButtonBar.ButtonType.OK_DONE);
        }

        @Override
        public void handle(ActionEvent event) {
            Dialog dlg = (Dialog) event.getSource();
            items.getSelectionModel().selectFirst();
            updater.commit();
            items.getSelectionModel().clearSelection();
            dlg.hide();
        }
    };

    private final Action actCancel = new DefaultDialogAction(impl.org.controlsfx.i18n.Localization.asKey("dlg.cancel.button")) {
        {
            ButtonBar.setType(this, ButtonBar.ButtonType.CANCEL_CLOSE);
        }

        @Override
        public void handle(ActionEvent event) {
            items.getSelectionModel().selectFirst();
            items.getSelectionModel().clearSelection();
            super.handle(event);
        }
    };

    @FXML
    private TextField name;

    @FXML
    private TableView<VendorUpdater.FakeOffer> items;
    @FXML
    private TableColumn<VendorUpdater.FakeOffer, Double> buy;
    @FXML
    private TableColumn<VendorUpdater.FakeOffer, Double> sell;

    @FXML
    private NumberField x;
    @FXML
    private NumberField y;
    @FXML
    private NumberField z;

    private VendorUpdater updater;


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
        updater = new VendorUpdater(MainController.getMarket());
        name.textProperty().bindBidirectional(updater.nameProperty());
        x.numberProperty().bindBidirectional(updater.xProperty());
        y.numberProperty().bindBidirectional(updater.yProperty());
        z.numberProperty().bindBidirectional(updater.zProperty());
        items.setItems(updater.getOffers());
    }

    public Action showDialog(Parent parent, Parent content, VendorModel vendor){
        updater.reset();
        updater.init(vendor);
        Dialog dlg = new Dialog(parent, Localization.getString(vendor == null ? "vEditor.title.add" : "vEditor.title.edit"));
        dlg.setContent(content);
        dlg.getActions().addAll(actSave, actCancel);
        dlg.setResizable(false);
        return dlg.show();
    }

    public void up(){
        int index = items.getSelectionModel().getSelectedIndex();
        if (index>0){
            VendorUpdater.FakeOffer offer = items.getItems().remove(index);
            items.getItems().add(index-1, offer);
            selectRow(index - 1);
        }
    }

    public void down(){
        int index = items.getSelectionModel().getSelectedIndex();
        if (index>=0 && index<items.getItems().size()-1){
            VendorUpdater.FakeOffer offer = items.getItems().remove(index);
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
