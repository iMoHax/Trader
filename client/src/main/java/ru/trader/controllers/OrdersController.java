package ru.trader.controllers;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.LongStringConverter;
import org.controlsfx.control.ButtonBar;
import org.controlsfx.control.action.AbstractAction;
import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;
import ru.trader.model.OfferDescModel;
import ru.trader.model.OfferModel;
import ru.trader.model.OrderModel;
import ru.trader.model.support.BindingsHelper;

import java.util.Collection;
import java.util.Optional;

public class OrdersController {
    private final Action OK = new AbstractAction("OK") {
        {
            ButtonBar.setType(this, ButtonBar.ButtonType.OK_DONE);
        }


        @Override
        public void handle(ActionEvent event) {
            Dialog dlg = (Dialog) event.getSource();
            dlg.hide();
        }
    };

    @FXML
    private TableView<OrderModel> tblOrders;

    @FXML
    private TableView<OfferModel> tblBuyers;

    @FXML
    private TableColumn<OrderModel, Long> count;

    @FXML
    private TableColumn<OrderModel, Long> maxCount;

    @FXML
    private TableColumn<OfferModel, Double> curProfit;

    @FXML
    private TableColumn<OfferModel, Double> curDistance;

    private OrderModel order;

    @FXML
    private void initialize() {
        count.setCellFactory(TextFieldTableCell.forTableColumn(new LongStringConverter()));
        maxCount.setCellFactory(TextFieldTableCell.forTableColumn(new LongStringConverter()));
        tblOrders.getSelectionModel().selectedItemProperty().addListener((v, o, n) -> changeOrder(n));
        tblBuyers.getSelectionModel().selectedItemProperty().addListener((v, o, n) -> setBuyer(n));
        curProfit.setCellValueFactory(param -> {
            OfferModel offer = param.getValue();
            return order !=null ? order.getProfit(offer) : new SimpleDoubleProperty(Double.NaN).asObject();
        });
        curDistance.setCellValueFactory(param -> {
            OfferModel offer = param.getValue();
            return new SimpleDoubleProperty(order !=null ? order.getVendor().getDistance(offer.getVendor()) :Double.NaN).asObject();
        });
    }


    public Collection<OrderModel> showDialog(Parent parent, Parent content, Collection<OfferDescModel> offers, double balance, long max) {

        init(offers, balance, max);

        Dialog dlg = new Dialog(parent, "Создание заказов");
        dlg.setContent(content);
        dlg.getActions().addAll(OK, Dialog.Actions.CANCEL);
        dlg.setResizable(false);
        return dlg.show() == OK ? getOrders() : null;
    }

    private Collection<OrderModel> getOrders() {
        return tblOrders.getItems().filtered((o) -> o.getCount()>0 && o.getBuyer()!=null);
    }

    private void init(Collection<OfferDescModel> offers, double balance, long max) {
        tblOrders.setItems(BindingsHelper.observableList(offers, (o) -> new OrderModel(o, balance, max)));
        if (tblOrders.getSortOrder().size()>0)
            tblOrders.sort();
    }

    private void changeOrder(OrderModel order) {
        this.order =  order;
        if (order != null) tblBuyers.setItems(FXCollections.observableList(order.getBuyers()));
            else tblBuyers.setItems(FXCollections.emptyObservableList());
        tblBuyers.getSelectionModel().clearSelection();
        if (tblBuyers.getSortOrder().size()>0)
            tblBuyers.sort();

    }

    private void setBuyer(OfferModel offer) {
        if (order != null && offer!=null) {
            order.setBuyer(offer);
            order.setCount(order.getMax());
        }
    }

}