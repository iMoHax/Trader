package ru.trader.controllers;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.LongStringConverter;
import org.controlsfx.control.ButtonBar;
import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;
import org.controlsfx.dialog.DialogAction;
import ru.trader.model.OfferModel;
import ru.trader.model.OrderModel;
import ru.trader.model.support.BindingsHelper;
import ru.trader.view.support.Localization;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

public class OrdersController {
    private final Action OK = new DialogAction("OK", ButtonBar.ButtonType.OK_DONE, false, true, false);

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

    private final List<OrderModel> orders = FXCollections.observableArrayList();
    private final List<OfferModel> buyers = FXCollections.observableArrayList();
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
            return new SimpleDoubleProperty(order !=null ? order.getStation().getDistance(offer.getStation()) :Double.NaN).asObject();
        });
        BindingsHelper.setTableViewItems(tblOrders, orders);
        BindingsHelper.setTableViewItems(tblBuyers, buyers);

    }


    public Collection<OrderModel> showDialog(Parent parent, Parent content, Collection<OfferModel> offers, double balance, long max) {

        init(offers, balance, max);

        Dialog dlg = new Dialog(parent, Localization.getString("orders.title"));
        dlg.setContent(content);
        dlg.getActions().addAll(OK, Dialog.ACTION_CANCEL);
        dlg.setResizable(false);
        Collection<OrderModel> res = dlg.show() == OK ? getOrders() : null;
        orders.clear();
        return res;
    }

    private List<OrderModel> getOrders() {
        return orders.stream().filter(o -> o.getCount() > 0 && o.getBuyOffer() != null).collect(toList());
    }

    private void init(Collection<OfferModel> offers, double balance, long max) {
        orders.clear();
        offers.forEach(o -> orders.add(new OrderModel(o, balance, max)));
    }

    private void changeOrder(OrderModel order) {
        this.order =  order;
        buyers.clear();
        if (order != null) buyers.addAll(order.getBuyers());
        tblBuyers.getSelectionModel().clearSelection();
    }

    private void setBuyer(OfferModel offer) {
        if (order != null && offer!=null) {
            order.setBuyOffer(offer);
            order.setCount(order.getMax());
        }
    }

}