package ru.trader.controllers;

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
import ru.trader.model.OrderModel;
import ru.trader.model.support.BindingsHelper;
import ru.trader.view.support.Localization;

import java.util.Collection;
import java.util.List;

public class TopOrdersController {
    private final Action OK = new DialogAction("OK", ButtonBar.ButtonType.OK_DONE, false, true, false);

    @FXML
    private TableView<OrderModel> tblOrders;

    @FXML
    private TableColumn<OrderModel, Long> count;

    private OrderModel order;

    private final List<OrderModel> orders = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        count.setCellFactory(TextFieldTableCell.forTableColumn(new LongStringConverter()));
        tblOrders.getSelectionModel().selectedItemProperty().addListener((v, o, n) -> changeOrder(n));
        BindingsHelper.setTableViewItems(tblOrders, orders);
    }


    public OrderModel showDialog(Parent parent, Parent content, Collection<OrderModel> orders) {

        init(orders);

        Dialog dlg = new Dialog(parent, Localization.getString("topOrders.title"));
        dlg.setContent(content);
        dlg.getActions().addAll(OK, Dialog.ACTION_CANCEL);
        dlg.setResizable(false);
        OrderModel res = dlg.show() == OK ? order : null;
        this.orders.clear();
        return res;
    }


    private void init(Collection<OrderModel> orders) {
        tblOrders.getSelectionModel().clearSelection();
        this.orders.addAll(orders);
    }

    private void changeOrder(OrderModel order) {
        this.order =  order;
    }

}
