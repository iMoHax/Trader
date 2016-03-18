package ru.trader.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextField;
import ru.trader.core.GROUP_TYPE;
import ru.trader.model.GroupModel;
import ru.trader.model.MarketModel;
import ru.trader.view.support.Localization;

import java.util.Optional;

public class GroupAddController {
    @FXML
    private ComboBox<GROUP_TYPE> type;
    @FXML
    private TextField name;

    private Dialog<GroupModel> dlg;
    private MarketModel market;

    @FXML
    private void initialize() {
        type.setItems(FXCollections.observableArrayList(GROUP_TYPE.values()));
        name.clear();
    }

    private void createDialog(Parent owner, Parent content){
        dlg = new Dialog<>();
        if (owner != null) dlg.initOwner(owner.getScene().getWindow());
        dlg.setTitle(Localization.getString("dialog.group.title"));
        dlg.getDialogPane().setContent(content);
        dlg.getDialogPane().getButtonTypes().addAll(Dialogs.OK, Dialogs.CANCEL);
        dlg.setResultConverter(dialogButton -> {
            if (dialogButton == Dialogs.OK) {
                return add(market);
            }
            return null;
        });
        dlg.setResizable(false);
    }

    private void clear(){
        this.market = null;
        name.clear();
        type.getSelectionModel().clearSelection();
    }

    private void fill(MarketModel market){
        this.market = market;
    }

    public Optional<GroupModel> showDialog(Parent parent, Parent content, MarketModel market) {
        if (dlg == null){
            createDialog(parent, content);
        }
        fill(market);
        Optional<GroupModel> res = dlg.showAndWait();
        clear();
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
