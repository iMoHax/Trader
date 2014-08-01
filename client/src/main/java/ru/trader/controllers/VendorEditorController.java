package ru.trader.controllers;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.util.converter.DoubleStringConverter;
import org.controlsfx.control.ButtonBar;
import org.controlsfx.control.action.AbstractAction;
import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.core.OFFER_TYPE;
import ru.trader.model.*;
import ru.trader.model.support.BindingsHelper;
import ru.trader.view.support.ViewUtils;
import ru.trader.view.support.cells.TextFieldCell;

import java.util.Optional;


public class VendorEditorController {
    private final static Logger LOG = LoggerFactory.getLogger(VendorEditorController.class);

    private VendorModel vendor;

    private final Action actSave = new AbstractAction("Сохранить") {
        {
            ButtonBar.setType(this, ButtonBar.ButtonType.OK_DONE);
        }

        @Override
        public void handle(ActionEvent event) {
            Dialog dlg = (Dialog) event.getSource();
            saveChanges();
            dlg.hide();
        }
    };

    @FXML
    private TextField name;

    @FXML
    private TableView<FakeOffer> items;
    @FXML
    private TableColumn<FakeOffer, Double> buy;
    @FXML
    private TableColumn<FakeOffer, Double> sell;

    @FXML
    private void initialize() {
        items.getSelectionModel().setCellSelectionEnabled(true);
        buy.setCellFactory(TextFieldCell.forTableColumn(new DoubleStringConverter()));
        sell.setCellFactory(TextFieldCell.forTableColumn(new DoubleStringConverter()));
        fillItems();
    }

    public Action showDialog(Parent parent, Parent content, VendorModel vendor){
        this.vendor = vendor;
        reset();
        if (vendor != null) {
            fill();
        }
        Dialog dlg = new Dialog(parent, vendor == null ? "Добавление станции" : "Редактирование станции");
        dlg.setContent(content);
        dlg.getActions().addAll(actSave, Dialog.Actions.CANCEL);
        dlg.setResizable(false);
        return dlg.show();
    }


    private void fill(){
        name.setText(vendor.getName());
        vendor.getSells().forEach(this::fillOffer);
        vendor.getBuys().forEach(this::fillOffer);
    }

    private void reset(){
        name.setText("");
        items.getItems().forEach(FakeOffer::reset);
    }

    private void fillItems() {
        items.setItems(BindingsHelper.observableList(MainController.getMarket().itemsProperty(), (item) -> new FakeOffer(item.getItem())));
    }


    private void fillOffer(OfferModel offer) {
        for (FakeOffer o : items.getItems()) {
            if (offer.hasItem(o.item)) {
                switch (offer.getType()) {
                    case SELL:
                        o.setSell(offer);
                        break;
                    case BUY:
                        o.setBuy(offer);
                        break;
                }
                return;
            }
        }
    }

    public void up(){
        int index = items.getSelectionModel().getSelectedIndex();
        if (index>0){
            FakeOffer offer = items.getItems().remove(index);
            items.getItems().add(index-1, offer);
            selectRow(index - 1);
        }
    }

    public void down(){
        int index = items.getSelectionModel().getSelectedIndex();
        if (index>=0 && index<items.getItems().size()-1){
            FakeOffer offer = items.getItems().remove(index);
            items.getItems().add(index+1, offer);
            selectRow(index + 1);
        }
    }

    public void add() {
        Optional<ItemModel> item = Screeners.showAddItem();
        if (item.isPresent()){
            int index = items.getSelectionModel().getSelectedIndex();
            if (index<0) index = items.getItems().size()-1;
            items.getItems().add(index, new FakeOffer(item.get()));
            selectRow(index);
        }
    }

    private void selectRow(int index){
        items.requestFocus();
        items.getSelectionModel().select(index, items.getColumns().get(0));
        ViewUtils.show(items, index);
    }



    public void saveChanges(){
        LOG.info("Save vendor changes");
        items.getSelectionModel().clearSelection();
        final MarketModel market = MainController.getMarket();
        if (vendor == null) {
            market.setAlert(false);
            vendor = market.newVendor(name.getText());
            items.getItems().forEach((o) -> commit(market, vendor, o));
            market.setAlert(true);
            market.add(vendor);
        } else {
            vendor.setName(name.getText());
            items.getItems().forEach((o) -> commit(market, vendor, o));
        }
    }



    private void commit(MarketModel market, VendorModel vendor, FakeOffer offer){
        LOG.trace("Commit changes of offers {}", offer);
        if (offer.isBlank()){
            LOG.trace("Is blank offer, skip");
            return;
        }

        if (offer.isNewBuy()){
            LOG.trace("Is new buy offer");
            vendor.add(market.newOffer(OFFER_TYPE.BUY, offer.item, offer.getBprice()));
        } else if (offer.isRemoveBuy()) {
            LOG.trace("Is remove buy offer");
            vendor.remove(offer.buy);
        } else if (offer.isChangeBuy()){
            LOG.trace("Is change buy price to {}", offer.getBprice());
            offer.buy.setPrice(offer.getBprice());
        } else {
            LOG.trace("No change buy offer");
        }

        if (offer.isNewSell()){
            LOG.trace("Is new sell offer");
            vendor.add(market.newOffer(OFFER_TYPE.SELL, offer.item, offer.getSprice()));
        } else if (offer.isRemoveSell()) {
            LOG.trace("Is remove sell offer");
            vendor.remove(offer.sell);
        } else if (offer.isChangeSell()){
            LOG.trace("Is change sell price to {}", offer.getSprice());
            offer.sell.setPrice(offer.getSprice());
        } else {
            LOG.trace("No change sell offer");
        }
    }

    public class FakeOffer {
        private final ItemModel item;
        private DoubleProperty sprice;
        private DoubleProperty bprice;
        private OfferModel sell;
        private OfferModel buy;

        public FakeOffer(ItemModel item){
            this.item = item;
            this.sprice = new SimpleDoubleProperty(0);
            this.bprice = new SimpleDoubleProperty(0);
        }

        public ReadOnlyStringProperty nameProperty(){
            return item.nameProperty();
        }

        public double getSprice() {
            return sprice.get();
        }

        public void setSprice(double sprice) {
            this.sprice.set(sprice);
        }

        public double getBprice() {
            return bprice.get();
        }

        public void setBprice(double bprice) {
            this.bprice.set(bprice);
        }

        public DoubleProperty bpriceProperty() {
            return bprice;
        }

        public DoubleProperty spriceProperty() {
            return sprice;
        }

        public boolean isChangeSell() {
            return sell!=null && getSprice() != sell.getPrice();
        }

        public boolean isChangeBuy() {
            return buy!=null && getBprice() != buy.getPrice();
        }


        public boolean isNewSell() {
            return sell == null && getSprice() != 0;
        }

        public boolean isNewBuy() {
            return buy == null && getBprice() != 0;
        }

        public boolean isRemoveSell() {
            return sell != null && getSprice() ==0;
        }

        public boolean isRemoveBuy() {
            return buy != null && getBprice() ==0;
        }

        public boolean isBlank(){
            return sell == null && getSprice() == 0 && buy == null && getBprice() == 0;
        }

        public boolean hasItem(ItemModel item){
            return this.item.equals(item);
        }

        public void setSell(OfferModel sell) {
            this.sell = sell;
            sprice.set(sell.getPrice());
        }

        public void setBuy(OfferModel buy) {
            this.buy = buy;
            bprice.set(buy.getPrice());
        }

        public void reset(){
            sprice.setValue(0);
            bprice.setValue(0);
            this.sell = null;
            this.buy = null;
        }

        @Override
        public String toString() {
            return "FakeOffer{" +
                    "item=" + item +
                    ", sprice=" + sprice.get() +
                    ", bprice=" + bprice.get() +
                    ", sell=" + sell +
                    ", buy=" + buy +
                    '}';
        }
    }
}
