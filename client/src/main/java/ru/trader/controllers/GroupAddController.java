package ru.trader.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import org.controlsfx.control.ButtonBar;
import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;
import org.controlsfx.dialog.DialogAction;
import ru.trader.core.GROUP_TYPE;
import ru.trader.model.GroupModel;
import ru.trader.model.MarketModel;
import ru.trader.view.support.Localization;

public class GroupAddController {
    private final Action OK = new DialogAction("OK", ButtonBar.ButtonType.OK_DONE, false, true, false);

    @FXML
    private ComboBox<GROUP_TYPE> type;
    @FXML
    private TextField name;


    @FXML
    private void initialize() {
        type.setItems(FXCollections.observableArrayList(GROUP_TYPE.values()));
        type.getSelectionModel().selectFirst();
        name.clear();
    }

    public GroupModel showDialog(Parent parent, Parent content, MarketModel market) {

        Dialog dlg = new Dialog(parent, Localization.getString("dialog.group.title"));
        dlg.setContent(content);
        dlg.getActions().addAll(OK, Dialog.ACTION_CANCEL);
        dlg.setResizable(false);
        GroupModel res = dlg.show() == OK ? add(market) : null;
        return res;
    }

    private GroupModel add(MarketModel market){
        GROUP_TYPE t = type.getValue();
        String id = name.getText();
        GroupModel res = null;
        if (t != null && id.length() > 0){
            res = market.addGroup(id, t);
        }
        return res;
    }

}
