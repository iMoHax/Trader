package ru.trader.controllers;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import org.controlsfx.control.ButtonBar;
import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;
import org.controlsfx.dialog.DialogAction;
import ru.trader.model.GroupModel;
import ru.trader.model.ItemModel;
import ru.trader.model.MarketModel;
import ru.trader.view.support.Localization;

import java.util.Optional;


public class ItemAddController {
    private final Action OK = new DialogAction("OK", ButtonBar.ButtonType.OK_DONE, false, true, false);

    @FXML
    private ComboBox<GroupModel> group;
    @FXML
    private TextField name;

    @FXML
    private void initialize() {

    }

    private void init(MarketModel market) {
        group.setItems(market.getGroups());
        group.getSelectionModel().selectFirst();
        name.clear();
    }

    public ItemModel showDialog(Parent parent, Parent content, MarketModel market) {
        init(market);
        Dialog dlg = new Dialog(parent, Localization.getString("dialog.item.title"));
        dlg.setContent(content);
        dlg.getActions().addAll(OK, Dialog.ACTION_CANCEL);
        dlg.setResizable(false);
        ItemModel res = dlg.show() == OK ? add(market) : null;
        return res;
    }

    private ItemModel add(MarketModel market){
        GroupModel g = group.getValue();
        String id = name.getText();
        ItemModel res = null;
        if (g != null && id.length() > 0){
            res = market.add(id, g);
        }
        return res;
    }


    public void add(ActionEvent actionEvent) {
        Optional<GroupModel> _group = Screeners.showAddGroup();
        if (_group.isPresent()){
            group.setValue(_group.get());
        }
    }
}
