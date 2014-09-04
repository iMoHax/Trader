package ru.trader.controllers;

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
import ru.trader.model.OrderModel;
import ru.trader.view.support.Localization;

import java.util.Collection;

public class TopOrdersController {
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
    private TableColumn<OrderModel, Long> count;

    private OrderModel order;

    @FXML
    private void initialize() {
        count.setCellFactory(TextFieldTableCell.forTableColumn(new LongStringConverter()));
        tblOrders.getSelectionModel().selectedItemProperty().addListener((v, o, n) -> changeOrder(n));
    }


    public OrderModel showDialog(Parent parent, Parent content, Collection<OrderModel> orders) {

        init(orders);

        Dialog dlg = new Dialog(parent, Localization.getString("topOrders.title"));
        dlg.setContent(content);
        dlg.getActions().addAll(OK, Dialog.Actions.CANCEL);
        dlg.setResizable(false);
        OrderModel res = dlg.show() == OK ? order : null;
        tblOrders.getItems().clear();
        return res;
    }


    private void init(Collection<OrderModel> orders) {
        tblOrders.getSelectionModel().clearSelection();
        tblOrders.setItems(FXCollections.observableArrayList(orders));
        if (tblOrders.getSortOrder().size()>0)
            tblOrders.sort();
    }

    private void changeOrder(OrderModel order) {
        this.order =  order;
    }

}
