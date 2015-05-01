package ru.trader.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.LongStringConverter;
import ru.trader.model.OrderModel;
import ru.trader.model.support.BindingsHelper;
import ru.trader.view.support.Localization;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class TopOrdersController {
    @FXML
    private TableView<OrderModel> tblOrders;

    @FXML
    private TableColumn<OrderModel, Long> count;

    private OrderModel order;
    private Dialog<OrderModel> dlg;

    private final List<OrderModel> orders = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        count.setCellFactory(TextFieldTableCell.forTableColumn(new LongStringConverter()));
        tblOrders.getSelectionModel().selectedItemProperty().addListener((v, o, n) -> changeOrder(n));
        BindingsHelper.setTableViewItems(tblOrders, orders);
    }

    private void createDialog(Parent owner, Parent content){
        dlg = new Dialog<>();
        if (owner != null) dlg.initOwner(owner.getScene().getWindow());
        dlg.setTitle(Localization.getString("topOrders.title"));
        dlg.getDialogPane().setContent(content);
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dlg.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return order;
            }
            return null;
        });
        dlg.setResizable(false);
    }

    public Optional<OrderModel> showDialog(Parent parent, Parent content, Collection<OrderModel> orders) {
        if (dlg == null){
            createDialog(parent, content);
        }
        fill(orders);
        Optional<OrderModel> res = dlg.showAndWait();
        clear();
        return res;
    }


    private void fill(Collection<OrderModel> orders) {
        tblOrders.getSelectionModel().clearSelection();
        this.orders.addAll(orders);
    }

    private void clear(){
        orders.clear();
    }

    private void changeOrder(OrderModel order) {
        this.order =  order;
    }

}
